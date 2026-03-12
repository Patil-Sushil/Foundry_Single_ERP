package com.kalibyte.foundry.billing.invoice.service.impl;

import com.kalibyte.foundry.billing.deliveryChallan.entity.DeliveryChallanItem;
import com.kalibyte.foundry.billing.deliveryChallan.repository.DeliveryChallanItemRepository;
import com.kalibyte.foundry.billing.invoice.dto.request.InvoiceRequest;
import com.kalibyte.foundry.billing.invoice.dto.response.InvoiceResponse;
import com.kalibyte.foundry.billing.invoice.entity.Invoice;
import com.kalibyte.foundry.billing.invoice.entity.InvoiceItem;
import com.kalibyte.foundry.billing.invoice.mapper.InvoiceMapper;
import com.kalibyte.foundry.billing.invoice.repository.InvoiceItemRepository;
import com.kalibyte.foundry.billing.invoice.repository.InvoiceRepository;
import com.kalibyte.foundry.billing.invoice.service.InvoiceService;
import com.kalibyte.foundry.billing.util.PdfGenerator;
import com.kalibyte.foundry.common.email.EmailService;
import com.kalibyte.foundry.common.response.PageResponse;
import com.kalibyte.foundry.customer.entity.Customer;
import com.kalibyte.foundry.order.entity.ENUM.OrderStatus;
import com.kalibyte.foundry.order.entity.Order;
import com.kalibyte.foundry.order.repository.OrderRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Year;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceItemRepository invoiceItemRepository;
    private final DeliveryChallanItemRepository deliveryChallanItemRepository;
    private final OrderRepository orderRepository;
    private final PdfGenerator pdfGenerator;
    private final EmailService emailService;

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

        BigDecimal cgst = BigDecimal.ZERO;
        BigDecimal sgst = BigDecimal.ZERO;
        BigDecimal igst = BigDecimal.ZERO;

        if ("Maharashtra".equalsIgnoreCase(customer.getState())) {

            cgst = subtotal.multiply(BigDecimal.valueOf(0.09));
            sgst = subtotal.multiply(BigDecimal.valueOf(0.09));

        } else {

            igst = subtotal.multiply(BigDecimal.valueOf(0.18));
        }

        BigDecimal total = subtotal.add(cgst).add(sgst).add(igst);

        //------------------------------------------------
        // CREATE INVOICE
        //------------------------------------------------

        Invoice invoice = Invoice.builder()
                .invoiceNumber(generateInvoiceNumber())
                .order(order)
                .vehicleNumber(request.getVehicleNumber())
                .subtotal(subtotal)
                .cgst(cgst)
                .sgst(sgst)
                .igst(igst)
                .gstPercentage(BigDecimal.valueOf(18))
                .totalAmount(total)
                .invoiceDate(request.getInvoiceDate())
                .dueDate(request.getDueDate())
                .billStatus(request.getBillStatus())
                .build();

        invoiceRepository.save(invoice);

        //------------------------------------------------
        // CREATE INVOICE ITEMS
        //------------------------------------------------

        List<InvoiceItem> items = dcItems.stream()
                .map(dcItem -> InvoiceItem.builder()
                        .invoice(invoice)
                        .orderItem(dcItem.getOrderItem())
                        .quantity(dcItem.getQuantity())
                        .weight(dcItem.getWeight())
                        .rate(dcItem.getRate())
                        .amount(dcItem.getAmount())
                        .build())
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

        emailService.sendEmailWithAttachment(
                customer.getEmail(),
                "Invoice - " + invoice.getInvoiceNumber(),
                "Please find attached invoice.",
                pdf,
                "Invoice-" + invoice.getInvoiceNumber() + ".pdf"
        );

        //------------------------------------------------
        // AUTO CLOSE ORDER
        //------------------------------------------------

        order.setStatus(OrderStatus.COMPLETED);
        orderRepository.save(order);

        return InvoiceMapper.toResponse(invoice);
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
    // INVOICE NUMBER GENERATOR
    //------------------------------------------------

    private String generateInvoiceNumber() {

        int year = Year.now().getValue();
        String prefix = "INV-" + year + "-";

        Optional<Invoice> last =
                invoiceRepository.findTopByInvoiceNumberStartingWithOrderByInvoiceNumberDesc(prefix);

        int next = 1;

        if (last.isPresent()) {

            String lastNo = last.get().getInvoiceNumber();
            String seq = lastNo.substring(prefix.length());
            next = Integer.parseInt(seq) + 1;
        }

        return prefix + String.format("%05d", next);
    }

    //------------------------------------------------
    // GET INVOICE
    //------------------------------------------------

    @Override
    @Transactional
    public InvoiceResponse getInvoice(UUID id) {

        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

        return InvoiceMapper.toResponse(invoice);
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

        List<InvoiceResponse> content = page.getContent()
                .stream()
                .map(InvoiceMapper::toResponse)
                .toList();

        return PageResponse.<InvoiceResponse>builder()
                .content(content)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }
}