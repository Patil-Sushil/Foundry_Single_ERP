package com.kalibyte.foundry.inventory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kalibyte.foundry.common.response.ApiResponse;
import com.kalibyte.foundry.inventory.department.dto.request.DepartmentRequest;
import com.kalibyte.foundry.inventory.department.dto.response.DepartmentResponse;
import com.kalibyte.foundry.inventory.vendor.dto.request.CreateVendorRequest;
import com.kalibyte.foundry.inventory.vendor.dto.response.VendorResponse;
import com.kalibyte.foundry.inventory.item.dto.request.CreateItemRequest;
import com.kalibyte.foundry.inventory.item.dto.response.ItemResponse;
import com.kalibyte.foundry.inventory.item.entity.enums.ItemCategory;
import com.kalibyte.foundry.inventory.item.entity.enums.ItemSubCategory;
import com.kalibyte.foundry.inventory.item.entity.enums.ItemUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"test", "seed"})
public abstract class BaseInventoryIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    protected Long createDepartment(String name, String code) throws Exception {
        String uniqueCode = code + UUID.randomUUID().toString().substring(0, 5);
        DepartmentRequest request = new DepartmentRequest();
        request.setName(name + " " + uniqueCode);
        request.setCode(uniqueCode);

        MvcResult result = mockMvc.perform(post("/api/departments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        ApiResponse<DepartmentResponse> response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<ApiResponse<DepartmentResponse>>() {}
        );
        return response.getData().getId();
    }

    protected Long createVendor(String name, String phone, String gst, String address) throws Exception {
        String uniqueName = name + " " + UUID.randomUUID().toString().substring(0, 5);
        CreateVendorRequest request = new CreateVendorRequest(uniqueName, phone, gst, address);

        MvcResult result = mockMvc.perform(post("/api/vendors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        ApiResponse<VendorResponse> response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<ApiResponse<VendorResponse>>() {}
        );
        return response.getData().id();
    }

    protected Long createItem(String name, String code, ItemCategory category, ItemSubCategory subCategory,
                              Long departmentId, ItemUnit unit, BigDecimal reorder, BigDecimal minStock) throws Exception {
        String uniqueCode = code + UUID.randomUUID().toString().substring(0, 5);
        CreateItemRequest request = new CreateItemRequest(
                name + " " + uniqueCode, uniqueCode, "Test Desc", category, subCategory, departmentId,
                unit, reorder, minStock, "A-1", "HSN123", new BigDecimal("18.00")
        );

        MvcResult result = mockMvc.perform(post("/api/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        ApiResponse<ItemResponse> response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<ApiResponse<ItemResponse>>() {}
        );
        return response.getData().id();
    }
}
