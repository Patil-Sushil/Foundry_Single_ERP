package com.kalibyte.foundry.production.service.impl;

import com.kalibyte.foundry.common.exception.BusinessException;
import com.kalibyte.foundry.common.exception.ResourceNotFoundException;
import com.kalibyte.foundry.common.response.PageResponse;
import com.kalibyte.foundry.order.entity.Order;
import com.kalibyte.foundry.order.entity.OrderItem;
import com.kalibyte.foundry.order.entity.enums.OrderStatus;
import com.kalibyte.foundry.order.repository.OrderRepository;
import com.kalibyte.foundry.order.validation.OrderStatusTransitionValidator;
import com.kalibyte.foundry.pattern.entity.Pattern;
import com.kalibyte.foundry.pattern.repository.PatternRepository;
import com.kalibyte.foundry.production.dto.PipelineTotals;
import com.kalibyte.foundry.production.dto.request.ProductionEntryRequest;
import com.kalibyte.foundry.production.dto.request.ProductionItemRequest;
import com.kalibyte.foundry.production.dto.request.UpdateStatusRequest;
import com.kalibyte.foundry.production.dto.response.entry.ProductionEntryListItem;
import com.kalibyte.foundry.production.dto.response.entry.ProductionEntryResponse;
import com.kalibyte.foundry.production.dto.response.entry.ProductionItemResponse;
import com.kalibyte.foundry.production.entity.ProductionEntry;
import com.kalibyte.foundry.production.entity.ProductionItem;
import com.kalibyte.foundry.production.entity.enums.ProductionShift;
import com.kalibyte.foundry.production.entity.enums.ProductionStatus;
import com.kalibyte.foundry.production.repository.ProductionEntryRepository;
import com.kalibyte.foundry.production.repository.ProductionItemRepository;
import com.kalibyte.foundry.production.service.ProductionService;
import com.kalibyte.foundry.production.specification.ProductionSpecification;
import com.kalibyte.foundry.production.util.ProductionNumberGenerator;
import com.kalibyte.foundry.qa.inspection.service.QaInspectionService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

