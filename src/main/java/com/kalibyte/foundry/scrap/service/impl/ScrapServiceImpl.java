package com.kalibyte.foundry.scrap.service.impl;

import com.kalibyte.foundry.common.exception.BusinessException;
import com.kalibyte.foundry.common.exception.ResourceNotFoundException;
import com.kalibyte.foundry.inventory.department.entity.Department;
import com.kalibyte.foundry.inventory.department.repository.DepartmentRepository;
import com.kalibyte.foundry.inventory.inward.dto.request.InternalReturnRequest;
import com.kalibyte.foundry.inventory.inward.service.MaterialInwardService;
import com.kalibyte.foundry.inventory.item.entity.Item;
import com.kalibyte.foundry.inventory.item.entity.enums.ItemCategory;
import com.kalibyte.foundry.inventory.item.entity.enums.ItemSubCategory;
import com.kalibyte.foundry.inventory.item.entity.enums.ItemUnit;
import com.kalibyte.foundry.inventory.item.repository.ItemRepository;
import com.kalibyte.foundry.qa.common.QaNumberGenerator;
import com.kalibyte.foundry.scrap.dto.request.ScrapEntryRequest;
import com.kalibyte.foundry.scrap.dto.request.ScrapItemRequest;
import com.kalibyte.foundry.scrap.dto.response.ScrapEntryResponse;
import com.kalibyte.foundry.scrap.entity.ScrapEntry;
import com.kalibyte.foundry.scrap.entity.ScrapItem;
import com.kalibyte.foundry.scrap.enums.ApprovalDecision;
import com.kalibyte.foundry.scrap.enums.ScrapStatus;
import com.kalibyte.foundry.scrap.mapper.ScrapMapper;
import com.kalibyte.foundry.scrap.repository.ScrapEntryRepository;
import com.kalibyte.foundry.scrap.service.ScrapService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScrapServiceImpl implements ScrapService {

    private final ScrapEntryRepository scrapEntryRepository;
    private final ScrapMapper scrapMapper;
    private final MaterialInwardService materialInwardService;
    private final ItemRepository itemRepository;
    private final DepartmentRepository departmentRepository;
    private final QaNumberGenerator numberGenerator;

    @Override
    @Transactional(readOnly = true)
    public List<ScrapEntryResponse> getAll() {
        return scrapMapper.toResponseList(scrapEntryRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ScrapEntryResponse> getByStatus(ScrapStatus status) {
        return scrapMapper.toResponseList(scrapEntryRepository.findByStatus(status));
    }

    @Override
    @Transactional(readOnly = true)
    public ScrapEntryResponse getById(Long id) {
        ScrapEntry entry = scrapEntryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Scrap entry not found: " + id));
        return scrapMapper.toResponse(entry);
    }

    @Override
    @Transactional
    public ScrapEntryResponse createScrapEntry(ScrapEntryRequest request) {
        ScrapEntry entry = scrapMapper.toEntity(request);
        
        if (entry.getScrapNumber() == null || entry.getScrapNumber().isEmpty()) {
            entry.setScrapNumber(numberGenerator.generateScrapNumber());
        }

        if (entry.getScrapDate() == null) {
            entry.setScrapDate(java.time.LocalDate.now());
        }

        if (request.getInitialStatus() != null) {
            entry.setStatus(request.getInitialStatus());
        }

        if (request.getScrapItems() != null) {
            for (ScrapItemRequest itemReq : request.getScrapItems()) {
                ScrapItem item = ScrapItem.builder()
                        .itemName(itemReq.getItemName())
                        .itemCode(itemReq.getItemCode())
                        .itemId(itemReq.getItemId())
                        .grade(itemReq.getGrade())
                        .weight(itemReq.getWeight())
                        .quantity(itemReq.getQuantity())
                        .scrapType(itemReq.getScrapType())
                        .recyclability(itemReq.getRecyclability())
                        .inspectionDefectId(itemReq.getInspectionDefectId())
                        .build();
                entry.addScrapItem(item);
            }
        }

        return scrapMapper.toResponse(scrapEntryRepository.save(entry));
    }

    @Override
    @Transactional
    public ScrapEntryResponse verifyScrap(Long id, String verifiedBy, String notes) {
        ScrapEntry entry = scrapEntryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Scrap entry not found"));
        
        entry.setStatus(ScrapStatus.VERIFIED);
        entry.setVerifiedBy(verifiedBy);
        entry.setVerifiedAt(LocalDateTime.now());
        entry.setVerificationNotes(notes);
        
        return scrapMapper.toResponse(scrapEntryRepository.save(entry));
    }

    @Override
    @Transactional
    public ScrapEntryResponse approveScrap(Long id, String approvedBy, ApprovalDecision decision, String notes, String finalGrade) {
        ScrapEntry entry = scrapEntryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Scrap entry not found"));

        if (entry.getStatus() == ScrapStatus.RETURNED_TO_INVENTORY || 
            entry.getStatus() == ScrapStatus.APPROVED_FOR_RETURN ||
            entry.getStatus() == ScrapStatus.REJECTED_FOR_RETURN) {
            throw new BusinessException("Scrap entry has already been processed and cannot be approved again.");
        }
        
        entry.setApprovedBy(approvedBy);
        entry.setApprovedAt(LocalDateTime.now());
        entry.setApprovalDecision(decision);
        entry.setApprovalNotes(notes);
        entry.setFinalGrade(finalGrade);
        
        if (decision == ApprovalDecision.APPROVE_REMELT || decision == ApprovalDecision.APPROVE_MIXED) {
            entry.setStatus(ScrapStatus.APPROVED_FOR_RETURN);
            // Auto-trigger inventory inward
            createInventoryInward(entry);
        } else {
            entry.setStatus(ScrapStatus.REJECTED_FOR_RETURN);
        }
        
        return scrapMapper.toResponse(scrapEntryRepository.save(entry));
    }

    private void createInventoryInward(ScrapEntry entry) {
        List<InternalReturnRequest.InternalReturnItemRequest> items = new ArrayList<>();
        
        for (ScrapItem scrapItem : entry.getScrapItems()) {
            Long itemId = scrapItem.getItemId();
            
            if (itemId == null && scrapItem.getGrade() != null) {
                Item item = getOrCreateScrapItem(scrapItem.getGrade());
                itemId = item.getId();
                scrapItem.setItemId(itemId);
                scrapItem.setItemCode(item.getCode());
                scrapItem.setInventoryItem(item);
            }
            
            if (itemId != null) {
                items.add(new InternalReturnRequest.InternalReturnItemRequest(
                        itemId,
                        scrapItem.getWeight(),
                        BigDecimal.ZERO,
                        "Scrap return: " + entry.getScrapSource()
                ));
            }
        }

        InternalReturnRequest request = InternalReturnRequest.builder()
                .scrapEntryId(entry.getId())
                .remarks("Automatic inward from scrap entry: " + entry.getScrapNumber())
                .returnDate(entry.getScrapDate())
                .items(items)
                .build();

        materialInwardService.createInternalReturnInward(request);
        
        entry.setStatus(ScrapStatus.RETURNED_TO_INVENTORY);
        entry.setInwardConfirmedAt(LocalDateTime.now());
        entry.setInwardConfirmedBy("SYSTEM");
    }

    private Item getOrCreateScrapItem(String grade) {
        return itemRepository.findByIsScrapTrueAndGrade(grade)
                .orElseGet(() -> {
                    Item newItem = Item.builder()
                            .name(grade + " Process Scrap")
                            .code("SCR-" + grade)
                            .category(ItemCategory.RAW_MATERIAL)
                            .subCategory(ItemSubCategory.FERROUS)
                            .department(getFurnaceDepartment())
                            .unit(ItemUnit.KG)
                            .reorderLevel(BigDecimal.ZERO)
                            .minStockLevel(BigDecimal.ZERO)
                            .currentStock(BigDecimal.ZERO)
                            .avgRate(BigDecimal.ZERO)
                            .isActive(true)
                            .isScrap(true)
                            .grade(grade)
                            .build();
                    return itemRepository.save(newItem);
                });
    }

    private Department getFurnaceDepartment() {
        return departmentRepository.findByName("FURNACE")
                .orElseGet(() -> {
                    Department dept = new Department();
                    dept.setName("FURNACE");
                    dept.setCode("FUR");
                    return departmentRepository.save(dept);
                });
    }
}
