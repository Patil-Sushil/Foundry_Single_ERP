package com.kalibyte.foundry.production.service.impl;

import com.kalibyte.foundry.common.exception.BusinessException;
import com.kalibyte.foundry.common.exception.ResourceNotFoundException;
import com.kalibyte.foundry.order.entity.Order;
import com.kalibyte.foundry.order.entity.OrderItem;
import com.kalibyte.foundry.order.repository.OrderRepository;
import com.kalibyte.foundry.production.dto.PipelineTotals;
import com.kalibyte.foundry.production.dto.request.ProductionEntryRequest;
import com.kalibyte.foundry.production.dto.request.ProductionItemRequest;
import com.kalibyte.foundry.production.dto.request.UpdateStatusRequest;
import com.kalibyte.foundry.production.dto.response.entry.ProductionEntryResponse;
import com.kalibyte.foundry.production.dto.response.entry.ProductionItemResponse;
import com.kalibyte.foundry.production.entity.ProductionEntry;
import com.kalibyte.foundry.production.entity.ProductionItem;
import com.kalibyte.foundry.production.mapper.ProductionMapper;
import com.kalibyte.foundry.production.repository.ProductionEntryRepository;
import com.kalibyte.foundry.production.repository.ProductionItemRepository;
import com.kalibyte.foundry.production.service.ProductionService;
import com.kalibyte.foundry.production.util.ProductionNumberGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductionServiceImpl implements ProductionService {

    private final ProductionEntryRepository entryRepo;
    private final ProductionItemRepository itemRepo;
    private final OrderRepository orderRepo;
    private final ProductionMapper mapper;
    private final ProductionNumberGenerator numberGenerator;

    //------------------------------------------------
    // CREATE ENTRY
    //------------------------------------------------

    @Override
    public ProductionEntryResponse createEntry(ProductionEntryRequest request) {

        // Fetch Order
        Order order = orderRepo.findWithDetailsById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        // Duplicate Check (Order + Date + Shift)
        if (entryRepo.existsByOrderIdAndReportDateAndShiftAndIsDeletedFalse(
                request.getOrderId(),
                request.getReportDate(),
                request.getShift()
        )) {
            throw new BusinessException("Production entry already exists for this date & shift");
        }

        // Create Entry
        ProductionEntry entry = ProductionEntry.builder()
                .entryNumber(numberGenerator.generate())
                .order(order)
                .reportDate(request.getReportDate())
                .shift(request.getShift())
                .operatorName(request.getOperatorName())
                .remarks(request.getRemarks())
                .build();

        List<ProductionItem> items = new ArrayList<>();

        //------------------------------------------------
        // PROCESS ITEMS
        //------------------------------------------------

        for (ProductionItemRequest itemReq : request.getItems()) {

            OrderItem orderItem = order.getItems().stream()
                    .filter(i -> i.getId().equals(itemReq.getOrderItemId()))
                    .findFirst()
                    .orElseThrow(() -> new BusinessException("Invalid order item"));

            //------------------------------------------------
            // PIPELINE VALIDATION
            //------------------------------------------------

            validatePipeline(itemReq, orderItem);

            //------------------------------------------------
            // CREATE ITEM
            //------------------------------------------------

            ProductionItem item = ProductionItem.builder()
                    .productionEntry(entry)
                    .orderItem(orderItem)
                    .itemName(orderItem.getProductName())
                    .patternNumber(
                            orderItem.getPattern() != null
                                    ? orderItem.getPattern().getPatternNumber()
                                    : null
                    )
                    .orderedQuantity(orderItem.getQuantity())
                    .readyCores(itemReq.getReadyCores())
                    .pouredMoulds(itemReq.getPouredMoulds())
                    .shotBlastingQuantity(itemReq.getShotBlastingQuantity())
                    .fettlingQuantity(itemReq.getFettlingQuantity())
                    .dispatchedQuantity(itemReq.getDispatchedQuantity())
                    .itemRemark(itemReq.getItemRemark())
                    .build();

            items.add(item);
        }

        entry.setProductionItems(items);

        //------------------------------------------------
        // CALCULATE TOTALS
        //------------------------------------------------

        calculateTotals(entry);

        //------------------------------------------------
        // SAVE
        //------------------------------------------------

        entryRepo.saveAndFlush(entry);

        //------------------------------------------------
        // RESPONSE BUILD
        //------------------------------------------------

        return buildResponse(entry);
    }

    //------------------------------------------------
    // GET BY ID
    //------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public ProductionEntryResponse getById(UUID id) {

        ProductionEntry entry = entryRepo.findWithItems(id)
                .orElseThrow(() -> new ResourceNotFoundException("Production entry not found"));

        return buildResponse(entry);
    }

    //------------------------------------------------
    // UPDATE STATUS
    //------------------------------------------------

    @Override
    public ProductionEntryResponse updateStatus(UUID id, UpdateStatusRequest request) {

        ProductionEntry entry = entryRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Production entry not found"));

        entry.setStatus(request.getStatus());

        return buildResponse(entry);
    }

    //------------------------------------------------
    // DELETE (SOFT DELETE)
    //------------------------------------------------

    @Override
    public void delete(UUID id) {

        ProductionEntry entry = entryRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Production entry not found"));

        entry.setIsDeleted(true);
    }

    //------------------------------------------------
    // PIPELINE VALIDATION
    //------------------------------------------------

    private void validatePipeline(ProductionItemRequest item, OrderItem orderItem) {

        int ordered = orderItem.getQuantity();
        UUID orderItemId = orderItem.getId();

        // Get already produced cumulative totals
        PipelineTotals cumulative = getCumulativeTotals(orderItemId);

        int alreadyCores = cumulative.getTotalReadyCores();
        int alreadyPoured = cumulative.getTotalPouredMoulds();
        int alreadyShot = cumulative.getTotalShotBlasting();
        int alreadyFettling = cumulative.getTotalFettling();
        int alreadyDispatch = cumulative.getTotalDispatched();

        // Check cumulative + today doesn't exceed ordered
        if (alreadyCores + item.getReadyCores() > ordered) {
            throw new BusinessException(
                    String.format("Ready cores would exceed ordered quantity. Ordered: %d, Already done: %d, Today: %d, Max allowed today: %d",
                            ordered, alreadyCores, item.getReadyCores(), ordered - alreadyCores)
            );
        }

        if (alreadyPoured + item.getPouredMoulds() > alreadyCores + item.getReadyCores()) {
            throw new BusinessException("Poured moulds cannot exceed total ready cores");
        }

        if (alreadyShot + item.getShotBlastingQuantity() > alreadyPoured + item.getPouredMoulds()) {
            throw new BusinessException("Shot blasting cannot exceed total poured moulds");
        }

        if (alreadyFettling + item.getFettlingQuantity() > alreadyShot + item.getShotBlastingQuantity()) {
            throw new BusinessException("Fettling cannot exceed total shot blasting");
        }

        if (alreadyDispatch + item.getDispatchedQuantity() > alreadyFettling + item.getFettlingQuantity()) {
            throw new BusinessException("Dispatched cannot exceed total fettling");
        }

        // Internal pipeline order (today's values)
        if (item.getPouredMoulds() > item.getReadyCores()) {
            throw new BusinessException("Today's poured cannot exceed today's cores");
        }

        if (item.getShotBlastingQuantity() > item.getPouredMoulds()) {
            throw new BusinessException("Today's shot blasting cannot exceed today's poured");
        }

        if (item.getFettlingQuantity() > item.getShotBlastingQuantity()) {
            throw new BusinessException("Today's fettling cannot exceed today's shot blasting");
        }

        if (item.getDispatchedQuantity() > item.getFettlingQuantity()) {
            throw new BusinessException("Today's dispatch cannot exceed today's fettling");
        }
    }

    //------------------------------------------------
    // TOTAL CALCULATION
    //------------------------------------------------

    private void calculateTotals(ProductionEntry entry) {

        int cores = 0, poured = 0, shot = 0, fettling = 0, dispatch = 0;

        for (ProductionItem item : entry.getProductionItems()) {
            cores += item.getReadyCores();
            poured += item.getPouredMoulds();
            shot += item.getShotBlastingQuantity();
            fettling += item.getFettlingQuantity();
            dispatch += item.getDispatchedQuantity();
        }

        entry.setTotalReadyCores(cores);
        entry.setTotalPouredMoulds(poured);
        entry.setTotalShotBlastingQuantity(shot);
        entry.setTotalFettlingQuantity(fettling);
        entry.setTotalDispatchedQuantity(dispatch);
    }

    //------------------------------------------------
    // GET CUMULATIVE TOTALS (HELPER)
    //------------------------------------------------

    private PipelineTotals getCumulativeTotals(UUID orderItemId) {

        Object[] totals = itemRepo.getPipelineTotals(orderItemId);

        int totalCores = 0;
        int totalPoured = 0;
        int totalShot = 0;
        int totalFettling = 0;
        int totalDispatch = 0;

        if (totals != null && totals.length >= 5) {
            totalCores = totals[0] != null ? ((Number) totals[0]).intValue() : 0;
            totalPoured = totals[1] != null ? ((Number) totals[1]).intValue() : 0;
            totalShot = totals[2] != null ? ((Number) totals[2]).intValue() : 0;
            totalFettling = totals[3] != null ? ((Number) totals[3]).intValue() : 0;
            totalDispatch = totals[4] != null ? ((Number) totals[4]).intValue() : 0;
        }

        return new PipelineTotals(totalCores, totalPoured, totalShot, totalFettling, totalDispatch);
    }

    //------------------------------------------------
    // RESPONSE BUILDER (CUMULATIVE)
    //------------------------------------------------

    private ProductionEntryResponse buildResponse(ProductionEntry entry) {

        List<ProductionItemResponse> itemResponses = new ArrayList<>();

        for (ProductionItem item : entry.getProductionItems()) {

            PipelineTotals totals = getCumulativeTotals(item.getOrderItem().getId());

            int totalCores = totals.getTotalReadyCores();
            int totalPoured = totals.getTotalPouredMoulds();
            int totalShot = totals.getTotalShotBlasting();
            int totalFettling = totals.getTotalFettling();
            int totalDispatch = totals.getTotalDispatched();

            int ordered = item.getOrderedQuantity();

            ProductionItemResponse res = ProductionItemResponse.builder()
                    .id(item.getId())
                    .orderItemId(item.getOrderItem().getId())
                    .itemName(item.getItemName())
                    .patternNumber(item.getPatternNumber())
                    .orderedQuantity(ordered)

                    // today
                    .readyCores(item.getReadyCores())
                    .pouredMoulds(item.getPouredMoulds())
                    .shotBlastingQuantity(item.getShotBlastingQuantity())
                    .fettlingQuantity(item.getFettlingQuantity())
                    .dispatchedQuantity(item.getDispatchedQuantity())

                    // cumulative
                    .totalReadyCores(totalCores)
                    .totalPouredMoulds(totalPoured)
                    .totalShotBlasting(totalShot)
                    .totalFettling(totalFettling)
                    .totalDispatched(totalDispatch)

                    // pending
                    .pendingDispatch(ordered - totalDispatch)
                    .pendingFettling(totalShot - totalFettling)
                    .pendingShotBlasting(totalPoured - totalShot)
                    .pendingPouring(totalCores - totalPoured)
                    .pendingCores(ordered - totalCores)

                    .itemRemark(item.getItemRemark())
                    .build();

            itemResponses.add(res);
        }

        return ProductionEntryResponse.builder()
                .id(entry.getId())
                .entryNumber(entry.getEntryNumber())
                .orderId(entry.getOrder().getId())
                .orderNumber(entry.getOrder().getOrderNumber())
                .reportDate(entry.getReportDate())
                .shift(entry.getShift())
                .status(entry.getStatus())
                .operatorName(entry.getOperatorName())
                .remarks(entry.getRemarks())
                .totalReadyCores(entry.getTotalReadyCores())
                .totalPouredMoulds(entry.getTotalPouredMoulds())
                .totalShotBlastingQuantity(entry.getTotalShotBlastingQuantity())
                .totalFettlingQuantity(entry.getTotalFettlingQuantity())
                .totalDispatchedQuantity(entry.getTotalDispatchedQuantity())
                .items(itemResponses)
                .build();
    }
}