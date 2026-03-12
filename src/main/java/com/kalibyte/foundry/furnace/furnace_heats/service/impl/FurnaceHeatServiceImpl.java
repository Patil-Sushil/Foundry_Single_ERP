package com.kalibyte.foundry.furnace.furnace_heats.service.impl;

import com.kalibyte.foundry.common.exception.InsufficientStockException;
import com.kalibyte.foundry.common.exception.ResourceNotFoundException;
import com.kalibyte.foundry.furnace.furnace_heats.dto.FurnaceHeatRequest;
import com.kalibyte.foundry.furnace.furnace_heats.dto.FurnaceHeatResponse;
import com.kalibyte.foundry.furnace.furnace_heats.dto.HeatMaterialItemRequest;
import com.kalibyte.foundry.furnace.furnace_heats.dto.HeatsByOrderResponse;
import com.kalibyte.foundry.furnace.furnace_heats.entity.FurnaceHeats;
import com.kalibyte.foundry.furnace.furnace_heats.entity.HeatMaterialItem;
import com.kalibyte.foundry.furnace.furnace_heats.repository.FurnaceHeatsRepository;
import com.kalibyte.foundry.furnace.furnace_heats.service.FurnaceHeatService;
import com.kalibyte.foundry.furnace.furnace_report.entity.Furnace;
import com.kalibyte.foundry.furnace.furnace_report.repository.FurnaceRepository;
import com.kalibyte.foundry.inventory.department.entity.Department;
import com.kalibyte.foundry.inventory.department.repository.DepartmentRepository;
import com.kalibyte.foundry.inventory.issue.dto.request.IssueItemRequest;
import com.kalibyte.foundry.inventory.issue.dto.request.RecordIssueRequest;
import com.kalibyte.foundry.inventory.issue.service.MaterialIssueService;
import com.kalibyte.foundry.inventory.item.entity.Item;
import com.kalibyte.foundry.inventory.item.repository.ItemRepository;
import com.kalibyte.foundry.order.entity.Order;
import com.kalibyte.foundry.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FurnaceHeatServiceImpl implements FurnaceHeatService {

    private final FurnaceHeatsRepository furnaceHeatsRepository;
    private final FurnaceRepository furnaceRepository;
    private final ItemRepository itemRepository;
    private final MaterialIssueService materialIssueService;
    private final DepartmentRepository departmentRepository;
    private final ModelMapper modelMapper;
    private final OrderRepository orderRepository;

    @Override
    @Transactional(readOnly = true)
    public List<FurnaceHeatResponse> getHeatsByReportId(Long reportId) {
        if (!furnaceRepository.existsById(reportId)) {
            throw new ResourceNotFoundException("Furnace report not found with id: " + reportId);
        }
        return furnaceHeatsRepository.findByFurnaceId(reportId).stream()
                .map(heat -> modelMapper.map(heat, FurnaceHeatResponse.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public FurnaceHeatResponse getHeatById(Long heatId) {
        FurnaceHeats heat = furnaceHeatsRepository.findById(heatId)
                .orElseThrow(() -> new ResourceNotFoundException("Furnace heat not found with id: " + heatId));
        return modelMapper.map(heat, FurnaceHeatResponse.class);
    }

    @Override
    @Transactional
    public FurnaceHeatResponse createHeat(Long reportId, FurnaceHeatRequest request) {
        Furnace furnace = furnaceRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Furnace report not found with id: " + reportId));

        FurnaceHeats heat = modelMapper.map(request, FurnaceHeats.class);
        heat.setId(null); // Ensure it's treated as a new entity even if an ID was passed in the request
        furnace.addHeat(heat); // Bidirectional maintenance
        calculateHeatFields(heat);

        if (heat.getMaterialsUsed() != null) {
            heat.getMaterialsUsed().clear();
        }

        if (request.getMaterialsUsed() != null && !request.getMaterialsUsed().isEmpty()) {
            processAndIssueMaterials(heat, request.getMaterialsUsed(), furnace.getFurnaceRefNo());
        }

        FurnaceHeats savedHeat = furnaceHeatsRepository.save(heat);
        return modelMapper.map(savedHeat, FurnaceHeatResponse.class);
    }

    @Override
    @Transactional
    public FurnaceHeatResponse updateHeat(Long heatId, FurnaceHeatRequest request) {
        FurnaceHeats existingHeat = furnaceHeatsRepository.findById(heatId)
                .orElseThrow(() -> new ResourceNotFoundException("Furnace heat not found with id: " + heatId));

        // Update fields
        existingHeat.setSipercentage(request.getSipercentage());
        existingHeat.setCpcpercentage(request.getCpcpercentage());
        existingHeat.setMgpercentage(request.getMgpercentage());
        existingHeat.setStartReading(request.getStartReading());
        existingHeat.setStopReading(request.getStopReading());
        existingHeat.setTotalWeight(request.getTotalWeight());
        existingHeat.setPouringTemp(request.getPouringTemp());
        existingHeat.setPouringStartTime(request.getPouringStartTime());
        existingHeat.setPouringEndTime(request.getPouringEndTime());
        //

        if (request.getOrderId() != null) {
            Order order = orderRepository.findById(request.getOrderId())
                    .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + request.getOrderId()));

            existingHeat.setOrder(order);
        }
        
        calculateHeatFields(existingHeat);

        handleMaterialDelta(existingHeat, request.getMaterialsUsed(), existingHeat.getFurnace().getFurnaceRefNo());

        FurnaceHeats updatedHeat = furnaceHeatsRepository.save(existingHeat);
        return modelMapper.map(updatedHeat, FurnaceHeatResponse.class);
    }

    @Override
    @Transactional(readOnly = true)
    public HeatsByOrderResponse getHeatsByOrderId(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        List<FurnaceHeats> heats = furnaceHeatsRepository.findByOrderIdWithMaterials(orderId);

        List<FurnaceHeatResponse> heatResponses = heats.stream()
                .map(this::mapToResponse) // your existing mapper
                .toList();

        return new HeatsByOrderResponse(
                order.getId(),
                order.getOrderNumber(),
                heatResponses
        );

    }

    private FurnaceHeatResponse mapToResponse(FurnaceHeats heat) {
        FurnaceHeatResponse response = modelMapper.map(heat, FurnaceHeatResponse.class);
        if (heat.getOrder() != null) {
            response.setOrderId(heat.getOrder().getId());
        } else {
            response.setOrderId(null);
        }
        return response;
    }


    @Override
    @Transactional
    public void deleteHeat(Long heatId) {
        FurnaceHeats heat = furnaceHeatsRepository.findById(heatId)
                .orElseThrow(() -> new ResourceNotFoundException("Furnace heat not found with id: " + heatId));
        
        reverseAllMaterialIssuances(heat);
        furnaceHeatsRepository.delete(heat);
    }

    @Override
    @Transactional
    public void deleteAllHeatsByReportId(Long reportId) {
        List<FurnaceHeats> heats = furnaceHeatsRepository.findByFurnaceId(reportId);
        for (FurnaceHeats heat : heats) {
            reverseAllMaterialIssuances(heat);
        }
        furnaceHeatsRepository.deleteAll(heats);
    }

    private void calculateHeatFields(FurnaceHeats heat) {
        heat.setDifferenceReading(heat.getStopReading() - heat.getStartReading());
        if (heat.getTotalWeight() != 0) {
            heat.setPowerToWeight(heat.getDifferenceReading() / heat.getTotalWeight());
        } else {
            heat.setPowerToWeight(0.0);
        }
    }

    private void processAndIssueMaterials(FurnaceHeats heat, List<HeatMaterialItemRequest> materialRequests, String furnaceRefNo) {
        List<IssueItemRequest> issueItems = new ArrayList<>();
        Department furnaceDept = getFurnaceDepartment();

        for (HeatMaterialItemRequest req : materialRequests) {
            Item item = itemRepository.findById(req.getItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("Item with ID " + req.getItemId() + " not found in inventory."));

            BigDecimal requestedQty = BigDecimal.valueOf(req.getQuantityUsed());
            if (item.getCurrentStock().compareTo(requestedQty) < 0) {
                throw new InsufficientStockException(String.format(
                        "Insufficient stock for item '%s' (ID: %d). Available: %s, Requested: %s",
                        item.getName(), item.getId(), item.getCurrentStock(), requestedQty));
            }

            HeatMaterialItem material = HeatMaterialItem.builder()
                    .heat(heat)
                    .itemId(req.getItemId())
                    .itemName(item.getName())
                    .materialType(req.getMaterialType())
                    .quantityUsed(req.getQuantityUsed())
                    .unitRate(item.getAvgRate().doubleValue())
                    .totalCost(req.getQuantityUsed() * item.getAvgRate().doubleValue())
                    .build();

            heat.addMaterial(material);
            issueItems.add(new IssueItemRequest(req.getItemId(), requestedQty, "Furnace Heat Usage"));
        }

        if (!issueItems.isEmpty()) {
            RecordIssueRequest issueRequest = new RecordIssueRequest(
                    furnaceDept.getId(),
                    String.format("Heat usage - %s", furnaceRefNo),
                    LocalDate.now(),
                    issueItems,
                    "Automatic issuance from Furnace Heat"
            );
            materialIssueService.recordIssue(issueRequest);
        }
    }

    private void handleMaterialDelta(FurnaceHeats existingHeat, List<HeatMaterialItemRequest> newRequests, String furnaceRefNo) {
        if (newRequests == null) newRequests = new ArrayList<>();

        Map<String, HeatMaterialItem> existingMap = existingHeat.getMaterialsUsed().stream()
                .collect(Collectors.toMap(m -> m.getItemId() + "_" + m.getMaterialType(), m -> m));

        List<IssueItemRequest> additions = new ArrayList<>();
        Department furnaceDept = getFurnaceDepartment();

        for (HeatMaterialItemRequest req : newRequests) {
            String key = req.getItemId() + "_" + req.getMaterialType();
            HeatMaterialItem existing = existingMap.remove(key);

            if (existing != null) {
                // Case A: Exists in both
                double delta = req.getQuantityUsed() - existing.getQuantityUsed();
                if (delta > 0) {
                    // Need more
                    Item item = itemRepository.findById(req.getItemId()).orElseThrow();
                    BigDecimal deltaBD = BigDecimal.valueOf(delta);
                    if (item.getCurrentStock().compareTo(deltaBD) < 0) {
                        throw new InsufficientStockException(String.format("Insufficient stock for item '%s'.", item.getName()));
                    }
                    additions.add(new IssueItemRequest(req.getItemId(), deltaBD, "Heat update addition"));
                    existing.setUnitRate(item.getAvgRate().doubleValue()); // Update to current rate for the whole thing or just delta? Simplified: current rate
                } else if (delta < 0) {
                    // Return some
                    Item item = itemRepository.findById(req.getItemId()).orElseThrow();
                    item.setCurrentStock(item.getCurrentStock().add(BigDecimal.valueOf(Math.abs(delta))));
                    itemRepository.save(item);
                }
                existing.setQuantityUsed(req.getQuantityUsed());
                existing.setTotalCost(existing.getQuantityUsed() * existing.getUnitRate());
            } else {
                // Case C: New addition
                Item item = itemRepository.findById(req.getItemId())
                        .orElseThrow(() -> new ResourceNotFoundException("Item ID " + req.getItemId() + " not found."));
                
                BigDecimal qtyBD = BigDecimal.valueOf(req.getQuantityUsed());
                if (item.getCurrentStock().compareTo(qtyBD) < 0) {
                    throw new InsufficientStockException("Insufficient stock for new item " + item.getName());
                }

                HeatMaterialItem material = HeatMaterialItem.builder()
                        .heat(existingHeat)
                        .itemId(req.getItemId())
                        .itemName(item.getName())
                        .materialType(req.getMaterialType())
                        .quantityUsed(req.getQuantityUsed())
                        .unitRate(item.getAvgRate().doubleValue())
                        .totalCost(req.getQuantityUsed() * item.getAvgRate().doubleValue())
                        .build();
                
                existingHeat.addMaterial(material);
                additions.add(new IssueItemRequest(req.getItemId(), qtyBD, "Heat update new item"));
            }
        }

        // Case B: Removed
        for (HeatMaterialItem removed : existingMap.values()) {
            Item item = itemRepository.findById(removed.getItemId()).orElse(null);
            if (item != null) {
                item.setCurrentStock(item.getCurrentStock().add(BigDecimal.valueOf(removed.getQuantityUsed())));
                itemRepository.save(item);
            }
            existingHeat.removeMaterial(removed);
        }

        if (!additions.isEmpty()) {
            RecordIssueRequest issueRequest = new RecordIssueRequest(
                    furnaceDept.getId(),
                    String.format("Heat update addition - %s", furnaceRefNo),
                    LocalDate.now(),
                    additions,
                    "Automatic issuance from Furnace Heat Update"
            );
            materialIssueService.recordIssue(issueRequest);
        }
    }

    private void reverseAllMaterialIssuances(FurnaceHeats heat) {
        if (heat.getMaterialsUsed() == null) return;
        for (HeatMaterialItem material : heat.getMaterialsUsed()) {
            Item item = itemRepository.findById(material.getItemId()).orElse(null);
            if (item != null) {
                item.setCurrentStock(item.getCurrentStock().add(BigDecimal.valueOf(material.getQuantityUsed())));
                itemRepository.save(item);
            }
        }
    }

    private Department getFurnaceDepartment() {
        return departmentRepository.findByName("FURNACE")
                .orElseGet(() -> {
                    Department dept = new Department();
                    dept.setName("FURNACE");
                    dept.setCode("FUR");
                    return departmentRepository.save(dept);
                });
    }
}
