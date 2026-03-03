package com.kalibyte.foundry.inventory.issue.service;

import com.kalibyte.foundry.common.exception.ResourceNotFoundException;
import com.kalibyte.foundry.inventory.common.IssueNumberGenerator;
import com.kalibyte.foundry.inventory.department.entity.Department;
import com.kalibyte.foundry.inventory.department.repository.DepartmentRepository;
import com.kalibyte.foundry.inventory.issue.dto.request.IssueItemRequest;
import com.kalibyte.foundry.inventory.issue.dto.request.RecordIssueRequest;
import com.kalibyte.foundry.inventory.issue.dto.response.*;
import com.kalibyte.foundry.inventory.issue.entity.IssuedItem;
import com.kalibyte.foundry.inventory.issue.entity.MaterialIssue;
import com.kalibyte.foundry.inventory.issue.repository.MaterialIssueRepository;
import com.kalibyte.foundry.inventory.item.entity.Item;
import com.kalibyte.foundry.inventory.item.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MaterialIssueService {

    private final MaterialIssueRepository materialIssueRepository;
    private final DepartmentRepository departmentRepository;
    private final ItemRepository itemRepository;
    private final IssueNumberGenerator issueNumberGenerator;

    @Transactional
    public MaterialIssueResponse recordIssue(RecordIssueRequest request) {
        Department department = departmentRepository.findById(request.departmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + request.departmentId()));

        MaterialIssue issue = MaterialIssue.builder()
                .issueNumber(issueNumberGenerator.generate())
                .department(department)
                .issuedByUserId(com.kalibyte.foundry.common.util.SecurityUtils.getCurrentUserId())
                .issueDate(request.issueDate() != null ? request.issueDate() : LocalDate.now())
                .purpose(request.purpose())
                .notes(request.notes())
                .build();

        for (IssueItemRequest itemRequest : request.items()) {
            Item item = itemRepository.findById(itemRequest.itemId())
                    .orElseThrow(() -> new ResourceNotFoundException("Item not found with id: " + itemRequest.itemId()));

            // Domain logic validation & stock update
            item.issueStock(itemRequest.issuedQuantity());
            
            // Capture rate at time of issue
            BigDecimal rateAtIssue = item.getAvgRate();

            IssuedItem issuedItem = IssuedItem.builder()
                    .item(item)
                    .issuedQuantity(itemRequest.issuedQuantity())
                    .unitRate(rateAtIssue)
                    .notes(itemRequest.notes())
                    .build();
            
            issue.addIssuedItem(issuedItem);
            itemRepository.save(item); // Update stock
        }

        return toResponse(materialIssueRepository.save(issue));
    }

    @Transactional(readOnly = true)
    public MaterialIssueResponse getById(Long id) {
        MaterialIssue issue = materialIssueRepository.findWithItems(id)
                .orElseThrow(() -> new ResourceNotFoundException("Material Issue not found with id: " + id));
        return toResponse(issue);
    }


    @Transactional(readOnly = true)
    public Page<MaterialIssueSummary> getAll(Long departmentId, LocalDate from, LocalDate to, Pageable pageable) {
        departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + departmentId));
        return materialIssueRepository.findAllFiltered(departmentId, from, to, pageable)
                .map(this::toSummary);
    }

    @Transactional(readOnly = true)
    public DepartmentConsumptionReport getConsumptionReport(Long departmentId, LocalDate from, LocalDate to) {
        departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + departmentId));

        List<MaterialIssue> issues = materialIssueRepository.findByDepartmentAndDateRange(departmentId, from, to);

        Map<Long, ConsumptionDetail> map = new HashMap<>();

        for (MaterialIssue issue : issues) {
            for (IssuedItem ii : issue.getIssuedItems()) {
                Long itemId = ii.getItem().getId();
                ConsumptionDetail detail = map.getOrDefault(itemId, new ConsumptionDetail(
                        ii.getItem().getName(),
                        ii.getItem().getCode(),
                        ii.getItem().getUnit().name(),
                        BigDecimal.ZERO,
                        BigDecimal.ZERO
                ));

                BigDecimal newQty = detail.totalQuantity().add(ii.getIssuedQuantity());
                BigDecimal newVal = detail.totalValue().add(ii.getAmount());

                map.put(itemId, new ConsumptionDetail(
                        detail.itemName(),
                        detail.itemCode(),
                        detail.unit(),
                        newQty,
                        newVal
                ));
            }
        }

        List<ConsumptionDetail> items = new ArrayList<>(map.values());
        BigDecimal grandTotal = items.stream()
                .map(ConsumptionDetail::totalValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new DepartmentConsumptionReport(departmentId, from, to, items, grandTotal);
    }

    private MaterialIssueResponse toResponse(MaterialIssue issue) {
        List<IssuedItemDetail> items = issue.getIssuedItems().stream()
                .map(ii -> new IssuedItemDetail(
                        ii.getId(),
                        ii.getItem().getName(),
                        ii.getItem().getCode(),
                        ii.getItem().getUnit().name(),
                        ii.getIssuedQuantity(),
                        ii.getUnitRate(),
                        ii.getAmount(),
                        ii.getNotes()
                ))
                .toList();

        return new MaterialIssueResponse(
                issue.getId(),
                issue.getIssueNumber(),
                issue.getDepartment().getName(),
                issue.getPurpose(),
                issue.getIssueDate(),
                items,
                issue.getTotalValue(),
                issue.getCreatedAt()
        );
    }

    private MaterialIssueSummary toSummary(MaterialIssue issue) {
        return new MaterialIssueSummary(
                issue.getId(),
                issue.getIssueNumber(),
                issue.getDepartment().getName(),
                issue.getPurpose(),
                issue.getIssueDate(),
                issue.getIssuedItems().size(),
                issue.getTotalValue()
        );
    }
}
