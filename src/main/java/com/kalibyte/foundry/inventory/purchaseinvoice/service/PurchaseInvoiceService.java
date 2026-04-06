package com.kalibyte.foundry.inventory.purchaseinvoice.service;

import com.kalibyte.foundry.common.exception.BusinessException;
import com.kalibyte.foundry.common.exception.ResourceNotFoundException;
import com.kalibyte.foundry.common.response.PageResponse;
import com.kalibyte.foundry.common.util.SecurityUtils;
import com.kalibyte.foundry.inventory.inward.entity.MaterialInward;
import com.kalibyte.foundry.inventory.inward.repository.MaterialInwardRepository;
import com.kalibyte.foundry.inventory.purchaseinvoice.dto.request.CreatePurchaseInvoiceRequest;
import com.kalibyte.foundry.inventory.purchaseinvoice.dto.request.UpdatePurchaseInvoiceRequest;
import com.kalibyte.foundry.inventory.purchaseinvoice.dto.response.PurchaseInvoiceResponse;
import com.kalibyte.foundry.inventory.purchaseinvoice.dto.response.PurchaseInvoiceSummary;
import com.kalibyte.foundry.inventory.purchaseinvoice.entity.PurchaseInvoice;
import com.kalibyte.foundry.inventory.purchaseinvoice.repository.PurchaseInvoiceRepository;
import com.kalibyte.foundry.inventory.purchaseorder.entity.PurchaseOrder;
import com.kalibyte.foundry.inventory.purchaseorder.repository.PurchaseOrderRepository;
import com.kalibyte.foundry.inventory.vendor.entity.Vendor;
import com.kalibyte.foundry.inventory.vendor.repository.VendorRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class PurchaseInvoiceService {

    private final PurchaseInvoiceRepository purchaseInvoiceRepository;
    private final VendorRepository vendorRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final MaterialInwardRepository materialInwardRepository;

    public PurchaseInvoiceService(
            PurchaseInvoiceRepository purchaseInvoiceRepository,
            VendorRepository vendorRepository,
            PurchaseOrderRepository purchaseOrderRepository,
            MaterialInwardRepository materialInwardRepository) {
        this.purchaseInvoiceRepository = purchaseInvoiceRepository;
        this.vendorRepository = vendorRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.materialInwardRepository = materialInwardRepository;
    }

    @Transactional
    public PurchaseInvoiceResponse create(CreatePurchaseInvoiceRequest request) {
        Vendor vendor = vendorRepository.findById(request.vendorId())
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found: " + request.vendorId()));

        if (purchaseInvoiceRepository.existsByVendorIdAndVendorInvoiceNumber(
                vendor.getId(), request.vendorInvoiceNumber())) {
            throw new BusinessException("Invoice '" + request.vendorInvoiceNumber()
                + "' already exists for vendor: " + vendor.getName());
        }

        PurchaseInvoice invoice = PurchaseInvoice.builder()
                .vendorInvoiceNumber(request.vendorInvoiceNumber().trim())
                .vendorInvoiceDate(request.vendorInvoiceDate())
                .invoiceAmount(request.invoiceAmount())
                .vendor(vendor)
                .source("MANUAL")
                .remarks(request.remarks())
                .createdByUserId(SecurityUtils.getCurrentUserId())
                .build();

        if (request.purchaseOrderId() != null) {
            PurchaseOrder po = purchaseOrderRepository.findById(request.purchaseOrderId())
                    .orElseThrow(() -> new ResourceNotFoundException("PO not found: " + request.purchaseOrderId()));
            if (!po.getVendor().getId().equals(vendor.getId())) {
                throw new BusinessException("PO vendor does not match invoice vendor");
            }
            invoice.setPurchaseOrder(po);
        }

        if (request.materialInwardId() != null) {
            linkInwardToInvoice(invoice, request.materialInwardId());
        }

        return toResponse(purchaseInvoiceRepository.save(invoice));
    }

    @Transactional
    public PurchaseInvoiceResponse linkToInward(Long invoiceId, Long inwardId) {
        PurchaseInvoice invoice = purchaseInvoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase Invoice not found: " + invoiceId));
        linkInwardToInvoice(invoice, inwardId);
        return toResponse(purchaseInvoiceRepository.save(invoice));
    }

    private void linkInwardToInvoice(PurchaseInvoice invoice, Long inwardId) {
        MaterialInward inward = materialInwardRepository.findById(inwardId)
                .orElseThrow(() -> new ResourceNotFoundException("Inward not found: " + inwardId));
        if (!inward.getVendor().getId().equals(invoice.getVendor().getId())) {
            throw new BusinessException("Inward vendor does not match invoice vendor");
        }
        invoice.setMaterialInward(inward);
        if (invoice.getPurchaseOrder() == null && inward.getPurchaseOrder() != null) {
            invoice.setPurchaseOrder(inward.getPurchaseOrder());
        }
    }

    @Transactional
    public PurchaseInvoiceResponse update(Long id, UpdatePurchaseInvoiceRequest request) {
        PurchaseInvoice invoice = purchaseInvoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase Invoice not found: " + id));

        if (invoice.getIsVerified()) {
            throw new BusinessException("Cannot update a verified invoice");
        }

        if (request.vendorInvoiceNumber() != null) {
            if (!request.vendorInvoiceNumber().equals(invoice.getVendorInvoiceNumber())) {
                if (purchaseInvoiceRepository.existsByVendorIdAndVendorInvoiceNumber(
                        invoice.getVendor().getId(), request.vendorInvoiceNumber())) {
                    throw new BusinessException("Invoice number already exists for this vendor");
                }
            }
            invoice.setVendorInvoiceNumber(request.vendorInvoiceNumber().trim());
        }
        if (request.vendorInvoiceDate() != null) {
            invoice.setVendorInvoiceDate(request.vendorInvoiceDate());
        }
        if (request.invoiceAmount() != null) {
            invoice.setInvoiceAmount(request.invoiceAmount());
        }
        if (request.remarks() != null) {
            invoice.setRemarks(request.remarks());
        }
        if (request.materialInwardId() != null) {
            linkInwardToInvoice(invoice, request.materialInwardId());
        }

        return toResponse(purchaseInvoiceRepository.save(invoice));
    }

    @Transactional
    public PurchaseInvoiceResponse verify(Long id) {
        PurchaseInvoice invoice = purchaseInvoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase Invoice not found: " + id));
        invoice.verify(SecurityUtils.getCurrentUserId() != null ? Long.valueOf(SecurityUtils.getCurrentUserId().getMostSignificantBits()) : null);
        return toResponse(purchaseInvoiceRepository.save(invoice));
    }

    @Transactional(readOnly = true)
    public PurchaseInvoiceResponse getById(Long id) {
        PurchaseInvoice invoice = purchaseInvoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase Invoice not found: " + id));
        return toResponse(invoice);
    }

    @Transactional(readOnly = true)
    public List<PurchaseInvoiceResponse> getByInwardId(Long inwardId) {
        return purchaseInvoiceRepository.findByMaterialInwardId(inwardId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<PurchaseInvoiceResponse> getByPurchaseOrderId(Long poId) {
        return purchaseInvoiceRepository.findByPurchaseOrderId(poId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<PurchaseInvoiceResponse> getUnlinkedInvoices(Long vendorId) {
        return purchaseInvoiceRepository.findByVendorIdAndMaterialInwardIsNull(vendorId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<PurchaseInvoiceSummary> getAll(
            Long vendorId, Boolean verified, LocalDate from, LocalDate to,
            Boolean hasInward, Pageable pageable) {
        Page<PurchaseInvoice> page = purchaseInvoiceRepository.findAllFiltered(vendorId, verified, from, to, hasInward, pageable);
        return PageResponse.from(page, this::toSummary);
    }

    @Transactional(readOnly = true)
    public List<PurchaseInvoiceResponse> getGstReport(LocalDate from, LocalDate to) {
        return purchaseInvoiceRepository.findForGstReport(from, to)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<PurchaseInvoice> getInvoicesForExport(LocalDate from, LocalDate to) {
        return purchaseInvoiceRepository.findForGstReport(from, to);
    }

    private PurchaseInvoiceResponse toResponse(PurchaseInvoice pi) {
        return new PurchaseInvoiceResponse(
            pi.getId(),
            pi.getVendorInvoiceNumber(),
            pi.getVendorInvoiceDate(),
            pi.getInvoiceAmount(),
            pi.getVendor().getId(),
            pi.getVendor().getName(),
            pi.getVendor().getGstNumber(),
            pi.getPurchaseOrder() != null ? pi.getPurchaseOrder().getId() : null,
            pi.getPurchaseOrder() != null ? pi.getPurchaseOrder().getPoNumber() : null,
            pi.getMaterialInward() != null ? pi.getMaterialInward().getId() : null,
            pi.getMaterialInward() != null ? pi.getMaterialInward().getInwardNumber() : null,
            pi.getSource(),
            pi.getIsVerified(),
            pi.getVerifiedAt(),
            pi.getMaterialInward() != null ? pi.getMaterialInward().getTotalAmount() : null,
            pi.getAmountMismatch(),
            pi.hasAmountMismatch(),
            pi.getRemarks(),
            pi.getCreatedAt()
        );
    }

    private PurchaseInvoiceSummary toSummary(PurchaseInvoice pi) {
        return new PurchaseInvoiceSummary(
            pi.getId(),
            pi.getVendorInvoiceNumber(),
            pi.getVendorInvoiceDate(),
            pi.getInvoiceAmount(),
            pi.getVendor().getName(),
            pi.getPurchaseOrder() != null ? pi.getPurchaseOrder().getPoNumber() : null,
            pi.getMaterialInward() != null ? pi.getMaterialInward().getInwardNumber() : null,
            pi.getSource(),
            pi.getIsVerified(),
            pi.hasAmountMismatch()
        );
    }
}
