package com.kalibyte.foundry.billing.invoice.service.impl;

import com.kalibyte.foundry.billing.deliveryChallan.entity.DeliveryChallanItem;
import com.kalibyte.foundry.billing.deliveryChallan.repository.DeliveryChallanItemRepository;
import com.kalibyte.foundry.billing.invoice.dto.request.InvoiceRequest;
import com.kalibyte.foundry.billing.invoice.dto.response.InvoiceResponse;
import com.kalibyte.foundry.billing.invoice.entity.Invoice;
import com.kalibyte.foundry.billing.invoice.entity.InvoiceItem;
import com.kalibyte.foundry.billing.invoice.entity.enums.InvoiceStatus;
import com.kalibyte.foundry.billing.invoice.mapper.InvoiceMapper;
import com.kalibyte.foundry.billing.invoice.repository.InvoiceItemRepository;
import com.kalibyte.foundry.billing.invoice.repository.InvoiceRepository;
import com.kalibyte.foundry.billing.invoice.service.InvoicePaymentService;
import com.kalibyte.foundry.billing.invoice.service.InvoiceService;
import com.kalibyte.foundry.billing.util.GstCalculationResult;
import com.kalibyte.foundry.billing.util.InvoiceNumberGenerator;
import com.kalibyte.foundry.billing.util.PdfGenerator;
import com.kalibyte.foundry.common.email.EmailService;
import com.kalibyte.foundry.common.response.PageResponse;
import com.kalibyte.foundry.customer.entity.Customer;
import com.kalibyte.foundry.order.entity.enums.OrderStatus;
import com.kalibyte.foundry.order.entity.Order;
import com.kalibyte.foundry.order.repository.OrderRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Year;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceItemRepository invoiceItemRepository;
    private final DeliveryChallanItemRepository deliveryChallanItemRepository;
    private final OrderRepository orderRepository;
    private final InvoiceMapper invoiceMapper;  // Injected MapStruct mapper
    private final PdfGenerator pdfGenerator;
    private final EmailService emailService;
    private final InvoicePaymentService invoicePaymentService;
    private final InvoiceNumberGenerator invoiceNumberGenerator;

    public InvoiceServiceImpl(InvoiceRepository invoiceRepository, InvoiceItemRepository invoiceItemRepository, DeliveryChallanItemRepository deliveryChallanItemRepository, OrderRepository orderRepository, InvoiceMapper invoiceMapper, PdfGenerator pdfGenerator, EmailService emailService, InvoicePaymentService invoicePaymentService, InvoiceNumberGenerator invoiceNumberGenerator) {
        this.invoiceRepository = invoiceRepository;
        this.invoiceItemRepository = invoiceItemRepository;
        this.deliveryChallanItemRepository = deliveryChallanItemRepository;
        this.orderRepository = orderRepository;
        this.invoiceMapper = invoiceMapper;
        this.pdfGenerator = pdfGenerator;
        this.emailService = emailService;
        this.invoicePaymentService = invoicePaymentService;
        this.invoiceNumberGenerator = invoiceNumberGenerator;
    }

    //------------------------------------------------
    // GENERATE INVOICE
    //------------------------------------------------

    @Override
    @Transactional
    public InvoiceResponse generateInvoice(InvoiceRequest request) {

        //------------------------------------------------
        // FETCH ORDER
        //------------------------------------------------

        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        //------------------------------------------------
        // PREVENT DUPLICATE INVOICE
        //------------------------------------------------

        if (invoiceRepository.findByOrder(order).isPresent()) {
            throw new RuntimeException("Invoice already exists for this order");
        }

        //------------------------------------------------
        // VALIDATE ORDER COMPLETION
        //------------------------------------------------

        if (!isOrderFullyDispatched(order)) {
            throw new RuntimeException("Cannot generate invoice. Order is not fully dispatched yet.");
        }

        //------------------------------------------------
        // FETCH DISPATCHED ITEMS
        //------------------------------------------------

        List<DeliveryChallanItem> dcItems =
                deliveryChallanItemRepository.findByDeliveryChallan_Order(order);

        Customer customer = order.getCustomer();

        //------------------------------------------------
        // CALCULATE SUBTOTAL
        //------------------------------------------------

        BigDecimal subtotal = dcItems.stream()
                .map(DeliveryChallanItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        //------------------------------------------------
        // GST CALCULATION
        //------------------------------------------------

        BigDecimal gstPercentage = order.getGstPercentage() != null
                ? order.getGstPercentage() : BigDecimal.valueOf(18);

        GstCalculationResult gstResult = GstCalculationResult.calculate(
                subtotal, gstPercentage, customer.getState());

        //------------------------------------------------
        // CREATE INVOICE
        //------------------------------------------------

        Invoice invoice = Invoice.builder()
                .invoiceNumber(invoiceNumberGenerator.generateInvoiceNumber())
                .order(order)
                .vehicleNumber(request.getVehicleNumber())
                .subtotal(subtotal)
                .gstType(gstResult.getGstType())
                .gstPercentage(gstResult.getGstPercentage())
                .cgst(gstResult.getCgst())
                .sgst(gstResult.getSgst())
                .igst(gstResult.getIgst())
                .totalGst(gstResult.getTotalGst())
                .totalAmount(gstResult.getGrandTotal())
                .invoiceDate(request.getInvoiceDate())
                .dueDate(request.getDueDate())
                .billStatus(InvoiceStatus.UNPAID)
                .build();

        invoiceRepository.save(invoice);
        //------------------------------------------------
        // PROCESS AUTOMATIC PAYMENT
        //------------------------------------------------

//        if (request.getAmountPaid() != null && request.getAmountPaid().compareTo(BigDecimal.ZERO) > 0) {
//            invoicePaymentService.processAutomaticPayment(invoice, request.getAmountPaid());
//        }

        //------------------------------------------------
        // CREATE INVOICE ITEMS WITH GST
        //------------------------------------------------

        List<InvoiceItem> items = dcItems.stream()
                .map(dcItem -> {
                    BigDecimal itemGstPct = dcItem.getGstPercentage() != null
                            ? dcItem.getGstPercentage() : gstPercentage;

                    BigDecimal itemGstAmount = dcItem.getAmount()
                            .multiply(itemGstPct)
                            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

                    BigDecimal itemTotalWithGst = dcItem.getAmount().add(itemGstAmount);

                    return InvoiceItem.builder()
                            .invoice(invoice)
                            .orderItem(dcItem.getOrderItem())
                            .quantity(dcItem.getQuantity())
                            .weight(dcItem.getWeight())
                            .rate(dcItem.getRate())
                            .amount(dcItem.getAmount())
                            .gstPercentage(itemGstPct)
                            .gstAmount(itemGstAmount)
                            .totalWithGst(itemTotalWithGst)
                            .build();
                })
                .collect(Collectors.toList());

        invoiceItemRepository.saveAll(items);

        invoice.setItems(items);

        //------------------------------------------------
        // GENERATE PDF
        //------------------------------------------------

        byte[] pdf = pdfGenerator.generateInvoicePdf(invoice, items);

        //------------------------------------------------
        // SEND EMAIL
        //------------------------------------------------

        Map<String, Object> variables = new HashMap<>();
        variables.put("customerName", customer.getName());
        variables.put("invoiceNumber", invoice.getInvoiceNumber());
        variables.put("invoiceDate", invoice.getInvoiceDate().toString());
        variables.put("dueDate", invoice.getDueDate().toString());
        variables.put("totalAmount", "₹ " + invoice.getTotalAmount());

        emailService.sendTemplatedEmailWithAttachment(
                customer.getEmail(),
                "Invoice - " + invoice.getInvoiceNumber(),
                "invoice",
                variables,
                pdf,
                "Invoice-" + invoice.getInvoiceNumber() + ".pdf"
        );

        //------------------------------------------------
        // AUTO CLOSE ORDER
        //------------------------------------------------

        order.setStatus(OrderStatus.COMPLETED);
        orderRepository.save(order);

        return invoiceMapper.toResponse(invoice);
    }

    //------------------------------------------------
    // VALIDATE ORDER COMPLETION
    //------------------------------------------------

    private boolean isOrderFullyDispatched(Order order) {

        BigDecimal orderedQty = order.getItems().stream()
                .map(i -> BigDecimal.valueOf(i.getQuantity()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal dispatchedQty =
                deliveryChallanItemRepository
                        .findByDeliveryChallan_Order(order)
                        .stream()
                        .map(i -> BigDecimal.valueOf(i.getQuantity()))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        return dispatchedQty.compareTo(orderedQty) >= 0;
    }

    //------------------------------------------------
    // GET INVOICE
    //------------------------------------------------

    @Override
    @Transactional
    public InvoiceResponse getInvoice(UUID id) {

        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

        return invoiceMapper.toResponse(invoice);
    }

    //------------------------------------------------
    // GENERATE INVOICE PDF
    //------------------------------------------------

    @Override
    @Transactional
    public byte[] generateInvoicePdf(UUID invoiceId) {

        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

        List<InvoiceItem> items =
                invoiceItemRepository.findByInvoice(invoice);

        return pdfGenerator.generateInvoicePdf(invoice, items);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<InvoiceResponse> getAllInvoices(Pageable pageable) {

        var page = invoiceRepository.findAll(pageable);

        List<InvoiceResponse> content = invoiceMapper.toResponseList(
                page.getContent()
        );

        return PageResponse.<InvoiceResponse>builder()
                .content(content)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }
}