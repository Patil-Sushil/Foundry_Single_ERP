package com.kalibyte.foundry.billing.deliveryChallan.service.impl;

import com.kalibyte.foundry.billing.deliveryChallan.entity.enums.DCStatus;
import com.kalibyte.foundry.billing.deliveryChallan.dto.request.DeliveryChallanItemRequest;
import com.kalibyte.foundry.billing.deliveryChallan.dto.request.DeliveryChallanRequest;
import com.kalibyte.foundry.billing.deliveryChallan.dto.response.DeliveryChallanResponse;
import com.kalibyte.foundry.billing.deliveryChallan.entity.DeliveryChallan;
import com.kalibyte.foundry.billing.deliveryChallan.entity.DeliveryChallanItem;
import com.kalibyte.foundry.billing.deliveryChallan.mapper.DeliveryChallanMapper;
import com.kalibyte.foundry.billing.deliveryChallan.repository.DeliveryChallanItemRepository;
import com.kalibyte.foundry.billing.deliveryChallan.repository.DeliveryChallanRepository;
import com.kalibyte.foundry.billing.deliveryChallan.service.DeliveryChallanService;
import com.kalibyte.foundry.billing.util.DCNumberGenerator;
import com.kalibyte.foundry.billing.util.PdfGenerator;
import com.kalibyte.foundry.common.email.EmailService;
import com.kalibyte.foundry.common.response.PageResponse;
import com.kalibyte.foundry.customer.entity.Customer;
import com.kalibyte.foundry.customer.repository.CustomerRepository;
import com.kalibyte.foundry.order.entity.Order;
import com.kalibyte.foundry.order.entity.OrderItem;
import com.kalibyte.foundry.order.entity.enums.OrderStatus;
import com.kalibyte.foundry.order.repository.OrderItemRepository;
import com.kalibyte.foundry.order.repository.OrderRepository;
import com.kalibyte.foundry.pattern.entity.Pattern;
import com.kalibyte.foundry.pattern.entity.enums.PatternStatus;
import com.kalibyte.foundry.pattern.repository.PatternRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeliveryChallanServiceImpl implements DeliveryChallanService {

    private final DeliveryChallanRepository deliveryChallanRepository;
    private final DeliveryChallanItemRepository itemRepository;
    private final DCNumberGenerator dcNumberGenerator;

    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PatternRepository patternRepository;

    private final EmailService emailService;
    private final PdfGenerator pdfGenerator;

    //------------------------------------------------
    // CREATE DELIVERY CHALLAN
    //------------------------------------------------

    @Override
    @Transactional
    public DeliveryChallanResponse createDeliveryChallan(DeliveryChallanRequest request) {

        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        DeliveryChallan dc = DeliveryChallan.builder()
                .dcNumber(dcNumberGenerator.generateDCNumber())
                .order(order)
                .customer(customer)
                .dispatchDate(request.getDispatchDate())
                .vehicleNumber(request.getVehicleNumber())
                .transportName(request.getTransportName())
                .lrNumber(request.getLrNumber())
                .status(DCStatus.CREATED)
                .build();

        deliveryChallanRepository.save(dc);

        //------------------------------------------------
        // CREATE ITEMS
        //------------------------------------------------

        List<DeliveryChallanItem> items = request.getItems()
                .stream()
                .map(item -> createItem(item, dc))
                .collect(Collectors.toList());

        itemRepository.saveAll(items);
        dc.setItems(items);

        //------------------------------------------------
        // CALCULATE TOTALS
        //------------------------------------------------

        int totalQty = items.stream()
                .mapToInt(DeliveryChallanItem::getQuantity)
                .sum();

        BigDecimal totalWeight = items.stream()
                .map(DeliveryChallanItem::getWeight)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalAmount = items.stream()
                .map(DeliveryChallanItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        dc.setTotalQuantity(totalQty);
        dc.setTotalWeight(totalWeight);
        dc.setTotalAmount(totalAmount);

        deliveryChallanRepository.save(dc);

        //------------------------------------------------
        // CHECK ORDER COMPLETION
        //------------------------------------------------

        checkAndCompleteOrder(order);

        //------------------------------------------------
        // GENERATE PDF + EMAIL
        //------------------------------------------------

        byte[] pdf = pdfGenerator.generateDeliveryChallanPdf(dc, items);

        emailService.sendEmailWithAttachment(
                customer.getEmail(),
                "Dispatch Notification - " + dc.getDcNumber(),
                "Your order has been dispatched. Please find attached Delivery Challan.",
                pdf,
                "DeliveryChallan-" + dc.getDcNumber() + ".pdf"
        );

        return DeliveryChallanMapper.toResponse(dc);
    }

    //------------------------------------------------
    // CREATE ITEM
    //------------------------------------------------

    private DeliveryChallanItem createItem(DeliveryChallanItemRequest request, DeliveryChallan dc) {

        OrderItem orderItem = orderItemRepository.findById(request.getOrderItemId())
                .orElseThrow(() -> new RuntimeException("Order item not found"));

        int orderedQty = orderItem.getQuantity();

        Integer alreadyDispatched =
                itemRepository.getTotalDispatchedQuantity(orderItem.getId());

        if (alreadyDispatched == null) alreadyDispatched = 0;

        int remainingQty = orderedQty - alreadyDispatched;

        if (request.getQuantity() > remainingQty) {
            throw new RuntimeException("Dispatch quantity exceeds remaining quantity for item");
        }

        BigDecimal amount = request.getWeight().multiply(request.getRate());

        return DeliveryChallanItem.builder()
                .deliveryChallan(dc)
                .orderItem(orderItem)
                .quantity(request.getQuantity())
                .weight(request.getWeight())
                .rate(request.getRate())
                .amount(amount)
                .build();
    }

    //------------------------------------------------
    // GET DELIVERY CHALLAN
    //------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public DeliveryChallanResponse getDeliveryChallan(UUID id) {

        DeliveryChallan dc = deliveryChallanRepository.findByIdWithItems(id)
                .orElseThrow(() -> new RuntimeException("Delivery Challan not found"));

        return DeliveryChallanMapper.toResponse(dc);
    }

    //------------------------------------------------
    // GET ALL DELIVERY CHALLANS
    //------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public List<DeliveryChallanResponse> getAllDeliveryChallans() {

        return deliveryChallanRepository.findAll()
                .stream()
                .map(DeliveryChallanMapper::toResponse)
                .collect(Collectors.toList());
    }

    //------------------------------------------------
    // DISPATCH DELIVERY CHALLAN
    //------------------------------------------------

    @Override
    @Transactional
    public DeliveryChallanResponse dispatchDeliveryChallan(UUID id) {

        DeliveryChallan dc = deliveryChallanRepository.findByIdWithItems(id)
                .orElseThrow(() -> new RuntimeException("Delivery Challan not found"));

        dc.setStatus(DCStatus.DISPATCHED);

        deliveryChallanRepository.save(dc);

        return DeliveryChallanMapper.toResponse(dc);
    }

    //------------------------------------------------
    // PAGINATION
    //------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DeliveryChallanResponse> list(Pageable pageable) {

        var page = deliveryChallanRepository.findAll(pageable);

        List<DeliveryChallanResponse> content = page.getContent()
                .stream()
                .map(DeliveryChallanMapper::toResponse)
                .collect(Collectors.toList());

        return PageResponse.<DeliveryChallanResponse>builder()
                .content(content)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }

    //------------------------------------------------
    // GENERATE DELIVERY CHALLAN PDF
    //------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public byte[] generateDeliveryChallanPdf(UUID dcId) {

        DeliveryChallan dc = deliveryChallanRepository.findByIdWithItems(dcId)
                .orElseThrow(() -> new RuntimeException("Delivery Challan not found"));

        return pdfGenerator.generateDeliveryChallanPdf(dc, dc.getItems());
    }

    //------------------------------------------------
    // ORDER COMPLETION CHECK
    //------------------------------------------------

    private void checkAndCompleteOrder(Order order) {

        List<DeliveryChallanItem> items =
                itemRepository.findByDeliveryChallan_Order(order);

        int totalDispatchedQty = items.stream()
                .mapToInt(DeliveryChallanItem::getQuantity)
                .sum();

        int totalOrderedQty = order.getOrderItems()
                .stream()
                .mapToInt(OrderItem::getQuantity)
                .sum();

        if (totalDispatchedQty >= totalOrderedQty) {

            order.setStatus(OrderStatus.COMPLETED);
            orderRepository.save(order);

            Pattern pattern = order.getPattern();

            if (pattern != null) {
                pattern.setStatus(PatternStatus.AVAILABLE);
                patternRepository.save(pattern);
            }
        }
    }
}