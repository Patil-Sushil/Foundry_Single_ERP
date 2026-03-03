package com.kalibyte.foundry.inventory;

import com.kalibyte.foundry.inventory.department.dto.request.DepartmentRequest;
import com.kalibyte.foundry.inventory.item.dto.request.CreateItemRequest;
import com.kalibyte.foundry.inventory.item.entity.enums.ItemCategory;
import com.kalibyte.foundry.inventory.item.entity.enums.ItemUnit;
import com.kalibyte.foundry.inventory.vendor.dto.request.CreateVendorRequest;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@WithMockCustomUser
public class MasterDataIntegrationTest extends BaseInventoryIntegrationTest {

    @Test
    @Order(1)
    void testDepartmentCRUD_Validations() throws Exception {
        String uniqueCode = "D1" + UUID.randomUUID().toString().substring(0, 5);
        DepartmentRequest invalidReq = new DepartmentRequest();
        invalidReq.setCode(uniqueCode);

        invalidReq.setName("Department 1 " + uniqueCode);
        mockMvc.perform(post("/api/departments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.name", is("Department 1")));

        // Duplicate code
        mockMvc.perform(post("/api/departments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidReq)))
                .andExpect(status().isBadRequest()) // Handled by BusinessException or custom logic? It throws DuplicateDepartmentException
                // Wait, DuplicateDepartmentException might not be handled in GlobalExceptionHandler unless it extends Exception and handled generically, or there's a specific handler.
                // It is not in GlobalExceptionHandler, so it returns 500 INTERNAL_SERVER_ERROR
                .andExpect(status().isInternalServerError());

        // Get All
        mockMvc.perform(get("/api/departments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @Order(2)
    void testVendorCRUD_Validations() throws Exception {
        // Validation: Blank name
        CreateVendorRequest invalidReq = new CreateVendorRequest("", "123", "GST", "Addr");
        mockMvc.perform(post("/api/vendors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", containsString("Validation failed")));

        // Valid Creation
        Long vendorId = createVendor("Vendor A", "9999999999", "27GST", "Pune");

        // Get By Id
        mockMvc.perform(get("/api/vendors/" + vendorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name", is("Vendor A")))
                .andExpect(jsonPath("$.data.isActive", is(true)));

        // Deactivate
        mockMvc.perform(delete("/api/vendors/" + vendorId))
                .andExpect(status().isNoContent());

        // Get By Id (verify deactivated)
        mockMvc.perform(get("/api/vendors/" + vendorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isActive", is(false)));
    }

    @Test
    @Order(3)
    void testItemCRUD_Validations() throws Exception {
        Long deptId = createDepartment("Stores", "ST01");

        // Validation: Missing name
        CreateItemRequest invalidReq = new CreateItemRequest(
                "", "ITM-01", "Desc", ItemCategory.RAW_MATERIAL, null, deptId,
                ItemUnit.KG, BigDecimal.TEN, BigDecimal.ONE, "Loc", "HSN", BigDecimal.TEN
        );
        mockMvc.perform(post("/api/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Name is required")));

        // Validation: Negative Reorder Level
        CreateItemRequest invalidReq2 = new CreateItemRequest(
                "Item 1", "ITM-01", "Desc", ItemCategory.RAW_MATERIAL, null, deptId,
                ItemUnit.KG, new BigDecimal("-1"), BigDecimal.ONE, "Loc", "HSN", BigDecimal.TEN
        );
        mockMvc.perform(post("/api/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidReq2)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Reorder level must be >= 0")));

        // Valid creation
        Long itemId = createItem("Pig Iron", "PI-1", ItemCategory.RAW_MATERIAL, null, deptId, ItemUnit.KG, BigDecimal.valueOf(100), BigDecimal.TEN);

        // Get All Low Stock (Currently 0 stock, so it should be returned because stock <= reorderLevel)
        mockMvc.perform(get("/api/items/low-stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[*].id", hasItem(itemId.intValue())));

        // Search
        mockMvc.perform(get("/api/items/search?q=Pig"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[*].name", hasItem("Pig Iron")));
    }
}
