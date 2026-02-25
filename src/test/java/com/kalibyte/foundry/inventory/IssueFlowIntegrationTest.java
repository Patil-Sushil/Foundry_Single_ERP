package com.kalibyte.foundry.inventory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.kalibyte.foundry.common.response.ApiResponse;
import com.kalibyte.foundry.inventory.inward.dto.request.StartInwardRequest;
import com.kalibyte.foundry.inventory.inward.dto.response.InwardResponse;
import com.kalibyte.foundry.inventory.issue.dto.request.IssueItemRequest;
import com.kalibyte.foundry.inventory.issue.dto.request.RecordIssueRequest;
import com.kalibyte.foundry.inventory.issue.dto.response.MaterialIssueResponse;
import com.kalibyte.foundry.inventory.item.entity.enums.ItemCategory;
import com.kalibyte.foundry.inventory.item.entity.enums.ItemUnit;
import com.kalibyte.foundry.inventory.purchaseorder.dto.request.CreatePurchaseOrderRequest;
import com.kalibyte.foundry.inventory.purchaseorder.dto.request.OrderItemRequest;
import com.kalibyte.foundry.inventory.purchaseorder.dto.response.PurchaseOrderResponse;
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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.containsString;

@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@WithMockCustomUser
public class IssueFlowIntegrationTest extends BaseInventoryIntegrationTest {

    @Test
    @Order(1)
    void testFullIssueFlow_SuccessfulAndInsufficient() throws Exception {
        // Setup Item with Stock
        Long vendorId = createVendor("Vendor Y", "123", "GST", "Addr");
        Long deptId = createDepartment("Issue Dept", "IS-01");
        Long itemId = createItem("Issue Item", "IT-ISS-1", ItemCategory.CONSUMABLE, null, deptId, ItemUnit.PCS, BigDecimal.TEN, BigDecimal.ONE);

        CreatePurchaseOrderRequest poReq = new CreatePurchaseOrderRequest(
                vendorId, LocalDate.now(), List.of(new OrderItemRequest(itemId, new BigDecimal("100"), new BigDecimal("10.00"), null)), null
        );

        Long poId = objectMapper.readValue(mockMvc.perform(post("/api/purchase-orders")
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(poReq)))
                .andReturn().getResponse().getContentAsString(), new TypeReference<ApiResponse<PurchaseOrderResponse>>() {}).getData().id();

        StartInwardRequest inwReq = new StartInwardRequest("V1", "D1", "P1", "C1");
        Long inwardId = objectMapper.readValue(mockMvc.perform(post("/api/inwards/from-po/" + poId)
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(inwReq)))
                .andReturn().getResponse().getContentAsString(), new TypeReference<ApiResponse<InwardResponse>>() {}).getData().id();

        mockMvc.perform(post("/api/inwards/" + inwardId + "/confirm")).andExpect(status().isOk());

        // Stock is now 100, Avg Rate is 10.00
        // Record Issue of 60
        RecordIssueRequest issueReq = new RecordIssueRequest(
                deptId, "Production", LocalDate.now(),
                List.of(new IssueItemRequest(itemId, new BigDecimal("60"), null)), "Urgent issue"
        );

        MvcResult issueResult = mockMvc.perform(post("/api/material-issues")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(issueReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.items[0].issuedQuantity", is(60.0)))
                .andExpect(jsonPath("$.data.items[0].unitRate", is(10.0)))
                .andExpect(jsonPath("$.data.totalValue", is(600.0)))
                .andReturn();

        // Assert Stock Decreased
        mockMvc.perform(get("/api/items/" + itemId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.currentStock", is(40.0)))
                .andExpect(jsonPath("$.data.avgRate", is(10.0))); // Avg rate does not change on issue

        // Insufficient Stock (Trying to issue 50 when only 40 available)
        RecordIssueRequest failReq = new RecordIssueRequest(
                deptId, "Production 2", LocalDate.now(),
                List.of(new IssueItemRequest(itemId, new BigDecimal("50"), null)), null
        );

        mockMvc.perform(post("/api/material-issues")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(failReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Insufficient stock")));

        // Exact Remaining Stock (Issue 40)
        RecordIssueRequest exactReq = new RecordIssueRequest(
                deptId, "Production 3", LocalDate.now(),
                List.of(new IssueItemRequest(itemId, new BigDecimal("40"), null)), null
        );

        mockMvc.perform(post("/api/material-issues")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(exactReq)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/items/" + itemId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.currentStock", is(0.0)));

        // Issuing from Zero Stock
        mockMvc.perform(post("/api/material-issues")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(exactReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Insufficient stock")));
    }
}
