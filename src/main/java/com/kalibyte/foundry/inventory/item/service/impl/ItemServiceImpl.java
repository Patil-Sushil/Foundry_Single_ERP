package com.kalibyte.foundry.inventory.item.service.impl;

import com.kalibyte.foundry.common.exception.ResourceNotFoundException;
import com.kalibyte.foundry.inventory.department.entity.Department;
import com.kalibyte.foundry.inventory.department.repository.DepartmentRepository;
import com.kalibyte.foundry.inventory.item.dto.request.CreateItemRequest;
import com.kalibyte.foundry.inventory.item.dto.request.UpdateItemRequest;
import com.kalibyte.foundry.inventory.item.dto.response.ItemResponse;
import com.kalibyte.foundry.inventory.item.dto.response.ItemSummary;
import com.kalibyte.foundry.inventory.item.entity.Item;
import com.kalibyte.foundry.inventory.item.entity.enums.ItemCategory;
import com.kalibyte.foundry.inventory.item.mapper.ItemMapper;
import com.kalibyte.foundry.inventory.item.repository.ItemRepository;
import com.kalibyte.foundry.inventory.item.service.ItemService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final DepartmentRepository departmentRepository;
    private final ItemMapper itemMapper;

    public ItemServiceImpl(ItemRepository itemRepository,
                       DepartmentRepository departmentRepository, 
                       ItemMapper itemMapper) {
        this.itemRepository = itemRepository;
        this.departmentRepository = departmentRepository;
        this.itemMapper = itemMapper;
    }

    @Transactional
    public ItemResponse create(CreateItemRequest request) {
        Department department = null;
        if (request.departmentId() != null) {
            department = departmentRepository.findById(request.departmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + request.departmentId()));
        }

        Item item = itemMapper.toItem(request);
        item.setDepartment(department);

        return itemMapper.toResponse(itemRepository.save(item));
    }

    @Transactional
    public ItemResponse update(Long id, UpdateItemRequest request) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with id: " + id));

        Department department = null;
        if (request.departmentId() != null) {
            department = departmentRepository.findById(request.departmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + request.departmentId()));
        }
        item.setName(request.name());
        item.setCode(request.code());
        item.setDescription(request.description());
        item.setCategory(request.category());
        item.setSubCategory(request.subCategory());
        item.setDepartment(department);
        item.setUnit(request.unit());
        item.setReorderLevel(request.reorderLevel() != null ? request.reorderLevel() : BigDecimal.ZERO);
        item.setMinStockLevel(request.minStockLevel() != null ? request.minStockLevel() : BigDecimal.ZERO);
        item.setLocation(request.location());
        item.setHsnCode(request.hsnCode());
        item.setGstRate(request.gstRate() != null ? request.gstRate() : new BigDecimal("18.00"));

        if (request.isActive() != null) {
            item.setIsActive(request.isActive());
        }
        
        if (request.isScrap() != null) {
            item.setIsScrap(request.isScrap());
        }

        return itemMapper.toResponse(itemRepository.save(item));
    }

    @Transactional(readOnly = true)
    public ItemResponse getById(Long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with id: " + id));
        return itemMapper.toResponse(item);
    }

    @Transactional(readOnly = true)
    public Page<ItemResponse> getAll(ItemCategory category, Boolean isActive, Boolean isScrap, Pageable pageable) {
        var spec = com.kalibyte.foundry.inventory.item.repository.ItemSpecification.filter(category, isActive, isScrap, null);
        return itemRepository.findAll(spec, pageable).map(itemMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public List<ItemSummary> search(String query, Boolean isScrap) {
        Pageable limit = PageRequest.of(0, 10);
        var spec = com.kalibyte.foundry.inventory.item.repository.ItemSpecification.filter(null, true, isScrap, query);
        return itemRepository.findAll(spec, limit)
                .stream()
                .map(itemMapper::toSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ItemResponse> getLowStockItems() {
        return itemRepository.findByCurrentStockLessThanEqualAndIsActiveTrue()
                .stream()
                .map(itemMapper::toResponse)
                .toList();
    }

    @Transactional
    @Modifying
    public ItemResponse toggleStatus(Long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with id: " + id));

        item.setIsActive(!item.getIsActive());
        itemRepository.save(item);
        return itemMapper.toResponse(item);
    }
}
