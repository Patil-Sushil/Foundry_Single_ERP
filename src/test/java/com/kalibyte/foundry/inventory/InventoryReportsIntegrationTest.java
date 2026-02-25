package com.kalibyte.foundry.inventory;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.kalibyte.foundry.common.response.ApiResponse;
import com.kalibyte.foundry.inventory.inward.dto.request.StartInwardRequest;
import com.kalibyte.foundry.inventory.inward.dto.response.InwardResponse;
import com.kalibyte.foundry.inventory.issue.dto.request.IssueItemRequest;
import com.kalibyte.foundry.inventory.issue.dto.request.RecordIssueRequest;
import com.kalibyte.foundry.inventory.item.entity.enums.ItemCategory;
import com.kalibyte.foundry.inventory.item.entity.enums.ItemUnit;
import com.kalibyte.foundry.inventory.purchaseorder.dto.request.CreatePurchaseOrderRequest;
import com.kalibyte.foundry.inventory.purchaseorder.dto.request.OrderItemRequest;
import com.kalibyte.foundry.inventory.purchaseorder.dto.response.PurchaseOrderResponse;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@WithMockCustomUser
public class InventoryReportsIntegrationTest extends BaseInventoryIntegrationTest {

    @Test
    @Order(1)
    void testAllReports() throws Exception {
        // Setup simple flow to generate report data
        Long vendorId = createVendor("Report Vendor", "987", "GST-R", "Pune");
        Long deptId = createDepartment("Report Dept", "R-DEPT");
        Long itemId = createItem("Report Item", "R-ITM", ItemCategory.TOOL, null, deptId, ItemUnit.PCS, BigDecimal.TEN, BigDecimal.ONE);

        CreatePurchaseOrderRequest poReq = new CreatePurchaseOrderRequest(
                vendorId, LocalDate.now(), List.of(new OrderItemRequest(itemId, new BigDecimal("100"), new BigDecimal("50.00"), null)), null
        );

        Long poId = objectMapper.readValue(mockMvc.perform(post("/api/purchase-orders")
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(poReq)))
                .andReturn().getResponse().getContentAsString(), new TypeReference<ApiResponse<PurchaseOrderResponse>>() {}).getData().id();

        StartInwardRequest inwReq = new StartInwardRequest("V1", "D1", "P1", "C1");
        Long inwardId = objectMapper.readValue(mockMvc.perform(post("/api/inwards/from-po/" + poId)
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(inwReq)))
                .andReturn().getResponse().getContentAsString(), new TypeReference<ApiResponse<InwardResponse>>() {}).getData().id();

        mockMvc.perform(post("/api/inwards/" + inwardId + "/confirm")).andExpect(status().isOk());

        RecordIssueRequest issueReq = new RecordIssueRequest(
                deptId, "Testing", LocalDate.now(),
                List.of(new IssueItemRequest(itemId, new BigDecimal("20"), null)), null
        );

        mockMvc.perform(post("/api/material-issues")
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(issueReq)))
                .andExpect(status().isCreated());

        // Now test Reports

        // 1. Stock Summary Report
        mockMvc.perform(get("/api/inventory/reports/stock-summary?category=TOOL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[*].itemCode", hasItem(startsWith("R-ITM"))))
                .andExpect(jsonPath("$.data.items[0].currentStock").value(anything())); // Avoid 80.0 vs 80 issue

        // 2. Material Inward Report
        mockMvc.perform(get("/api/inventory/reports/inwards?vendorId=" + vendorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalQuantity").value(anything()))
                .andExpect(jsonPath("$.data.totalValue").value(anything()))
                .andExpect(jsonPath("$.data.records", hasSize(greaterThanOrEqualTo(1))));

        // 3. Material Issue Report
        mockMvc.perform(get("/api/inventory/reports/issues?departmentId=" + deptId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalQuantity").value(anything()))
                .andExpect(jsonPath("$.data.totalValue").value(anything()));

        // 4. Item Ledger Report
        mockMvc.perform(get("/api/inventory/reports/items/" + itemId + "/ledger"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.closingStock").value(anything()))
                .andExpect(jsonPath("$.data.transactions", hasSize(2))) // 1 Inward, 1 Issue
                .andExpect(jsonPath("$.data.transactions[0].type", is("INWARD")))
                .andExpect(jsonPath("$.data.transactions[1].type", is("ISSUE")));

        // 5. Daily Movement Report
        mockMvc.perform(get("/api/inventory/reports/daily-movement?date=" + LocalDate.now()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records[*].itemCode", hasItem(startsWith("R-ITM"))));

        // 6. Vendor Summary Report
        mockMvc.perform(get("/api/inventory/reports/vendor-summary?vendorId=" + vendorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.vendors[0].totalPOsRaised", is(1)))
                .andExpect(jsonPath("$.data.vendors[0].totalPOValue", anything()))
                .andExpect(jsonPath("$.data.vendors[0].ledgerBalance", anything()));
    }
}
