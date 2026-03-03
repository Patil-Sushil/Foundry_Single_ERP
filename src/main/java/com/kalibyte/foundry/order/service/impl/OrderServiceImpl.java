package com.kalibyte.foundry.order.service.impl;

import com.kalibyte.foundry.common.exception.ResourceNotFoundException;
import com.kalibyte.foundry.common.response.PageResponse;
import com.kalibyte.foundry.customer.entity.Customer;
import com.kalibyte.foundry.customer.repository.CustomerRepository;
import com.kalibyte.foundry.order.dto.request.OrderCreateRequest;
import com.kalibyte.foundry.order.dto.request.OrderItemRequest;
import com.kalibyte.foundry.order.dto.response.OrderResponse;
import com.kalibyte.foundry.order.entity.ENUM.OrderType;
import com.kalibyte.foundry.order.entity.Order;
import com.kalibyte.foundry.order.entity.OrderItem;
import com.kalibyte.foundry.order.entity.ENUM.OrderStatus;
import com.kalibyte.foundry.order.mapper.OrderMapper;
import com.kalibyte.foundry.order.repository.OrderRepository;
import com.kalibyte.foundry.order.service.OrderService;
import com.kalibyte.foundry.order.specification.OrderSpecification;
import com.kalibyte.foundry.order.validation.OrderStatusTransitionValidator;
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

    // =====================================================
    // CREATE ORDER (Quotation-based OR Direct)
    // =====================================================
    @Override
    public OrderResponse createOrder(OrderCreateRequest request) {

        log.info("Creating order request received");

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

    // =====================================================
    // CREATE FROM QUOTATION
    // =====================================================
    private OrderResponse createFromQuotation(OrderCreateRequest request) {

        Quotation quotation = quotationRepository.findById(request.getQuotationId())
                .orElseThrow(() -> new ResourceNotFoundException("Quotation not found"));


        if (!QuotationStatus.APPROVED.equals(quotation.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Quotation must be approved before creating order"
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
                .totalAmount(quotation.getTotalAmount())
                .build();

        List<OrderItem> items = quotation.getItems()
                .stream()
                .map(qItem -> OrderItem.builder()
                        .order(order)
                        .productName(qItem.getPartName())
                        .metalType(qItem.getMaterialGrade())
                        .quantity(qItem.getQuantity())
                        .unitPrice(qItem.getUnitPrice())
                        .totalPrice(qItem.getLineTotal())
                        .build())
                .toList();

        order.setOrderItems(items);

        Order saved = orderRepository.save(order);

        log.info("Order created from quotation successfully. Order ID: {}", saved.getId());

        return orderMapper.toResponse(saved);
    }

    // =====================================================
    // CREATE DIRECT ORDER
    // =====================================================
    private OrderResponse createDirectOrder(OrderCreateRequest request) {

        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Order items are required for direct order"
            );
        }

        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .customer(customer)
                .orderType(OrderType.DIRECT)
                .orderDate(LocalDate.now())
                .deliveryDate(request.getDeliveryDate())
                .status(OrderStatus.CREATED)
                .build();

        List<OrderItem> items = request.getItems()
                .stream()
                .map(item -> buildOrderItem(order,item))
                .toList();

        order.setOrderItems(items);

        BigDecimal totalAmount = items.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setTotalAmount(totalAmount);

        Order saved = orderRepository.save(order);

        log.info("Direct order created successfully. Order ID: {}", saved.getId());

        return orderMapper.toResponse(saved);
    }


    // =====================================================
    // GET BY ID (Full Details)
    // =====================================================
    @Override
    @Transactional(readOnly = true)
    public OrderResponse getById(UUID id) {

        Order order = orderRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        return orderMapper.toResponse(order);
    }

    // =====================================================
    // GET ALL WITH FILTERING
    // =====================================================
    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> getAll(
            OrderStatus status,
            UUID customerId,
            LocalDate from,
            LocalDate to,
            Pageable pageable) {

        Specification<Order> specification =
                OrderSpecification.filter(status, customerId, from, to);

        Page<Order> page = orderRepository.findAll(specification, pageable);

        return PageResponse.of(page.map(orderMapper::toResponse));
    }

    // =====================================================
    // UPDATE STATUS
    // =====================================================
    @Override
    public void updateStatus(UUID id, OrderStatus newStatus) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        OrderStatusTransitionValidator.validate(order.getStatus(), newStatus);

        log.info("Updating order {} status from {} to {}",
                order.getId(), order.getStatus(), newStatus);

        order.setStatus(newStatus);
    }

    // =====================================================
    // PRIVATE HELPERS
    // =====================================================
    private OrderItem buildOrderItem(Order order, OrderItemRequest item) {

        BigDecimal total = item.getUnitPrice().multiply(item.getQuantity());

        return OrderItem.builder()
                .order(order)
                .productName(item.getProductName())
                .metalType(item.getMetalType())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .totalPrice(total)
                .build();
    }

    private String generateOrderNumber() {
        int year = LocalDate.now().getYear();
        long count = orderRepository.count() + 1;
        return String.format("ORD-%d-%05d", year, count);
    }
}