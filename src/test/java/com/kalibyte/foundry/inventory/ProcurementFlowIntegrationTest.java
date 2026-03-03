package com.kalibyte.foundry.inventory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.kalibyte.foundry.common.response.ApiResponse;
import com.kalibyte.foundry.inventory.inward.dto.request.StartInwardRequest;
import com.kalibyte.foundry.inventory.inward.dto.request.UpdateReceivedQuantityRequest;
import com.kalibyte.foundry.inventory.inward.dto.response.InwardResponse;
import com.kalibyte.foundry.inventory.item.dto.response.ItemResponse;
import com.kalibyte.foundry.inventory.item.entity.enums.ItemCategory;
import com.kalibyte.foundry.inventory.item.entity.enums.ItemSubCategory;
import com.kalibyte.foundry.inventory.item.entity.enums.ItemUnit;
import com.kalibyte.foundry.inventory.purchaseorder.dto.request.CreatePurchaseOrderRequest;
import com.kalibyte.foundry.inventory.purchaseorder.dto.request.OrderItemRequest;
import com.kalibyte.foundry.inventory.purchaseorder.dto.response.PurchaseOrderResponse;
import com.kalibyte.foundry.inventory.purchaseorder.entity.enums.POStatus;
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

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@WithMockCustomUser
public class ProcurementFlowIntegrationTest extends BaseInventoryIntegrationTest {

