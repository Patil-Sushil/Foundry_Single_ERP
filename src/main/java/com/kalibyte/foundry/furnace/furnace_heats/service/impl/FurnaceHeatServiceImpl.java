package com.kalibyte.foundry.furnace.furnace_heats.service.impl;

import com.kalibyte.foundry.common.exception.BusinessException;
import com.kalibyte.foundry.common.exception.InsufficientStockException;
import com.kalibyte.foundry.common.exception.ResourceNotFoundException;
import com.kalibyte.foundry.furnace.furnace_heats.dto.request.FurnaceHeatRequest;
import com.kalibyte.foundry.furnace.furnace_heats.dto.request.HeatOrderItemRequest;
import com.kalibyte.foundry.furnace.furnace_heats.dto.response.FurnaceHeatResponse;
import com.kalibyte.foundry.furnace.furnace_heats.dto.request.HeatMaterialItemRequest;
import com.kalibyte.foundry.furnace.furnace_heats.dto.response.HeatsByOrderResponse;
import com.kalibyte.foundry.furnace.furnace_heats.entity.FurnaceHeats;
import com.kalibyte.foundry.furnace.furnace_heats.entity.HeatMaterialItem;
import com.kalibyte.foundry.furnace.furnace_heats.entity.HeatOrderItem;
import com.kalibyte.foundry.furnace.furnace_heats.mapper.HeatOrderItemMapper;
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
import com.kalibyte.foundry.inventory.item.entity.enums.ItemCategory;
import com.kalibyte.foundry.inventory.item.entity.enums.ItemSubCategory;
import com.kalibyte.foundry.inventory.item.entity.enums.ItemUnit;
import com.kalibyte.foundry.inventory.item.repository.ItemRepository;
import com.kalibyte.foundry.order.entity.Order;
import com.kalibyte.foundry.order.entity.OrderItem;
import com.kalibyte.foundry.order.repository.OrderRepository;
import com.kalibyte.foundry.order.repository.OrderItemRepository;
import com.kalibyte.foundry.furnace.furnace_heats.mapper.FurnaceHeatMapper;
import com.kalibyte.foundry.scrap.entity.ScrapEntry;
import com.kalibyte.foundry.scrap.entity.ScrapItem;
import com.kalibyte.foundry.scrap.enums.ConfidenceLevel;
import com.kalibyte.foundry.scrap.enums.ScrapSource;
import com.kalibyte.foundry.scrap.enums.ScrapStatus;
import com.kalibyte.foundry.scrap.enums.VerificationMethod;
import com.kalibyte.foundry.scrap.repository.ScrapEntryRepository;
import lombok.RequiredArgsConstructor;
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
    private final FurnaceHeatMapper furnaceHeatMapper;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final HeatOrderItemMapper heatOrderItemMapper;
    private final ScrapEntryRepository scrapEntryRepository;

    @Override
    @Transactional(readOnly = true)
    public List<FurnaceHeatResponse> getHeatsByReportId(Long reportId) {
        if (!furnaceRepository.existsById(reportId)) {
            throw new ResourceNotFoundException("Furnace report not found with id: " + reportId);
        }
        return furnaceHeatsRepository.findByFurnaceId(reportId).stream()
                .map(furnaceHeatMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public FurnaceHeatResponse getHeatById(Long heatId) {
        FurnaceHeats heat = furnaceHeatsRepository.findById(heatId)
                .orElseThrow(() -> new ResourceNotFoundException("Furnace heat not found with id: " + heatId));
        return furnaceHeatMapper.toResponse(heat);
    }

    @Override
    @Transactional
    public FurnaceHeatResponse createHeat(Long reportId, FurnaceHeatRequest request) {
        Furnace furnace = furnaceRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Furnace report not found with id: " + reportId));

        FurnaceHeats heat = furnaceHeatMapper.toEntity(request);
        furnace.addHeat(heat); // Bidirectional maintenance
        calculateHeatFields(heat);

        // Fetch the order if orderId is provided to ensure full entity is set
        if (request.getOrderId() != null) {
            Order order = orderRepository.findById(request.getOrderId())
                    .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + request.getOrderId()));
            heat.setOrder(order);
        }

        if (heat.getMaterialsUsed() != null) {
            heat.getMaterialsUsed().clear();
        }

        if (request.getMaterialsUsed() != null && !request.getMaterialsUsed().isEmpty()) {
            processAndIssueMaterials(heat, request.getMaterialsUsed(), furnace.getFurnaceRefNo());
        }

        // Handle HeatOrderItems
        if (request.getHeatOrderItems() != null) {
            for (HeatOrderItemRequest itemReq : request.getHeatOrderItems()) {
                HeatOrderItem heatOrderItem = heatOrderItemMapper.toEntity(itemReq);
                if (itemReq.getOrderItemId() != null) {
                    OrderItem orderItem = orderItemRepository.findById(itemReq.getOrderItemId())
                            .orElseThrow(() -> new ResourceNotFoundException("OrderItem not found with id: " + itemReq.getOrderItemId()));
                    // Validate grade match
                    if (!orderItem.getMaterialGrade().equals(heat.getGrade())) {
                        throw new BusinessException(
                            String.format("Grade mismatch! Heat grade: %s, Order item grade: %s", heat.getGrade(), orderItem.getMaterialGrade()));
                    }
                    heatOrderItem.setOrderItem(orderItem);
                }
                heat.addHeatOrderItem(heatOrderItem);
            }
        }

        // Validate metal balance before saving
        heat.validateMetalBalance();

        FurnaceHeats savedHeat = furnaceHeatsRepository.save(heat);

        // Auto-create scrap entry if needed
        if (Boolean.TRUE.equals(savedHeat.getAutoReturnScrap())) {
            createProcessScrapEntry(savedHeat);
        }

        return furnaceHeatMapper.toResponse(savedHeat);
    }

    @Override
    @Transactional
    public FurnaceHeatResponse updateHeat(Long heatId, FurnaceHeatRequest request) {
        FurnaceHeats existingHeat = furnaceHeatsRepository.findById(heatId)
                .orElseThrow(() -> new ResourceNotFoundException("Furnace heat not found with id: " + heatId));

        furnaceHeatMapper.updateEntity(request, existingHeat);

        if (request.getOrderId() != null) {
            Order order = orderRepository.findById(request.getOrderId())
                    .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + request.getOrderId()));

            existingHeat.setOrder(order);
        }
        
        calculateHeatFields(existingHeat);

        handleMaterialDelta(existingHeat, request.getMaterialsUsed(), existingHeat.getFurnace().getFurnaceRefNo());

        // Handle HeatOrderItems delta
        handleHeatOrderItemDelta(existingHeat, request.getHeatOrderItems());

        // Validate metal balance before saving
        existingHeat.validateMetalBalance();

        FurnaceHeats updatedHeat = furnaceHeatsRepository.save(existingHeat);

        // Update scrap entry if it exists
        if (updatedHeat.getProcessScrapEntryId() != null) {
            updateProcessScrapEntry(updatedHeat);
        }

        return furnaceHeatMapper.toResponse(updatedHeat);
    }

    private void handleHeatOrderItemDelta(FurnaceHeats existingHeat, List<HeatOrderItemRequest> newRequests) {
        if (newRequests == null) newRequests = new ArrayList<>();

        Map<Long, HeatOrderItem> existingMap = existingHeat.getHeatOrderItems().stream()
                .filter(i -> i.getId() != null)
                .collect(Collectors.toMap(HeatOrderItem::getId, i -> i));

        for (HeatOrderItemRequest req : newRequests) {
            if (req.getId() != null && existingMap.containsKey(req.getId())) {
                HeatOrderItem existing = existingMap.remove(req.getId());
                heatOrderItemMapper.updateEntity(req, existing);
            } else {
                HeatOrderItem newItem = heatOrderItemMapper.toEntity(req);
                if (req.getOrderItemId() != null) {
                    OrderItem orderItem = orderItemRepository.findById(req.getOrderItemId())
                            .orElseThrow(() -> new ResourceNotFoundException("OrderItem not found"));
                    newItem.setOrderItem(orderItem);
                }
                existingHeat.addHeatOrderItem(newItem);
            }
        }

        for (HeatOrderItem removed : existingMap.values()) {
            existingHeat.removeHeatOrderItem(removed);
        }
    }

    private void createProcessScrapEntry(FurnaceHeats heat) {
        BigDecimal totalScrapWeight = heat.getRunnerWeight()
                .add(heat.getRiserWeight())
                .add(heat.getSkullWeight())
                .add(heat.getSpillageWeight());

        if (totalScrapWeight.compareTo(BigDecimal.ZERO) <= 0) return;

        ScrapEntry scrapEntry = ScrapEntry.builder()
                .scrapNumber("SCR-H-" + heat.getId())
                .scrapDate(LocalDate.now())
                .scrapSource(ScrapSource.PROCESS_SCRAP)
                .heatId(heat.getId())
                .grade(heat.getGrade())
                .totalWeight(totalScrapWeight)
                .status(ScrapStatus.PENDING_VERIFICATION)
                .confidenceLevel(ConfidenceLevel.HIGH)
                .verificationMethod(VerificationMethod.AUTO_FROM_HEAT)
                .remarks("Auto-generated from Furnace Heat " + heat.getId())
                .build();

        // Add line items - Get or Create the scrap item automatically
        Item scrapItemEntity = getOrCreateScrapItem(heat.getGrade());
        
        addScrapItem(scrapEntry, "Runners", heat.getRunnerWeight(), scrapItemEntity);
        addScrapItem(scrapEntry, "Risers", heat.getRiserWeight(), scrapItemEntity);
        addScrapItem(scrapEntry, "Skull", heat.getSkullWeight(), scrapItemEntity);
        addScrapItem(scrapEntry, "Spillage", heat.getSpillageWeight(), scrapItemEntity);

        ScrapEntry savedEntry = scrapEntryRepository.save(scrapEntry);
        heat.setProcessScrapEntryId(savedEntry.getId());
    }

    private Item getOrCreateScrapItem(String grade) {
        return itemRepository.findByIsScrapTrueAndGrade(grade)
                .orElseGet(() -> {
                    Item newItem = Item.builder()
                            .name(grade + " Process Scrap")
                            .code("SCR-" + grade)
                            .category(ItemCategory.RAW_MATERIAL)
                            .subCategory(ItemSubCategory.FERROUS)
                            .department(getFurnaceDepartment())
                            .unit(ItemUnit.KG)
                            .reorderLevel(BigDecimal.ZERO)
                            .minStockLevel(BigDecimal.ZERO)
                            .currentStock(BigDecimal.ZERO)
                            .avgRate(BigDecimal.ZERO)
                            .isActive(true)
                            .isScrap(true)
                            .grade(grade)
                            .build();
                    return itemRepository.save(newItem);
                });
    }

    private void addScrapItem(ScrapEntry entry, String name, BigDecimal weight, Item item) {
        if (weight.compareTo(BigDecimal.ZERO) > 0) {
           ScrapItem.ScrapItemBuilder builder = ScrapItem.builder()
                    .itemName(name)
                    .weight(weight)
                    .grade(entry.getGrade())
                    .scrapType("PROCESS_SCRAP")
                    .recyclability("HIGH");
            
            if (item != null) {
                builder.itemId(item.getId())
                       .itemCode(item.getCode())
                       .inventoryItem(item);
            }
            
            entry.addScrapItem(builder.build());
        }
    }

    private void updateProcessScrapEntry(FurnaceHeats heat) {
        scrapEntryRepository.findById(heat.getProcessScrapEntryId()).ifPresent(entry -> {
            BigDecimal totalScrapWeight = heat.getRunnerWeight()
                    .add(heat.getRiserWeight())
                    .add(heat.getSkullWeight())
                    .add(heat.getSpillageWeight());
            
            entry.setTotalWeight(totalScrapWeight);
            entry.setGrade(heat.getGrade());
            
            // Simplified: Clear and re-add items
            entry.getScrapItems().clear();
            Item scrapItemEntity = getOrCreateScrapItem(heat.getGrade());
            
            addScrapItem(entry, "Runners", heat.getRunnerWeight(), scrapItemEntity);
            addScrapItem(entry, "Risers", heat.getRiserWeight(), scrapItemEntity);
            addScrapItem(entry, "Skull", heat.getSkullWeight(), scrapItemEntity);
            addScrapItem(entry, "Spillage", heat.getSpillageWeight(), scrapItemEntity);
            
            scrapEntryRepository.save(entry);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public HeatsByOrderResponse getHeatsByOrderId(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        List<FurnaceHeats> heats = furnaceHeatsRepository.findByOrderIdWithMaterials(orderId);

        List<FurnaceHeatResponse> heatResponses = heats.stream()
                .map(furnaceHeatMapper::toResponse)
                .toList();

        return new HeatsByOrderResponse(
                order.getId(),
                order.getOrderNumber(),
                heatResponses
        );

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
