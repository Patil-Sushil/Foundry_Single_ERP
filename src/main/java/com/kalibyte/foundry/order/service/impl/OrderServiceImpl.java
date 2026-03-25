package com.kalibyte.foundry.order.service.impl;

import com.kalibyte.foundry.common.email.EmailService;
import com.kalibyte.foundry.common.exception.ResourceNotFoundException;
import com.kalibyte.foundry.common.response.PageResponse;
import com.kalibyte.foundry.customer.entity.Customer;
import com.kalibyte.foundry.customer.repository.CustomerRepository;
import com.kalibyte.foundry.enquiry.entity.Enquiry;
import com.kalibyte.foundry.enquiry.entity.EnquiryItem;
import com.kalibyte.foundry.order.dto.request.OrderCreateRequest;
import com.kalibyte.foundry.order.dto.request.OrderItemRequest;
import com.kalibyte.foundry.order.dto.response.OrderResponse;
import com.kalibyte.foundry.order.entity.Order;
import com.kalibyte.foundry.order.entity.OrderItem;
import com.kalibyte.foundry.order.entity.enums.OrderStatus;
import com.kalibyte.foundry.order.entity.enums.OrderType;
import com.kalibyte.foundry.order.mapper.OrderMapper;
import com.kalibyte.foundry.order.repository.OrderRepository;
import com.kalibyte.foundry.order.service.OrderService;
import com.kalibyte.foundry.order.specification.OrderSpecification;
import com.kalibyte.foundry.order.validation.OrderStatusTransitionValidator;
import com.kalibyte.foundry.pattern.dto.request.PatternReceiptRequest;
import com.kalibyte.foundry.pattern.entity.Pattern;
import com.kalibyte.foundry.pattern.entity.PatternReceipt;
import com.kalibyte.foundry.pattern.repository.PatternRepository;
import com.kalibyte.foundry.quotation.entity.Quotation;
import com.kalibyte.foundry.quotation.entity.QuotationItem;
import com.kalibyte.foundry.quotation.entity.enums.QuotationStatus;
import com.kalibyte.foundry.quotation.repository.QuotationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final QuotationRepository quotationRepository;
    private final CustomerRepository customerRepository;
    private final PatternRepository patternRepository;
    private final OrderMapper orderMapper;
    private final EmailService emailService;

    // =========================================================
    //  CREATE ORDER (ENTRY POINT)
    // =========================================================

    @Override
    public OrderResponse createOrder(OrderCreateRequest request) {

        // Scenario 1: From Quotation
        if (request.getQuotationId() != null) {
            return createFromQuotation(request);
        }

        // Scenario 2: Direct Order
        if (request.getCustomerId() != null) {
            return createDirectOrder(request);
        }

        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Either quotationId or customerId must be provided");
    }

    // =========================================================
    //  SCENARIO 1: CREATE FROM QUOTATION
    //  Auto-populates all data from Quotation + Enquiry
    // =========================================================

    private OrderResponse createFromQuotation(OrderCreateRequest request) {

        // 1. Fetch quotation
        Quotation quotation = quotationRepository.findById(request.getQuotationId())
                .orElseThrow(() -> new ResourceNotFoundException("Quotation not found"));

        // 2. Validate quotation status
        if (!QuotationStatus.APPROVED.equals(quotation.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Quotation must be APPROVED. Current status: " + quotation.getStatus());
        }

        // 3. Check duplicate
        if (orderRepository.existsByQuotationId(request.getQuotationId())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Order already exists for this quotation");
        }

        // 4. Get enquiry if linked (for extra data)
        Enquiry enquiry = quotation.getEnquiry();

        // 5. Build order
        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .customer(quotation.getCustomer())
                .quotation(quotation)
                .orderType(OrderType.QUOTATION)
                .orderDate(LocalDate.now())
                .deliveryDate(request.getDeliveryDate())
                .status(OrderStatus.CREATED)
                .placeOfSupply(request.getPlaceOfSupply() != null ?
                        request.getPlaceOfSupply() : quotation.getDeliveryLocation())
                .poReference(request.getPoReference())
                .subTotal(BigDecimal.ZERO)
                .discount(BigDecimal.ZERO)
                .tax(BigDecimal.ZERO)
                .totalAmount(BigDecimal.ZERO)
                .items(new ArrayList<>())
                .build();

        // 6. Process items from quotation
        List<QuotationItem> quotationItems = quotation.getItems();
        List<EnquiryItem> enquiryItems = (enquiry != null) ? enquiry.getEnquiryItems() : null;

        for (int i = 0; i < quotationItems.size(); i++) {

            QuotationItem qItem = quotationItems.get(i);

            // Get corresponding enquiry item if available
            EnquiryItem eItem = null;
            if (enquiryItems != null && i < enquiryItems.size()) {
                eItem = enquiryItems.get(i);
            }

            OrderItem orderItem = buildOrderItemFromQuotation(order, qItem, eItem);
            order.addItem(orderItem);
        }

        // 7. Calculate totals
        recalculateTotals(order);

        // 8. Save
        Order saved = orderRepository.save(order);

        log.info("Order created from quotation: {} -> Order: {}",
                quotation.getQuotationNumber(), saved.getOrderNumber());

        // 9. Send confirmation email
        sendOrderConfirmationEmail(saved);

        return orderMapper.toResponse(saved);
    }

    // =========================================================
    //  BUILD ORDER ITEM FROM QUOTATION ITEM
    // =========================================================

    private OrderItem buildOrderItemFromQuotation(Order order,
                                                  QuotationItem qItem,
                                                  EnquiryItem eItem) {

        OrderItem item = OrderItem.builder()
                .order(order)
                // Autopopulated from Quotation
                .partName(qItem.getPartName())
                .drawingNumber(qItem.getDrawingNumber())
                .materialGrade(qItem.getMaterialGrade())
                .metalType(qItem.getMetalType())
                .castingProcess(qItem.getCastingProcess())
                .netWeightKg(qItem.getNetWeightKg())
                .grossWeightKg(qItem.getGrossWeightKg())
                .quantity(qItem.getQuantity())
                .unitPrice(qItem.getUnitPrice())
                .lineTotal(qItem.getLineTotal())
                .patternProvidedByCustomer(qItem.getPatternProvidedByCustomer())
                // Production tracking defaults
                .producedQuantity(0)
                .dispatchedQuantity(0)
                .build();

        // Fill missing data from Enquiry if available
        if (eItem != null) {

            if (item.getMaterialGrade() == null || item.getMaterialGrade().isBlank()) {
                item.setMaterialGrade(eItem.getMaterialGrade());
                log.debug("MaterialGrade auto-filled from enquiry: {}", eItem.getMaterialGrade());
            }

            if (item.getMetalType() == null) {
                item.setMetalType(eItem.getMetalType());
                log.debug("MetalType auto-filled from enquiry: {}", eItem.getMetalType());
            }

            if (item.getCastingProcess() == null || item.getCastingProcess().isBlank()) {
                item.setCastingProcess(eItem.getCastingProcess());
                log.debug("CastingProcess auto-filled from enquiry: {}", eItem.getCastingProcess());
            }
        }

        // Copy pattern info from quotation
        if (Boolean.TRUE.equals(qItem.getPatternProvidedByCustomer())) {
            item.setPatternReceipt(qItem.getPatternReceipt());
        } else {
            item.setPattern(qItem.getPattern());
        }

        return item;
    }

    // =========================================================
    //  SCENARIO 2: CREATE DIRECT ORDER
    //  All fields required from request
    // =========================================================

    private OrderResponse createDirectOrder(OrderCreateRequest request) {

        // 1. Fetch customer
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        // 2. Validate items
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Items are required for direct order");
        }

        // 3. Build order
        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .customer(customer)
                .orderType(OrderType.DIRECT)
                .orderDate(LocalDate.now())
                .deliveryDate(request.getDeliveryDate())
                .placeOfSupply(request.getPlaceOfSupply())
                .poReference(request.getPoReference())
                .status(OrderStatus.CREATED)
                .subTotal(BigDecimal.ZERO)
                .discount(BigDecimal.ZERO)
                .tax(BigDecimal.ZERO)
                .totalAmount(BigDecimal.ZERO)
                .items(new ArrayList<>())
                .build();

        // 4. Process items from request
        for (OrderItemRequest itemReq : request.getItems()) {
            OrderItem item = buildOrderItemFromRequest(order, itemReq);
            order.addItem(item);
        }

        // 5. Calculate totals
        recalculateTotals(order);

        // 6. Save
        Order saved = orderRepository.save(order);

        log.info("Direct order created: {}", saved.getOrderNumber());

        // 7. Send confirmation email
        sendOrderConfirmationEmail(saved);

        return orderMapper.toResponse(saved);
    }

    // =========================================================
    //  BUILD ORDER ITEM FROM REQUEST (DIRECT ORDER)
    // =========================================================

    private OrderItem buildOrderItemFromRequest(Order order, OrderItemRequest req) {

        // Validate
        validateOrderItemRequest(req);

        // Calculate line total
        BigDecimal lineTotal = req.getNetWeightKg()
                .multiply(req.getUnitPrice())
                .multiply(BigDecimal.valueOf(req.getQuantity()));

        OrderItem item = OrderItem.builder()
                .order(order)
                .partName(req.getPartName())
                .materialGrade(req.getMaterialGrade())
                .metalType(req.getMetalType())
                .castingProcess(req.getCastingProcess())
                .netWeightKg(req.getNetWeightKg())
                .grossWeightKg(req.getGrossWeightKg())
                .quantity(req.getQuantity())
                .unitPrice(req.getUnitPrice())
                .lineTotal(lineTotal)
                .patternProvidedByCustomer(req.getPatternProvidedByCustomer())
                .producedQuantity(0)
                .dispatchedQuantity(0)
                .build();

        // Handle pattern
        applyPatternLogic(item, req);

        return item;
    }

    // =========================================================
    //  PATTERN LOGIC
    // =========================================================

    private void applyPatternLogic(OrderItem item, OrderItemRequest req) {

        // Customer provides pattern
        if (Boolean.TRUE.equals(req.getPatternProvidedByCustomer())) {

            if (req.getPatternReceipt() == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Pattern receipt is required when pattern is provided by customer");
            }

            PatternReceiptRequest pr = req.getPatternReceipt();

            PatternReceipt receipt = PatternReceipt.builder()
                    .name(pr.getName())
                    .type(pr.getType())
                    .material(pr.getMaterial())
                    .inwardDate(pr.getInwardDate())
                    .outwardDate(pr.getOutwardDate())
                    .build();

            item.setPatternReceipt(receipt);

            // Company provides pattern
        } else {

            if (req.getPatternId() == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Pattern ID is required when using company pattern");
            }

            Pattern pattern = patternRepository.findById(req.getPatternId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Pattern not found: " + req.getPatternId()));

            item.setPattern(pattern);
        }
    }

    // =========================================================
    //  VALIDATION
    // =========================================================

    private void validateOrderItemRequest(OrderItemRequest req) {

        List<String> errors = new ArrayList<>();

        if (req.getPartName() == null || req.getPartName().isBlank()) {
            errors.add("Part name is required");
        }
        if (req.getNetWeightKg() == null) {
            errors.add("Net weight is required");
        }
        if (req.getQuantity() == null || req.getQuantity() <= 0) {
            errors.add("Quantity must be greater than 0");
        }
        if (req.getUnitPrice() == null) {
            errors.add("Unit price is required");
        }
        if (req.getPatternProvidedByCustomer() == null) {
            errors.add("Pattern source (patternProvidedByCustomer) is required");
        }

        if (!errors.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Validation failed: " + String.join(", ", errors));
        }
    }

    // =========================================================
    //  RECALCULATE TOTALS
    // =========================================================

    private void recalculateTotals(Order order) {

        BigDecimal subTotal = order.getItems().stream()
                .map(OrderItem::getLineTotal)
                .filter(lt -> lt != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setSubTotal(subTotal);

        BigDecimal discount = order.getDiscount() != null ? order.getDiscount() : BigDecimal.ZERO;
        BigDecimal tax = order.getTax() != null ? order.getTax() : BigDecimal.ZERO;

        order.setTotalAmount(subTotal.subtract(discount).add(tax));
    }

    // =========================================================
    //  GET BY ID
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getById(UUID id) {

        Order order = orderRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        return orderMapper.toResponse(order);
    }

    // =========================================================
    //  GET ALL WITH FILTERS
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> getAll(
            OrderStatus status,
            UUID customerId,
            LocalDate from,
            LocalDate to,
            Pageable pageable) {

        Specification<Order> spec = OrderSpecification.filter(status, customerId, from, to);
        Page<Order> page = orderRepository.findAll(spec, pageable);

        return PageResponse.of(page.map(orderMapper::toResponse));
    }

    // =========================================================
    //  GET PENDING ORDERS
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> getPendingOrders(Pageable pageable) {

        List<OrderStatus> pendingStatuses = List.of(
                OrderStatus.CREATED,
                OrderStatus.CONFIRMED,
                OrderStatus.IN_PRODUCTION,
                OrderStatus.PARTIALLY_PRODUCED,
                OrderStatus.PRODUCED,
                OrderStatus.PARTIALLY_DISPATCHED
        );

        Page<Order> page = orderRepository.findByStatusIn(pendingStatuses, pageable);

        return PageResponse.of(page.map(orderMapper::toResponse));
    }

    // =========================================================
    //  UPDATE STATUS
    // =========================================================

    @Override
    public void updateStatus(UUID id, OrderStatus newStatus) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        // Validate transition
        OrderStatusTransitionValidator.validate(order.getStatus(), newStatus);

        OrderStatus oldStatus = order.getStatus();
        order.setStatus(newStatus);

        log.info("Order {} status changed: {} -> {}",
                order.getOrderNumber(), oldStatus, newStatus);
    }

    // =========================================================
    //  EMAIL
    // =========================================================

    private void sendOrderConfirmationEmail(Order order) {
        try {
            Customer customer = order.getCustomer();

            if (customer.getEmail() == null || customer.getEmail().isBlank()) {
                log.warn("Cannot send email - customer {} has no email", customer.getName());
                return;
            }

            String subject = "Order Confirmation - " + order.getOrderNumber();

            String body = String.format("""
                    Dear %s,
                    
                    Your order has been created successfully.
                    
                    Order Number  : %s
                    Order Type    : %s
                    Order Date    : %s
                    Delivery Date : %s
                    Total Amount  : ₹%s
                    
                    Items:
                    %s
                    
                    Thank you for your business!
                    
                    Regards,
                    Foundry Team
                    """,
                    customer.getName(),
                    order.getOrderNumber(),
                    order.getOrderType(),
                    order.getOrderDate(),
                    order.getDeliveryDate() != null ? order.getDeliveryDate() : "To be confirmed",
                    order.getTotalAmount(),
                    formatOrderItems(order.getItems()));

            emailService.sendEmail(customer.getEmail(), subject, body);

            log.info("Order confirmation email sent for: {}", order.getOrderNumber());

        } catch (Exception e) {
            log.error("Failed to send order confirmation email for: {}",
                    order.getOrderNumber(), e);
            // Don't throw - email failure shouldn't fail order creation
        }
    }

    private String formatOrderItems(List<OrderItem> items) {

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < items.size(); i++) {
            OrderItem item = items.get(i);
            sb.append(String.format(
                    "  %d. %s | Grade: %s | Metal: %s | Process: %s | Qty: %d | Weight: %s kg | Total: ₹%s\n",
                    i + 1,
                    item.getPartName(),
                    item.getMaterialGrade() != null ? item.getMaterialGrade() : "N/A",
                    item.getMetalType() != null ? item.getMetalType().getDisplayName() : "N/A",
                    item.getCastingProcess() != null ? item.getCastingProcess() : "N/A",
                    item.getQuantity(),
                    item.getNetWeightKg(),
                    item.getLineTotal()));
        }

        return sb.toString();
    }

    // =========================================================
    //  ORDER NUMBER GENERATION
    // =========================================================

    private synchronized String generateOrderNumber() {

        int year = LocalDate.now().getYear();
        String prefix = "ORD-" + year + "-";

        String lastNumber = orderRepository
                .findTopByOrderNumberStartingWithOrderByOrderNumberDesc(prefix)
                .map(Order::getOrderNumber)
                .orElse(null);

        int next = 1;

        if (lastNumber != null) {
            String[] parts = lastNumber.split("-");
            if (parts.length == 3) {
                next = Integer.parseInt(parts[2]) + 1;
            }
        }

        return String.format("%s%04d", prefix, next);
    }
}