    @Test
    @Order(1)
    void testFullProcurementFlowAndWAC() throws Exception {
        // 1. Setup Master Data
        Long vendorId = createVendor("Steel Supplier", "1234567890", "GSTIN1", "Pune");
        Long deptId = createDepartment("Melt Shop", "MELT");
        Long itemId = createItem("Scrap Steel", "SS-1", ItemCategory.RAW_MATERIAL, ItemSubCategory.FERROUS, deptId, ItemUnit.KG, new BigDecimal("1000"), new BigDecimal("500"));

        // 2. Create PO
        CreatePurchaseOrderRequest poReq = new CreatePurchaseOrderRequest(
                vendorId,
                LocalDate.now().plusDays(10),
                List.of(new OrderItemRequest(itemId, new BigDecimal("2000"), new BigDecimal("40.00"), "First Lot")),
                "Urgent"
        );

        MvcResult poResult = mockMvc.perform(post("/api/purchase-orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(poReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status", is("OPEN")))
                .andExpect(jsonPath("$.data.totalOrderValue", is(80000.0)))
                .andReturn();

        Long poId = objectMapper.readValue(poResult.getResponse().getContentAsString(),
                new TypeReference<ApiResponse<PurchaseOrderResponse>>() {}).getData().id();

        // 3. Start Inward (DRAFT)
        StartInwardRequest inwReq = new StartInwardRequest("MH12AB1234", "Driver 1", "9876543210", "CH-001");
        MvcResult inwResult = mockMvc.perform(post("/api/inwards/from-po/" + poId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inwReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status", is("DRAFT")))
                .andExpect(jsonPath("$.data.receivedItems", hasSize(1)))
                .andReturn();

        InwardResponse inwardResp = objectMapper.readValue(inwResult.getResponse().getContentAsString(),
                new TypeReference<ApiResponse<InwardResponse>>() {}).getData();
        Long inwardId = inwardResp.id();
        Long receivedItemId = inwardResp.receivedItems().get(0).id();

        // 4. Update Received Quantity (Partial Reception)
        UpdateReceivedQuantityRequest updateReq = new UpdateReceivedQuantityRequest(receivedItemId, new BigDecimal("1000"), new BigDecimal("40.00"));
        mockMvc.perform(put("/api/inwards/" + inwardId + "/received-quantities")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(List.of(updateReq))))
                .andExpect(status().isOk());

        // 5. Confirm Inward
        mockMvc.perform(post("/api/inwards/" + inwardId + "/confirm"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is("CONFIRMED")));

        // 6. Assert stock updated, PO status changed to PARTIALLY_RECEIVED
        MvcResult itemResult = mockMvc.perform(get("/api/items/" + itemId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.currentStock", is(1000.0)))
                .andExpect(jsonPath("$.data.avgRate", is(40.0)))
                .andReturn();

        mockMvc.perform(get("/api/purchase-orders/" + poId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is("PARTIALLY_RECEIVED")))
                .andExpect(jsonPath("$.data.items[0].receivedQuantity", is(1000.0)))
                .andExpect(jsonPath("$.data.items[0].pendingQuantity", is(1000.0)));

        // 7. Verify Ledger Entry (Credit)
        mockMvc.perform(get("/api/vendors/" + vendorId + "/ledger"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].entryType", is("CREDIT")))
                .andExpect(jsonPath("$.data.content[0].amount", is(40000.0)));

        // 8. WAC Calculation Check - Another Inward at different rate
        StartInwardRequest inwReq2 = new StartInwardRequest("MH12AB1234", "Driver 2", "9876543211", "CH-002");
        MvcResult inwResult2 = mockMvc.perform(post("/api/inwards/from-po/" + poId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inwReq2)))
                .andExpect(status().isCreated())
                .andReturn();

        InwardResponse inwardResp2 = objectMapper.readValue(inwResult2.getResponse().getContentAsString(),
                new TypeReference<ApiResponse<InwardResponse>>() {}).getData();
        Long inwardId2 = inwardResp2.id();
        Long receivedItemId2 = inwardResp2.receivedItems().get(0).id();

        // Update to 1000 qty but rate is now 50.00
        UpdateReceivedQuantityRequest updateReq2 = new UpdateReceivedQuantityRequest(receivedItemId2, new BigDecimal("1000"), new BigDecimal("50.00"));
        mockMvc.perform(put("/api/inwards/" + inwardId2 + "/received-quantities")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(List.of(updateReq2))))
                .andExpect(status().isOk());

        // Confirm second inward
        mockMvc.perform(post("/api/inwards/" + inwardId2 + "/confirm"))
                .andExpect(status().isOk());

        // WAC should be: ((1000 * 40) + (1000 * 50)) / 2000 = 45.0
        mockMvc.perform(get("/api/items/" + itemId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.currentStock", is(2000.0)))
                .andExpect(jsonPath("$.data.avgRate", is(45.0)))
                .andExpect(jsonPath("$.data.lastPurchaseRate", is(50.0)));

        // PO Should be RECEIVED
        mockMvc.perform(get("/api/purchase-orders/" + poId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is("RECEIVED")))
                .andExpect(jsonPath("$.data.items[0].pendingQuantity", is(0.0)));
    }

    @Test
    @Order(2)
    void testCancelReceivedPO_Fails() throws Exception {
        // Verify we can't cancel a fully received PO
        Long vendorId = createVendor("Vendor Z", "1234", "GSTZ", "Address");
        Long deptId = createDepartment("Test Dept", "TD01");
        Long itemId = createItem("Test Item", "TI-01", ItemCategory.RAW_MATERIAL, null, deptId, ItemUnit.KG, BigDecimal.TEN, BigDecimal.ONE);

        CreatePurchaseOrderRequest poReq = new CreatePurchaseOrderRequest(
                vendorId, LocalDate.now(), List.of(new OrderItemRequest(itemId, BigDecimal.TEN, BigDecimal.TEN, null)), null
        );

        Long poId = objectMapper.readValue(mockMvc.perform(post("/api/purchase-orders")
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(poReq)))
                .andReturn().getResponse().getContentAsString(), new TypeReference<ApiResponse<PurchaseOrderResponse>>() {}).getData().id();

        StartInwardRequest inwReq = new StartInwardRequest("V1", "D1", "P1", "C1");
        Long inwardId = objectMapper.readValue(mockMvc.perform(post("/api/inwards/from-po/" + poId)
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(inwReq)))
                .andReturn().getResponse().getContentAsString(), new TypeReference<ApiResponse<InwardResponse>>() {}).getData().id();

        mockMvc.perform(post("/api/inwards/" + inwardId + "/confirm")).andExpect(status().isOk());

        // Cancel PO should throw BusinessException because status is RECEIVED
        mockMvc.perform(post("/api/purchase-orders/" + poId + "/cancel"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Only OPEN orders can be cancelled")));
    }
}
