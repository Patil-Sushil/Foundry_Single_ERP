package com.kalibyte.foundry.billing.deliveryChallan.service.impl;

import com.kalibyte.foundry.billing.deliveryChallan.entity.enums.DCStatus;
import com.kalibyte.foundry.billing.deliveryChallan.dto.request.DeliveryChallanItemRequest;
import com.kalibyte.foundry.billing.deliveryChallan.dto.request.DeliveryChallanRequest;
import com.kalibyte.foundry.billing.deliveryChallan.dto.response.DeliveryChallanResponse;
import com.kalibyte.foundry.billing.deliveryChallan.dto.response.DispatchAvailableResponse;
import com.kalibyte.foundry.billing.deliveryChallan.entity.DeliveryChallan;
import com.kalibyte.foundry.billing.deliveryChallan.entity.DeliveryChallanItem;
import com.kalibyte.foundry.billing.deliveryChallan.mapper.DeliveryChallanMapper;
import com.kalibyte.foundry.billing.deliveryChallan.repository.DeliveryChallanItemRepository;
import com.kalibyte.foundry.billing.deliveryChallan.repository.DeliveryChallanRepository;
import com.kalibyte.foundry.billing.deliveryChallan.service.DeliveryChallanService;
import com.kalibyte.foundry.billing.util.DCNumberGenerator;
import com.kalibyte.foundry.billing.util.GstCalculationResult;
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
import com.kalibyte.foundry.production.repository.ProductionItemRepository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DeliveryChallanServiceImpl implements DeliveryChallanService {

    private final DeliveryChallanRepository deliveryChallanRepository;
    private final DeliveryChallanItemRepository itemRepository;
    private final ProductionItemRepository productionItemRepository;
    private final DCNumberGenerator dcNumberGenerator;

    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PatternRepository patternRepository;

    private final DeliveryChallanMapper deliveryChallanMapper;  // Injected MapStruct mapper
    private final EmailService emailService;
    private final PdfGenerator pdfGenerator;

    public DeliveryChallanServiceImpl(DeliveryChallanRepository deliveryChallanRepository, DeliveryChallanItemRepository itemRepository, ProductionItemRepository productionItemRepository, DCNumberGenerator dcNumberGenerator, CustomerRepository customerRepository, OrderRepository orderRepository, OrderItemRepository orderItemRepository, PatternRepository patternRepository, DeliveryChallanMapper deliveryChallanMapper, EmailService emailService, PdfGenerator pdfGenerator) {
        this.deliveryChallanRepository = deliveryChallanRepository;
        this.itemRepository = itemRepository;
        this.productionItemRepository = productionItemRepository;
        this.dcNumberGenerator = dcNumberGenerator;
        this.customerRepository = customerRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.patternRepository = patternRepository;
        this.deliveryChallanMapper = deliveryChallanMapper;
        this.emailService = emailService;
        this.pdfGenerator = pdfGenerator;
    }

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
        // CREATE ITEMS WITH GST
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

        BigDecimal subtotal = items.stream()
                .map(DeliveryChallanItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        dc.setTotalQuantity(totalQty);
        dc.setTotalWeight(totalWeight);
        dc.setSubtotal(subtotal);

        //------------------------------------------------
        // CALCULATE GST AT DC LEVEL
        //------------------------------------------------

        BigDecimal gstPercentage = order.getGstPercentage() != null
                ? order.getGstPercentage() : BigDecimal.valueOf(18);

        GstCalculationResult gstResult = GstCalculationResult.calculate(
                subtotal, gstPercentage, customer.getState());

        dc.setGstType(gstResult.getGstType());
        dc.setGstPercentage(gstResult.getGstPercentage());
        dc.setCgst(gstResult.getCgst());
        dc.setSgst(gstResult.getSgst());
        dc.setIgst(gstResult.getIgst());
        dc.setTotalGst(gstResult.getTotalGst());
        dc.setTotalAmount(gstResult.getGrandTotal());

        deliveryChallanRepository.save(dc);

        //------------------------------------------------
        // CHECK ORDER COMPLETION
        //------------------------------------------------

        checkAndCompleteOrder(order);

        //------------------------------------------------
        // GENERATE PDF + EMAIL
        //------------------------------------------------

        byte[] pdf = pdfGenerator.generateDeliveryChallanPdf(dc, items);

        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("customerName", customer.getName());
            variables.put("dcNumber", dc.getDcNumber());
            variables.put("dispatchDate", dc.getDispatchDate().toString());
            variables.put("vehicleNumber", dc.getVehicleNumber());
            variables.put("totalItems", dc.getTotalQuantity());

            emailService.sendTemplatedEmailWithAttachment(
                    customer.getEmail(),
                    "Dispatch Notification - " + dc.getDcNumber(),
                    "dispatch",
                    variables,
                    pdf,
                    "DeliveryChallan-" + dc.getDcNumber() + ".pdf"
            );
            log.info("Delivery challan email sent successfully for: {}", dc.getDcNumber());
        } catch (Exception e) {
            log.error("Failed to send delivery challan email for {}: {}", dc.getDcNumber(), e.getMessage());
        }

        return deliveryChallanMapper.toResponse(dc);
    }

    //------------------------------------------------
    // CREATE ITEM WITH GST
    //------------------------------------------------

    private DeliveryChallanItem createItem(DeliveryChallanItemRequest request, DeliveryChallan dc) {

        OrderItem orderItem = orderItemRepository.findById(request.getOrderItemId())
                .orElseThrow(() -> new RuntimeException("Order item not found"));

        int orderedQuantity = orderItem.getQuantity();
        int totalAccepted = productionItemRepository.getTotalAcceptedQuantity(orderItem.getId());
        Integer alreadyDispatched = itemRepository.getTotalDispatchedQuantity(orderItem.getId());
        if (alreadyDispatched == null) alreadyDispatched = 0;

        int ceiling = Math.min(orderedQuantity, totalAccepted);
        int availableForDispatch = ceiling - alreadyDispatched;

        if (totalAccepted == 0) {
            throw new com.kalibyte.foundry.common.exception.BusinessException(String.format(
                    "No items available for dispatch for [%s]. Nothing has been accepted by QA yet.",
                    orderItem.getPartName()
            ));
        }

        if (availableForDispatch <= 0) {
            throw new com.kalibyte.foundry.common.exception.BusinessException(String.format(
                    "No items available for dispatch for [%s]. Either nothing is produced/accepted by QA or everything is already dispatched. (Accepted: %d, Dispatched: %d)",
                    orderItem.getPartName(), totalAccepted, alreadyDispatched
            ));
        }

        if (request.getQuantity() > availableForDispatch) {
            throw new com.kalibyte.foundry.common.exception.BusinessException(String.format(
                    "Cannot dispatch %d pcs for [%s]. Only %d pcs available for dispatch (Accepted: %d, Already Dispatched: %d)",
                    request.getQuantity(), orderItem.getPartName(), availableForDispatch, totalAccepted, alreadyDispatched
            ));
        }

        BigDecimal amount = request.getWeight().multiply(request.getRate());

        BigDecimal gstPercentage = orderItem.getGstPercentage() != null
                ? orderItem.getGstPercentage() : BigDecimal.valueOf(18);

        BigDecimal gstAmount = amount.multiply(gstPercentage)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        BigDecimal totalWithGst = amount.add(gstAmount);

        return DeliveryChallanItem.builder()
                .deliveryChallan(dc)
                .orderItem(orderItem)
                .quantity(request.getQuantity())
                .weight(request.getWeight())
                .rate(request.getRate())
                .amount(amount)
                .gstPercentage(gstPercentage)
                .gstAmount(gstAmount)
                .totalWithGst(totalWithGst)
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

        return deliveryChallanMapper.toResponse(dc);
    }

    //------------------------------------------------
    // GET ALL DELIVERY CHALLANS
    //------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public List<DeliveryChallanResponse> getAllDeliveryChallans() {

        return deliveryChallanMapper.toResponseList(
                deliveryChallanRepository.findAll()
        );
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

        return deliveryChallanMapper.toResponse(dc);
    }

    //------------------------------------------------
    // PAGINATION
    //------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DeliveryChallanResponse> list(Pageable pageable) {

        var page = deliveryChallanRepository.findAll(pageable);

        List<DeliveryChallanResponse> content = deliveryChallanMapper.toResponseList(
                page.getContent()
        );

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

    @Override
    @Transactional(readOnly = true)
    public DispatchAvailableResponse getDispatchAvailable(UUID orderItemId) {
        OrderItem orderItem = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new RuntimeException("Order item not found"));

        int orderedQuantity = orderItem.getQuantity();
        int totalAccepted = productionItemRepository.getTotalAcceptedQuantity(orderItemId);
        Integer alreadyDispatched = itemRepository.getTotalDispatchedQuantity(orderItemId);
        if (alreadyDispatched == null) alreadyDispatched = 0;

        int ceiling = Math.min(orderedQuantity, totalAccepted);
        int availableForDispatch = Math.max(0, ceiling - alreadyDispatched);

        return DispatchAvailableResponse.builder()
                .orderItemId(orderItemId)
                .orderedQuantity(orderedQuantity)
                .totalAccepted(totalAccepted)
                .alreadyDispatched(alreadyDispatched)
                .availableForDispatch(availableForDispatch)
                .build();
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

        int totalOrderedQty = order.getItems()
                .stream()
                .mapToInt(OrderItem::getQuantity)
                .sum();

        if (totalDispatchedQty >= totalOrderedQty) {

            order.setStatus(OrderStatus.COMPLETED);
            orderRepository.save(order);

            for (OrderItem orderItem : order.getItems()) {

                Pattern pattern = orderItem.getPattern();

                if (pattern != null) {
                    pattern.setStatus(PatternStatus.AVAILABLE);
                    patternRepository.save(pattern);
                }
            }
        }
    }
}
