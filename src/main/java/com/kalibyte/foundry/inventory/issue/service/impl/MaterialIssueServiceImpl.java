package com.kalibyte.foundry.inventory.issue.service.impl;

import com.kalibyte.foundry.common.exception.ResourceNotFoundException;
import com.kalibyte.foundry.common.response.PageResponse;
import com.kalibyte.foundry.inventory.common.IssueNumberGenerator;
import com.kalibyte.foundry.inventory.department.entity.Department;
import com.kalibyte.foundry.inventory.department.repository.DepartmentRepository;
import com.kalibyte.foundry.inventory.issue.dto.request.IssueItemRequest;
import com.kalibyte.foundry.inventory.issue.dto.request.RecordIssueRequest;
import com.kalibyte.foundry.inventory.issue.dto.response.DepartmentConsumptionReport;
import com.kalibyte.foundry.inventory.issue.dto.response.MaterialIssueResponse;
import com.kalibyte.foundry.inventory.issue.dto.response.MaterialIssueSummary;
import com.kalibyte.foundry.inventory.issue.entity.IssuedItem;
import com.kalibyte.foundry.inventory.issue.entity.MaterialIssue;
import com.kalibyte.foundry.inventory.issue.mapper.MaterialIssueMapper;
import com.kalibyte.foundry.inventory.issue.repository.MaterialIssueRepository;
import com.kalibyte.foundry.inventory.issue.service.MaterialIssueService;
import com.kalibyte.foundry.inventory.item.entity.Item;
import com.kalibyte.foundry.inventory.item.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MaterialIssueServiceImpl implements MaterialIssueService {

    private final MaterialIssueRepository materialIssueRepository;
    private final DepartmentRepository departmentRepository;
    private final ItemRepository itemRepository;
    private final IssueNumberGenerator issueNumberGenerator;
    private final MaterialIssueMapper materialIssueMapper;

    @Override
    @Transactional
    @CacheEvict(value = "items", allEntries = true)
    public MaterialIssueResponse recordIssue(RecordIssueRequest request) {
        Department department = departmentRepository.findById(request.departmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + request.departmentId()));

        MaterialIssue issue = materialIssueMapper.toEntity(request);
        issue.setIssueNumber(issueNumberGenerator.generate());
        issue.setDepartment(department);
        issue.setIssuedByUserId(com.kalibyte.foundry.common.util.SecurityUtils.getCurrentUserId());

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

        return materialIssueMapper.toResponse(materialIssueRepository.save(issue));
    }

    @Override
    @Transactional(readOnly = true)
    public MaterialIssueResponse getById(Long id) {
        MaterialIssue issue = materialIssueRepository.findWithItems(id)
                .orElseThrow(() -> new ResourceNotFoundException("Material Issue not found with id: " + id));
        return materialIssueMapper.toResponse(issue);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<MaterialIssueSummary> getAll(Long departmentId, LocalDate from, LocalDate to, Pageable pageable) {
        if (departmentId != null) {
            departmentRepository.findById(departmentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + departmentId));
        }
        return PageResponse.from(materialIssueRepository.findAllFiltered(departmentId, from, to, pageable), materialIssueMapper::toSummary);
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentConsumptionReport getConsumptionReport(Long departmentId, LocalDate from, LocalDate to) {
        departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + departmentId));

        List<MaterialIssue> issues = materialIssueRepository.findByDepartmentAndDateRange(departmentId, from, to);

        return materialIssueMapper.toConsumptionReport(departmentId, from, to, issues);
    }
}
