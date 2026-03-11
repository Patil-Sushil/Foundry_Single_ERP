package com.kalibyte.foundry.inventory.item.service;

import com.kalibyte.foundry.common.exception.ResourceNotFoundException;
import com.kalibyte.foundry.inventory.department.entity.Department;
import com.kalibyte.foundry.inventory.department.repository.DepartmentRepository;
import com.kalibyte.foundry.inventory.item.dto.request.CreateItemRequest;
import com.kalibyte.foundry.inventory.item.dto.request.UpdateItemRequest;
import com.kalibyte.foundry.inventory.item.dto.response.ItemResponse;
import com.kalibyte.foundry.inventory.item.dto.response.ItemSummary;
import com.kalibyte.foundry.inventory.item.entity.Item;
import com.kalibyte.foundry.inventory.item.entity.enums.ItemCategory;
import com.kalibyte.foundry.inventory.item.repository.ItemRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ItemService {

    private final ItemRepository itemRepository;
    private final DepartmentRepository departmentRepository;

	public ItemService(ItemRepository itemRepository, DepartmentRepository departmentRepository) {
		this.itemRepository = itemRepository;
		this.departmentRepository = departmentRepository;
	}

	@Transactional
    public ItemResponse create(CreateItemRequest request) {
        Department department = null;
        if (request.departmentId() != null) {
            department = departmentRepository.findById(request.departmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + request.departmentId()));
        }

        Item item = Item.builder()
                .name(request.name())
                .code(request.code())
                .description(request.description())
                .category(request.category())
                .subCategory(request.subCategory())
                .department(department)
                .unit(request.unit())
                .reorderLevel(request.reorderLevel() != null ? request.reorderLevel() : BigDecimal.ZERO)
                .minStockLevel(request.minStockLevel() != null ? request.minStockLevel() : BigDecimal.ZERO)
                .location(request.location())
                .hsnCode(request.hsnCode())
                .gstRate(request.gstRate() != null ? request.gstRate() : new BigDecimal("18.00"))
                .currentStock(BigDecimal.ZERO)
                .avgRate(BigDecimal.ZERO)
                .lastPurchaseRate(BigDecimal.ZERO)
                .isActive(true)
                .build();

        return toResponse(itemRepository.save(item));
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

        return toResponse(itemRepository.save(item));
    }

    @Transactional(readOnly = true)
    public ItemResponse getById(Long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with id: " + id));
        return toResponse(item);
    }

    @Transactional(readOnly = true)
    public Page<ItemResponse> getAll(ItemCategory category, Boolean isActive, Pageable pageable) {
        Page<Item> items;
        if (category != null) {
            boolean active = isActive != null ? isActive : true; 
            items = itemRepository.findByCategoryAndIsActive(category, active, pageable);
        } else if (isActive != null) {
            items = itemRepository.findByIsActive(isActive, pageable);
        } else {
            items = itemRepository.findAll(pageable);
        }
        return items.map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<ItemSummary> search(String query) {
        Pageable limit = PageRequest.of(0, 10);
        return itemRepository.findByNameOrCode(query, limit)
                .stream()
                .map(i -> new ItemSummary(
                        i.getId(),
                        i.getName(),
                        i.getCode(),
                        i.getUnit(),
                        i.getCurrentStock(),
                        i.getAvgRate()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ItemResponse> getLowStockItems() {
        return itemRepository.findByCurrentStockLessThanEqualAndIsActiveTrue()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private ItemResponse toResponse(Item item) {
        return new ItemResponse(
                item.getId(),
                item.getName(),
                item.getCode(),
                item.getDescription(),
                item.getCategory(),
                item.getSubCategory(),
                item.getDepartment() != null ? item.getDepartment().getName() : null,
                item.getUnit(),
                item.getCurrentStock(),
                item.getReorderLevel(),
                item.getMinStockLevel(),
                item.getLocation(),
                item.getLastPurchaseRate(),
                item.getAvgRate(),
                item.getStockValue(),
                item.getStockStatus(),
                item.getHsnCode(),
                item.getGstRate(),
                item.getIsActive(),
                item.getCreatedAt()
        );
    }

    @Transactional
    @Modifying
	public ItemResponse toggleStatus(Long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with id: " + id));

        item.setIsActive(!item.getIsActive());
        itemRepository.save(item);
        return toResponse(item);
	}
}
