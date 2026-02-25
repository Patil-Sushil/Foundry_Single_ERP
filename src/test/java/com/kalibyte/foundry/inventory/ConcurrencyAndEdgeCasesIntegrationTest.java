package com.kalibyte.foundry.inventory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.kalibyte.foundry.common.response.ApiResponse;
import com.kalibyte.foundry.inventory.inward.dto.request.StartInwardRequest;
import com.kalibyte.foundry.inventory.inward.dto.request.UpdateReceivedQuantityRequest;
import com.kalibyte.foundry.inventory.inward.dto.response.InwardResponse;
import com.kalibyte.foundry.inventory.issue.dto.request.IssueItemRequest;
import com.kalibyte.foundry.inventory.issue.dto.request.RecordIssueRequest;
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
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@WithMockCustomUser
public class ConcurrencyAndEdgeCasesIntegrationTest extends BaseInventoryIntegrationTest {

    @Test
    @Order(1)
    void testNumberGenerationAndAuditFields() throws Exception {
        Long vendorId = createVendor("Audit Vendor", "888", "GST-A", "Addr");
        Long deptId = createDepartment("Audit Dept", "A-DEPT");
        Long itemId = createItem("Audit Item", "A-ITM", ItemCategory.TOOL, null, deptId, ItemUnit.PCS, BigDecimal.TEN, BigDecimal.ONE);

        CreatePurchaseOrderRequest poReq = new CreatePurchaseOrderRequest(
                vendorId, LocalDate.now(), List.of(new OrderItemRequest(itemId, new BigDecimal("100"), new BigDecimal("50.00"), null)), null
        );

        MvcResult poResult = mockMvc.perform(post("/api/purchase-orders")
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(poReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.poNumber", matchesPattern("^PO-\\d{4}-\\d{4}$")))
                .andExpect(jsonPath("$.data.createdAt", notNullValue()))
                .andReturn();

        Long poId = objectMapper.readValue(poResult.getResponse().getContentAsString(), new TypeReference<ApiResponse<PurchaseOrderResponse>>() {}).getData().id();

        StartInwardRequest inwReq = new StartInwardRequest("V1", "D1", "P1", "C1");
        MvcResult inwResult = mockMvc.perform(post("/api/inwards/from-po/" + poId)
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(inwReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.inwardNumber", matchesPattern("^MI-\\d{4}-\\d{4}$")))
                .andExpect(jsonPath("$.data.createdAt", notNullValue()))
                .andReturn();

        Long inwardId = objectMapper.readValue(inwResult.getResponse().getContentAsString(), new TypeReference<ApiResponse<InwardResponse>>() {}).getData().id();
        mockMvc.perform(post("/api/inwards/" + inwardId + "/confirm")).andExpect(status().isOk());

        RecordIssueRequest issueReq = new RecordIssueRequest(
                deptId, "Testing Audit", LocalDate.now(),
                List.of(new IssueItemRequest(itemId, new BigDecimal("10"), null)), null
        );

        mockMvc.perform(post("/api/material-issues")
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(issueReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.issueNumber", matchesPattern("^ISS-\\d{4}-\\d{4}$")))
                .andExpect(jsonPath("$.data.createdAt", notNullValue()));
    }

    // A note about concurrency: True multi-threaded transactional testing in Spring Boot with MockMvc is tricky because MockMvc runs in the same thread.
    // If we use MockMvc from multiple threads, each thread starts a new HTTP request context.
    // We would need to either mock it at the service level, or spin up actual threads to hit MockMvc (which works, since MockMvc is thread-safe to an extent).
    @Test
    @Order(2)
    void testConcurrentIssues_ShouldFailOne() throws Exception {
        Long vendorId = createVendor("Concurrent Vendor", "123", "C-GST", "Addr");
        Long deptId = createDepartment("Concurrent Dept", "C-DEPT");
        Long itemId = createItem("Concurrent Item", "C-ITM", ItemCategory.TOOL, null, deptId, ItemUnit.PCS, BigDecimal.TEN, BigDecimal.ONE);

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

        // Now we have 100 stock. We will try to issue 100 twice concurrently.
        RecordIssueRequest issueReq = new RecordIssueRequest(
                deptId, "Testing Concurrency", LocalDate.now(),
                List.of(new IssueItemRequest(itemId, new BigDecimal("100"), null)), null
        );

        String jsonPayload = objectMapper.writeValueAsString(issueReq);

        int threadCount = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    latch.await();
                    MvcResult result = mockMvc.perform(post("/api/material-issues")
                            .contentType(MediaType.APPLICATION_JSON).content(jsonPayload))
                            .andReturn();
                    if (result.getResponse().getStatus() == 201) {
                        successCount.incrementAndGet();
                    } else {
                        failCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    done.countDown();
                }
            });
        }

        latch.countDown();
        done.await();
        executor.shutdown();

        // Only one should succeed, stock should be 0.
        // It's possible both fail if OptimisticLockException is thrown, but at most one succeeds.
        assertTrue(successCount.get() <= 1);
        
        mockMvc.perform(get("/api/items/" + itemId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.currentStock", is(0.0)));
    }
}