import static com.kalibyte.foundry.production.entity.enums.ProductionStatus.*;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ProductionServiceImpl implements ProductionService {

    private final ProductionEntryRepository entryRepo;
    private final ProductionItemRepository itemRepo;
    private final OrderRepository orderRepo;
    private final PatternRepository patternRepository;
    private final ProductionNumberGenerator numberGenerator;
    private final QaInspectionService qaInspectionService;

    @PersistenceContext
    private EntityManager entityManager;

    // ── Allowed status transitions ──────────────────
    private static final Map<ProductionStatus, Set<ProductionStatus>> STATUS_TRANSITIONS = Map.of(
            IN_PROGRESS, Set.of(COMPLETED, ON_HOLD, CANCELLED),
            ON_HOLD,     Set.of(IN_PROGRESS, CANCELLED),
            COMPLETED,   Set.of(),   // terminal — no further transitions
            CANCELLED,   Set.of()    // terminal — no further transitions
    );

    // ── Max days in past for report date ────────────
    private static final int MAX_BACKDATE_DAYS = 7;

    // ================================================================
    //  CREATE ENTRY
    // ================================================================

    @Override
    public ProductionEntryResponse createEntry(ProductionEntryRequest request) {

        // ── 1. Fetch & validate order ──
        Order order = orderRepo.findWithDetailsById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        validateOrderForProduction(order);
        validateReportDate(request.getReportDate());

        // ── 2. Check duplicate (application-level) ──
        if (entryRepo.existsByOrderIdAndReportDateAndShiftAndIsDeletedFalse(
                request.getOrderId(),
                request.getReportDate(),
                request.getShift()
        )) {
            throw new BusinessException(
                    "Production entry already exists for order " + order.getOrderNumber()
                            + " on " + request.getReportDate() + " (" + request.getShift() + " shift)"
            );
        }

        // ── 3. Build entry ──
        ProductionEntry entry = ProductionEntry.builder()
                .entryNumber(numberGenerator.generate())
                .order(order)
                .reportDate(request.getReportDate())
                .shift(request.getShift())
                .operatorName(request.getOperatorName())
                .remarks(request.getRemarks())
                .build();

        // ── 4. Process items ──
        List<ProductionItem> items = new ArrayList<>();
        List<PipelineTotals> preSaveTotals = new ArrayList<>();

        for (ProductionItemRequest itemReq : request.getItems()) {

            OrderItem orderItem = findOrderItem(order, itemReq.getOrderItemId());

            int cores    = safe(itemReq.getReadyCores());
            int poured   = safe(itemReq.getPouredMoulds());
            int shot     = safe(itemReq.getShotBlastingQuantity());
            int fettling = safe(itemReq.getFettlingQuantity());

            // validate at least one positive value
            validateAtLeastOnePositive(cores, poured, shot, fettling, orderItem.getPartName());

            // capture cumulative BEFORE this entry
            PipelineTotals beforeTotals = getCumulativeTotals(orderItem.getId());
            preSaveTotals.add(beforeTotals);

            // validate pipeline constraints
            validatePipeline(cores, poured, shot, fettling, orderItem, beforeTotals);

            // resolve pattern
            Pattern pattern = resolvePattern(itemReq.getPatternNumber());

            ProductionItem item = ProductionItem.builder()
                    .productionEntry(entry)
                    .orderItem(orderItem)
                    .itemName(orderItem.getPartName())
                    .pattern(pattern)
                    .orderedQuantity(orderItem.getQuantity())
                    .readyCores(cores)
                    .pouredMoulds(poured)
                    .shotBlastingQuantity(shot)
                    .fettlingQuantity(fettling)
                    .dispatchedQuantity(0) // Initialized to 0, ONLY QA can update this
                    .itemRemark(itemReq.getItemRemark())
                    .build();

            items.add(item);
        }

        entry.setProductionItems(items);
        calculateEntryTotals(entry);

        // ── 5. Save with race-condition handling ──
        try {
            entryRepo.saveAndFlush(entry);
        } catch (DataIntegrityViolationException e) {
            log.warn("Concurrent production entry creation detected for order={}, date={}, shift={}",
                    order.getOrderNumber(), request.getReportDate(), request.getShift());
            throw new BusinessException(
                    "Production entry already exists for this order, date & shift (concurrent creation detected)"
            );
        }

        // ── 5a. Auto-create QA Drafts for items that have fettling quantity ──
        for (ProductionItem item : entry.getProductionItems()) {
            if (item.getFettlingQuantity() > 0) {
                qaInspectionService.createDraftFromProduction(item);
            }
        }

        log.info("Created production entry {} for order {}", entry.getEntryNumber(), order.getOrderNumber());

        // ── 5b. Auto-update order status to IN_PRODUCTION ──
        autoUpdateOrderStatus(order);

        // ── 6. Build response using pre-computed totals ──
        return buildCreateResponse(entry, preSaveTotals);
    }


    // ================================================================
    //  GET BY ID
    // ================================================================

    @Override
    @Transactional(readOnly = true)
    public ProductionEntryResponse getById(UUID id) {

        ProductionEntry entry = entryRepo.findWithItems(id)
                .orElseThrow(() -> new ResourceNotFoundException("Production entry not found"));

        return buildFullResponse(entry);
    }


    // ================================================================
    //  LIST (PAGINATED + FILTERED)
    // ================================================================

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductionEntryListItem> list(
            UUID orderId,
            LocalDate fromDate,
            LocalDate toDate,
            ProductionStatus status,
            ProductionShift shift,
            int page,
            int size
    ) {
        Page<ProductionEntry> entries = entryRepo.findAll(
                ProductionSpecification.withFilters(orderId, fromDate, toDate, status, shift),
                PageRequest.of(page, size)
        );

        return PageResponse.from(entries, this::toListItem);
    }


    // ================================================================
    //  UPDATE STATUS
    // ================================================================

    @Override
    public ProductionEntryResponse updateStatus(UUID id, UpdateStatusRequest request) {

        ProductionEntry entry = entryRepo.findWithItems(id)
                .orElseThrow(() -> new ResourceNotFoundException("Production entry not found"));

        // validate transition
        validateStatusTransition(entry.getStatus(), request.getStatus());

        entry.setStatus(request.getStatus());
        entryRepo.saveAndFlush(entry);

        log.info("Updated production entry {} status: {} → {}",
                entry.getEntryNumber(), entry.getStatus(), request.getStatus());

        return buildFullResponse(entry);
    }


    // ================================================================
    //  UPDATE ENTRY (FULL EDIT)
    // ================================================================

    @Override
    public ProductionEntryResponse updateEntry(UUID id, ProductionEntryRequest request) {

        ProductionEntry entry = entryRepo.findWithItems(id)
                .orElseThrow(() -> new ResourceNotFoundException("Production entry not found"));

        // ── only allow editing IN_PROGRESS entries ──
        if (entry.getStatus() != IN_PROGRESS) {
            throw new BusinessException(
                    "Cannot edit entry with status: " + entry.getStatus()
                            + ". Only IN_PROGRESS entries can be modified."
            );
        }

        validateReportDate(request.getReportDate());

        // ── Re-fetch order with details (entry.getOrder() is lazy) ──
        Order order = orderRepo.findWithDetailsById(entry.getOrder().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        // ── Map existing items by orderItem.id for updates ──
        Map<UUID, ProductionItem> existingItemsMap = new HashMap<>();
        entry.getProductionItems().forEach(i -> existingItemsMap.put(i.getOrderItem().getId(), i));

        // ── Track which items are updated/kept ──
        Set<UUID> updatedOrderItemIds = new HashSet<>();

        // ── Check if date/shift changed and new combo already exists ──
        boolean dateOrShiftChanged = !entry.getReportDate().equals(request.getReportDate())
                || entry.getShift() != request.getShift();

        if (dateOrShiftChanged
                && entryRepo.existsByOrderIdAndReportDateAndShiftAndIsDeletedFalse(
                order.getId(), request.getReportDate(), request.getShift()
        )) {
            throw new BusinessException(
                    "Another production entry already exists for order " + order.getOrderNumber()
                            + " on " + request.getReportDate() + " (" + request.getShift() + " shift)"
            );
        }

        // ── Process request items ──
        List<PipelineTotals> preSaveTotals = new ArrayList<>();
        List<ProductionItem> updatedItemsList = new ArrayList<>();

        for (ProductionItemRequest itemReq : request.getItems()) {
            OrderItem orderItem = findOrderItem(order, itemReq.getOrderItemId());
            UUID orderItemId = orderItem.getId();
            updatedOrderItemIds.add(orderItemId);

            int cores    = safe(itemReq.getReadyCores());
            int poured   = safe(itemReq.getPouredMoulds());
            int shot     = safe(itemReq.getShotBlastingQuantity());
            int fettling = safe(itemReq.getFettlingQuantity());

            validateAtLeastOnePositive(cores, poured, shot, fettling, orderItem.getPartName());

            // cumulative EXCLUDING this entry's values (for validation)
            PipelineTotals beforeTotals = getCumulativeTotalsExcluding(orderItemId, entry.getId());
            preSaveTotals.add(beforeTotals);

            validatePipeline(cores, poured, shot, fettling, orderItem, beforeTotals);

            Pattern pattern = resolvePattern(itemReq.getPatternNumber());

            ProductionItem item = existingItemsMap.get(orderItemId);
            if (item != null) {
                // Update existing entity to preserve ID
                item.setPattern(pattern);
                item.setReadyCores(cores);
                item.setPouredMoulds(poured);
                item.setShotBlastingQuantity(shot);
                item.setFettlingQuantity(fettling);
                item.setItemRemark(itemReq.getItemRemark());
                // Note: dispatchedQuantity and other QA fields are already on this entity
            } else {
                // Create new item for this entry
                item = ProductionItem.builder()
                        .productionEntry(entry)
                        .orderItem(orderItem)
                        .itemName(orderItem.getPartName())
                        .pattern(pattern)
                        .orderedQuantity(orderItem.getQuantity())
                        .readyCores(cores)
                        .pouredMoulds(poured)
                        .shotBlastingQuantity(shot)
                        .fettlingQuantity(fettling)
                        .dispatchedQuantity(0)
                        .itemRemark(itemReq.getItemRemark())
                        .build();
            }
            updatedItemsList.add(item);
        }

        // ── Remove items no longer in request ──
        entry.getProductionItems().removeIf(item -> !updatedOrderItemIds.contains(item.getOrderItem().getId()));
        
        // ── Add new items ──
        for (ProductionItem item : updatedItemsList) {
            if (!entry.getProductionItems().contains(item)) {
                entry.getProductionItems().add(item);
            }
        }

        // ── Update metadata ──
        entry.setReportDate(request.getReportDate());
        entry.setShift(request.getShift());
        entry.setOperatorName(request.getOperatorName());
        entry.setRemarks(request.getRemarks());

        calculateEntryTotals(entry);

        try {
            entryRepo.saveAndFlush(entry);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException("Failed to update — possible duplicate date/shift combination");
        }

        log.info("Updated production entry {}", entry.getEntryNumber());

        // ── Auto-update order status to IN_PRODUCTION ──
        autoUpdateOrderStatus(order);

        return buildCreateResponse(entry, preSaveTotals);
    }


    // ================================================================
    //  DELETE (SOFT)
    // ================================================================

    @Override
    public void delete(UUID id) {

        ProductionEntry entry = entryRepo.findWithItems(id)
                .orElseThrow(() -> new ResourceNotFoundException("Production entry not found"));

        entry.setIsDeleted(true);
        entry.getProductionItems().forEach(item -> item.setIsDeleted(true));

        log.info("Soft-deleted production entry {} with {} items",
                entry.getEntryNumber(), entry.getProductionItems().size());
    }


    // ================================================================
    //  PRIVATE — ORDER VALIDATION
    // ================================================================

    private void validateOrderForProduction(Order order) {
        String status = order.getStatus().name();

        Set<String> blockedStatuses = Set.of("CANCELLED", "COMPLETED", "REJECTED");

        if (blockedStatuses.contains(status)) {
            throw new BusinessException(
                    "Cannot create production entry for " + status + " order: " + order.getOrderNumber()
            );
        }
        
        // Ensure status is valid for production (CREATED, CONFIRMED, or already IN_PRODUCTION)
        if (!status.equals("CREATED") && !status.equals("CONFIRMED") && !status.equals("IN_PRODUCTION") &&
            !status.equals("PARTIALLY_PRODUCED") && !status.equals("ON_HOLD")) {
             log.warn("Order {} in status {} is receiving production entry", order.getOrderNumber(), status);
        }
    }


    // ================================================================
    //  PRIVATE — DATE VALIDATION
    // ================================================================

    private void validateReportDate(LocalDate reportDate) {
        LocalDate today = LocalDate.now();

        if (reportDate.isAfter(today)) {
            throw new BusinessException("Report date cannot be in the future");
        }

        LocalDate maxPast = today.minusDays(MAX_BACKDATE_DAYS);
        if (reportDate.isBefore(maxPast)) {
            throw new BusinessException(
                    "Report date cannot be older than " + MAX_BACKDATE_DAYS + " days. "
                            + "Earliest allowed: " + maxPast
            );
        }
    }


    // ================================================================
    //  PRIVATE — AT LEAST ONE POSITIVE VALUE
    // ================================================================

    private void validateAtLeastOnePositive(
            int cores, int poured, int shot, int fettling,
            String itemName
    ) {
        if (cores == 0 && poured == 0 && shot == 0 && fettling == 0) {
            throw new BusinessException(
                    "At least one production quantity must be greater than zero for item: " + itemName
            );
        }
    }


    // ================================================================
    //  PRIVATE — STATUS TRANSITION VALIDATION
    // ================================================================

    private void validateStatusTransition(ProductionStatus from, ProductionStatus to) {

        if (from == to) {
            throw new BusinessException("Entry is already in " + from + " status");
        }

        Set<ProductionStatus> allowed = STATUS_TRANSITIONS.getOrDefault(from, Set.of());

        if (!allowed.contains(to)) {
            throw new BusinessException(String.format(
                    "Cannot transition from %s to %s. Allowed transitions: %s",
                    from, to, allowed.isEmpty() ? "none (terminal state)" : allowed
            ));
        }
    }


    // ================================================================
    //  PRIVATE — PIPELINE VALIDATION
    // ================================================================

    private void validatePipeline(
            int cores, int poured, int shot, int fettling,
            OrderItem orderItem, PipelineTotals cumulative
    ) {
        int ordered = orderItem.getQuantity();
        String itemName = orderItem.getPartName();

        int cumCores    = cumulative.totalReadyCores();
        int cumPoured   = cumulative.totalPouredMoulds();
        int cumShot     = cumulative.totalShotBlasting();
        int cumFettling = cumulative.totalFettling();
        int totalRejected = cumulative.totalRejected();

        int allowedQuantity = ordered + totalRejected;

        // ── Cumulative + today must not exceed allowed quantity (ordered + rejected) ──
        if (cumCores + cores > allowedQuantity) {
            throw new BusinessException(String.format(
                    "[%s] Ready cores would exceed allowed qty (Ordered: %d + Rejected: %d = %d). Already: %d, Today: %d, Max today: %d",
                    itemName, ordered, totalRejected, allowedQuantity, cumCores, cores, allowedQuantity - cumCores
            ));
        }

        // ── Pipeline flow: each stage cannot exceed previous stage (cumulative) ──
        if (cumPoured + poured > cumCores + cores) {
            throw new BusinessException(String.format(
                    "[%s] Cumulative poured moulds (%d + %d = %d) cannot exceed cumulative ready cores (%d + %d = %d)",
                    itemName, cumPoured, poured, cumPoured + poured, cumCores, cores, cumCores + cores
            ));
        }

        if (cumShot + shot > cumPoured + poured) {
            throw new BusinessException(String.format(
                    "[%s] Cumulative shot blasting (%d + %d = %d) cannot exceed cumulative poured moulds (%d + %d = %d)",
                    itemName, cumShot, shot, cumShot + shot, cumPoured, poured, cumPoured + poured
            ));
        }

        if (cumFettling + fettling > cumShot + shot) {
            throw new BusinessException(String.format(
                    "[%s] Cumulative fettling (%d + %d = %d) cannot exceed cumulative shot blasting (%d + %d = %d)",
                    itemName, cumFettling, fettling, cumFettling + fettling, cumShot, shot, cumShot + shot
            ));
        }

        // ── Today's per-row constraint (matches DB constraint) ──
        if (poured > cores) {
            throw new BusinessException(String.format(
                    "[%s] Today's poured moulds (%d) cannot exceed today's ready cores (%d)",
                    itemName, poured, cores
            ));
        }
    }


    // ================================================================
    //  PRIVATE — CUMULATIVE TOTALS
    // ================================================================

    private PipelineTotals getCumulativeTotals(UUID orderItemId) {
        List<Object[]> results = itemRepo.getPipelineTotalsRaw(orderItemId);
        return extractTotals(results);
    }

    private PipelineTotals getCumulativeTotalsExcluding(UUID orderItemId, UUID excludeEntryId) {
        List<Object[]> results = itemRepo.getPipelineTotalsExcluding(orderItemId, excludeEntryId);
        return extractTotals(results);
    }

    private PipelineTotals extractTotals(List<Object[]> results) {
        if (results == null || results.isEmpty()) {
            return PipelineTotals.ZERO;
        }
        Object[] raw = results.get(0);
        if (raw == null || raw.length < 6) {
            return PipelineTotals.ZERO;
        }
        return new PipelineTotals(
                toInt(raw[0]),
                toInt(raw[1]),
                toInt(raw[2]),
                toInt(raw[3]),
                toInt(raw[4]),
                toInt(raw[5])
        );
    }


    // ================================================================
    //  PRIVATE — UTILITY HELPERS
    // ================================================================

    private OrderItem findOrderItem(Order order, UUID orderItemId) {
        return order.getItems().stream()
                .filter(i -> i.getId().equals(orderItemId))
                .findFirst()
                .orElseThrow(() -> new BusinessException(
                        "Order item " + orderItemId + " not found in order " + order.getOrderNumber()
                ));
    }

    private void calculateEntryTotals(ProductionEntry entry) {
        int cores = 0, poured = 0, shot = 0, fettling = 0, dispatch = 0;

        for (ProductionItem item : entry.getProductionItems()) {
            cores    += item.getReadyCores();
            poured   += item.getPouredMoulds();
            shot     += item.getShotBlastingQuantity();
            fettling += item.getFettlingQuantity();
            dispatch += item.getDispatchedQuantity();
        }

        entry.setTotalReadyCores(cores);
        entry.setTotalPouredMoulds(poured);
        entry.setTotalShotBlastingQuantity(shot);
        entry.setTotalFettlingQuantity(fettling);
        entry.setTotalDispatchedQuantity(dispatch);
    }

    private Pattern resolvePattern(String patternNumber) {
        if (patternNumber == null || patternNumber.isBlank()) {
            return null;
        }
        return patternRepository.findByPatternNumber(patternNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Pattern not found: " + patternNumber));
    }

    private int safe(Integer value) {
        return value != null ? value : 0;
    }

    private int toInt(Object value) {
        return value != null ? ((Number) value).intValue() : 0;
    }

    private void autoUpdateOrderStatus(Order order) {
        OrderStatus current = order.getStatus();
        // If it's already in a production or post-production status, don't revert or change
        if (current == OrderStatus.IN_PRODUCTION || 
            current == OrderStatus.PARTIALLY_PRODUCED || 
            current == OrderStatus.PRODUCED ||
            current == OrderStatus.PARTIALLY_DISPATCHED ||
            current == OrderStatus.DISPATCHED ||
            current == OrderStatus.COMPLETED) {
            return;
        }

        try {
            OrderStatusTransitionValidator.validate(current, OrderStatus.IN_PRODUCTION);
            order.setStatus(OrderStatus.IN_PRODUCTION);
            orderRepo.save(order);
            log.info("Auto-transitioned order {} status to IN_PRODUCTION", order.getOrderNumber());
        } catch (Exception e) {
            log.warn("Could not auto-transition order {} status: {}", order.getOrderNumber(), e.getMessage());
        }
    }


    // ================================================================
    //  RESPONSE BUILDER — CREATE
    //  Uses pre-computed cumulative totals (before + today).
    //  No DB re-query needed — avoids flush/timing issues.
    // ================================================================

    private ProductionEntryResponse buildCreateResponse(
            ProductionEntry entry,
            List<PipelineTotals> preSaveTotals
    ) {
        List<ProductionItemResponse> itemResponses = new ArrayList<>();
        List<ProductionItem> items = entry.getProductionItems();

        for (int i = 0; i < items.size(); i++) {

            ProductionItem item = items.get(i);
            PipelineTotals before = preSaveTotals.get(i);
            int ordered = item.getOrderedQuantity();

            // cumulative = what was before + what we just added
            int cumCores    = before.totalReadyCores()    + item.getReadyCores();
            int cumPoured   = before.totalPouredMoulds()  + item.getPouredMoulds();
            int cumShot     = before.totalShotBlasting()  + item.getShotBlastingQuantity();
            int cumFettling = before.totalFettling()      + item.getFettlingQuantity();
            int cumDispatch = before.totalDispatched()    + item.getDispatchedQuantity();
            int cumRejected = before.totalRejected()      + item.getRejectedQuantity();

            itemResponses.add(buildItemResponse(item, ordered,
                    cumCores, cumPoured, cumShot, cumFettling, cumDispatch, cumRejected));
        }

        return buildEntryResponse(entry, itemResponses);
    }


    // ================================================================
    //  RESPONSE BUILDER — READ
    //  Queries DB for cumulative totals.
    //  Used by getById, updateStatus.
    // ================================================================

    private ProductionEntryResponse buildFullResponse(ProductionEntry entry) {

        List<ProductionItemResponse> itemResponses = new ArrayList<>();

        for (ProductionItem item : entry.getProductionItems()) {

            PipelineTotals totals = getCumulativeTotals(item.getOrderItem().getId());
            int ordered = item.getOrderedQuantity();

            itemResponses.add(buildItemResponse(item, ordered,
                    totals.totalReadyCores(),
                    totals.totalPouredMoulds(),
                    totals.totalShotBlasting(),
                    totals.totalFettling(),
                    totals.totalDispatched(),
                    totals.totalRejected()));
        }

        return buildEntryResponse(entry, itemResponses);
    }


    // ================================================================
    //  RESPONSE BUILDER — SHARED ITEM BUILDER
    //  Eliminates duplicate code between create & read responses.
    // ================================================================

    private ProductionItemResponse buildItemResponse(
            ProductionItem item, int ordered,
            int cumCores, int cumPoured, int cumShot, int cumFettling, int cumDispatch, int cumRejected
    ) {
        return ProductionItemResponse.builder()
                .id(item.getId())
                .orderItemId(item.getOrderItem().getId())
                .itemName(item.getItemName())
                .patternNumber(item.getPattern() != null
                        ? item.getPattern().getPatternNumber() : null)
                .orderedQuantity(ordered)
                // ── today's values ──
                .readyCores(item.getReadyCores())
                .pouredMoulds(item.getPouredMoulds())
                .shotBlastingQuantity(item.getShotBlastingQuantity())
                .fettlingQuantity(item.getFettlingQuantity())
                .dispatchedQuantity(item.getDispatchedQuantity())
                // ── cumulative totals ──
                .totalReadyCores(cumCores)
                .totalPouredMoulds(cumPoured)
                .totalShotBlasting(cumShot)
                .totalFettling(cumFettling)
                .totalDispatched(cumDispatch)
                .totalRejected(cumRejected)
                // ── pending (ordered minus completed at each stage) ──
                .pendingCores(ordered - cumCores)
                .pendingPouring(cumCores - cumPoured)
                .pendingShotBlasting(cumPoured - cumShot)
                .pendingFettling(cumShot - cumFettling)
                .pendingDispatch(ordered - cumDispatch)
                // ── remark ──
                .itemRemark(item.getItemRemark())
                .build();
    }


    // ================================================================
    //  RESPONSE BUILDER — SHARED ENTRY BUILDER
    // ================================================================

    private ProductionEntryResponse buildEntryResponse(
            ProductionEntry entry,
            List<ProductionItemResponse> itemResponses
    ) {
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
                .totalInspectedQuantity(entry.getTotalInspectedQuantity())
                .totalAcceptedQuantity(entry.getTotalAcceptedQuantity())
                .totalRejectedQuantity(entry.getTotalRejectedQuantity())
                .totalReworkQuantity(entry.getTotalReworkQuantity())
                .items(itemResponses)
                .build();
    }


    // ================================================================
    //  LIST ITEM MAPPER
    // ================================================================

    private ProductionEntryListItem toListItem(ProductionEntry entry) {
        return new ProductionEntryListItem(
                entry.getId(),
                entry.getEntryNumber(),
                entry.getOrder().getId(),
                entry.getOrder().getOrderNumber(),
                entry.getOrder().getCustomer().getName(),
                entry.getReportDate(),
                entry.getShift(),
                entry.getStatus(),
                entry.getOperatorName(),
                entry.getTotalReadyCores(),
                entry.getTotalPouredMoulds(),
                entry.getTotalShotBlastingQuantity(),
                entry.getTotalFettlingQuantity(),
                entry.getTotalDispatchedQuantity(),
                entry.getTotalInspectedQuantity(),
                entry.getTotalAcceptedQuantity(),
                entry.getTotalRejectedQuantity(),
                entry.getTotalReworkQuantity(),
                entry.getCreatedAt()
        );
    }
}