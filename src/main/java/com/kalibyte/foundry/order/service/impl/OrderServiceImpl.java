package com.kalibyte.foundry.order.service.impl;

import com.kalibyte.foundry.common.email.EmailService;
import com.kalibyte.foundry.common.exception.ResourceNotFoundException;
import com.kalibyte.foundry.common.response.PageResponse;
import com.kalibyte.foundry.customer.entity.Customer;
import com.kalibyte.foundry.customer.repository.CustomerRepository;
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
    private final OrderMapper orderMapper;
    private final EmailService emailService;
    private final PatternRepository patternRepository;

    //-----------------------------------------------------
    // CREATE ORDER
    //-----------------------------------------------------

    @Override
    public OrderResponse createOrder(OrderCreateRequest request) {

        if (request.getQuotationId() != null) {
            return createFromQuotation(request);
        }

        if (request.getCustomerId() != null) {
            return createDirectOrder(request);
        }

        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Either quotationId or customerId must be provided"
        );
    }

    //-----------------------------------------------------
    // FROM QUOTATION
    //-----------------------------------------------------

    private OrderResponse createFromQuotation(OrderCreateRequest request) {

        Quotation quotation = quotationRepository.findById(request.getQuotationId())
                .orElseThrow(() -> new ResourceNotFoundException("Quotation not found"));

        if (!QuotationStatus.APPROVED.equals(quotation.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Quotation must be approved"
            );
        }

        if (orderRepository.existsByQuotationId(request.getQuotationId())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Order already exists for this quotation"
            );
        }

        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .customer(quotation.getCustomer())
                .quotation(quotation)
                .orderType(OrderType.QUOTATION)
                .orderDate(LocalDate.now())
                .deliveryDate(request.getDeliveryDate())
                .status(OrderStatus.CREATED)
                .placeOfSupply(request.getPlaceOfSupply())
                .poReference(request.getPoReference())
                .build();

        List<OrderItem> items = quotation.getItems()
                .stream()
                .map(q -> {

                    OrderItem item = OrderItem.builder()
                            .order(order)
                            .partName(q.getPartName())
                            .materialGrade(q.getMaterialGrade())
                            .netWeightKg(q.getNetWeightKg())
                            .quantity(q.getQuantity())
                            .unitPrice(q.getUnitPrice())
                            .lineTotal(q.getLineTotal())
                            .patternProvidedByCustomer(q.getPatternProvidedByCustomer())
                            .build();

                    if (Boolean.TRUE.equals(q.getPatternProvidedByCustomer())) {
                        item.setPatternReceipt(q.getPatternReceipt());
                    } else {
                        item.setPattern(q.getPattern());
                    }

                    return item;
                })
                .toList();

        order.setItems(items);

        BigDecimal total = items.stream()
                .map(OrderItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setTotalAmount(total);

        Order saved = orderRepository.save(order);

        sendOrderConfirmationEmail(saved);

        return orderMapper.toResponse(saved);
    }

    //-----------------------------------------------------
    // DIRECT ORDER
    //-----------------------------------------------------

    private OrderResponse createDirectOrder(OrderCreateRequest request) {

        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Items required"
            );
        }

        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .customer(customer)
                .orderType(OrderType.DIRECT)
                .orderDate(LocalDate.now())
                .deliveryDate(request.getDeliveryDate())
                .placeOfSupply(request.getPlaceOfSupply())
                .poReference(request.getPoReference())
                .status(OrderStatus.CREATED)
                .build();

        List<OrderItem> items = request.getItems()
                .stream()
                .map(i -> buildOrderItem(order, i))
                .toList();

        order.setItems(items);

        BigDecimal total = items.stream()
                .map(OrderItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setTotalAmount(total);

        Order saved = orderRepository.save(order);

        sendOrderConfirmationEmail(saved);

        return orderMapper.toResponse(saved);
    }

    //-----------------------------------------------------
    // ITEM BUILDER (FINAL FIX)
    //-----------------------------------------------------

    private OrderItem buildOrderItem(Order order, OrderItemRequest item) {

        if (item.getNetWeightKg() == null) {
            throw new IllegalArgumentException("Net weight is required");
        }

        BigDecimal total = item.getNetWeightKg()
                .multiply(item.getUnitPrice())
                .multiply(BigDecimal.valueOf(item.getQuantity()));

        OrderItem orderItem = OrderItem.builder()
                .order(order)
                .partName(item.getPartName())
                .materialGrade(item.getMaterialGrade())
                .netWeightKg(item.getNetWeightKg())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .lineTotal(total)
                .patternProvidedByCustomer(item.getPatternProvidedByCustomer())
                .build();

        if (Boolean.TRUE.equals(item.getPatternProvidedByCustomer())) {

            PatternReceiptRequest pr = item.getPatternReceipt();

            PatternReceipt receipt = PatternReceipt.builder()
                    .name(pr.getName())
                    .type(pr.getType())
                    .material(pr.getMaterial())
                    .inwardDate(pr.getInwardDate())
                    .outwardDate(pr.getOutwardDate())
                    .build();

            orderItem.setPatternReceipt(receipt);

        } else {

            Pattern pattern = patternRepository.findById(item.getPatternId())
                    .orElseThrow(() -> new ResourceNotFoundException("Pattern not found"));

            orderItem.setPattern(pattern);
        }

        return orderItem;
    }

    //-----------------------------------------------------
    // STATUS UPDATE
    //-----------------------------------------------------

    @Override
    public void updateStatus(UUID id, OrderStatus status) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        OrderStatusTransitionValidator.validate(order.getStatus(), status);

        order.setStatus(status);
    }

    //-----------------------------------------------------
    // GET METHODS
    //-----------------------------------------------------

    @Override
    public OrderResponse getById(UUID id) {
        return orderMapper.toResponse(
                orderRepository.findWithDetailsById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Order not found"))
        );
    }

    @Override
    public PageResponse<OrderResponse> getAll(
            OrderStatus status,
            UUID customerId,
            LocalDate from,
            LocalDate to,
            Pageable pageable) {

        Specification<Order> spec =
                OrderSpecification.filter(status, customerId, from, to);

        Page<Order> page = orderRepository.findAll(spec, pageable);

        return PageResponse.of(page.map(orderMapper::toResponse));
    }

    //-----------------------------------------------------
    // EMAIL
    //-----------------------------------------------------

    private void sendOrderConfirmationEmail(Order order) {

        Customer customer = order.getCustomer();

        String body = "Order: " + order.getOrderNumber()
                + "\nAmount: " + order.getTotalAmount();

        emailService.sendEmail(customer.getEmail(), "Order Created", body);
    }

    //-----------------------------------------------------
    // NUMBER
    //-----------------------------------------------------

    private String generateOrderNumber() {

        int year = LocalDate.now().getYear();
        long count = orderRepository.count() + 1;

        return String.format("ORD-%d-%04d", year, count);
    }
}