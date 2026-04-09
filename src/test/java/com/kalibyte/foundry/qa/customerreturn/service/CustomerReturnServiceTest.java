package com.kalibyte.foundry.qa.customerreturn.service;

import com.kalibyte.foundry.billing.creditnote.dto.response.CreditNoteResponse;
import com.kalibyte.foundry.billing.creditnote.service.CreditNoteService;
import com.kalibyte.foundry.customer.entity.Customer;
import com.kalibyte.foundry.order.dto.request.OrderCreateRequest;
import com.kalibyte.foundry.order.dto.response.OrderResponse;
import com.kalibyte.foundry.order.entity.Order;
import com.kalibyte.foundry.order.entity.OrderItem;
import com.kalibyte.foundry.order.repository.OrderItemRepository;
import com.kalibyte.foundry.order.service.OrderService;
import com.kalibyte.foundry.qa.common.QaNumberGenerator;
import com.kalibyte.foundry.qa.common.enums.ReturnDisposition;
import com.kalibyte.foundry.qa.common.enums.ReturnStatus;
import com.kalibyte.foundry.qa.customerreturn.entity.CustomerReturn;
import com.kalibyte.foundry.qa.customerreturn.repository.CustomerReturnRepository;
import com.kalibyte.foundry.qa.tracking.service.QaTrackingLogService;
import com.kalibyte.foundry.scrap.service.ScrapService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerReturnServiceTest {

    @Mock
    private CustomerReturnRepository repository;
    @Mock
    private ScrapService scrapService;
    @Mock
    private QaTrackingLogService trackingLogService;
    @Mock
    private QaNumberGenerator numberGenerator;
    @Mock
    private OrderItemRepository orderItemRepository;
    @Mock
    private OrderService orderService;
    @Mock
    private CreditNoteService creditNoteService;
    @Mock
    private com.kalibyte.foundry.billing.invoice.repository.InvoiceRepository invoiceRepository;

    @InjectMocks
    private CustomerReturnService service;

    private CustomerReturn returnEntry;
    private OrderItem orderItem;
    private Order order;
    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId(UUID.randomUUID());
        customer.setName("Test Customer");

        order = new Order();
        order.setId(UUID.randomUUID());
        order.setOrderNumber("ORD-001");
        order.setGstPercentage(BigDecimal.valueOf(18));

        orderItem = new OrderItem();
        orderItem.setId(UUID.randomUUID());
        orderItem.setOrder(order);
        orderItem.setPartName("Test Part");
        orderItem.setUnitPrice(BigDecimal.valueOf(100));
        orderItem.setNetWeightKg(BigDecimal.valueOf(10));
        orderItem.setGstPercentage(BigDecimal.valueOf(18));

        returnEntry = new CustomerReturn();
        returnEntry.setId(1L);
        returnEntry.setReturnNumber("RET-001");
        returnEntry.setStatus(ReturnStatus.ASSESSED);
        returnEntry.setCustomer(customer);
        returnEntry.setOrder(order);
        returnEntry.setOrderItem(orderItem);
        returnEntry.setReturnedQuantity(5);
        returnEntry.setReturnedWeight(BigDecimal.valueOf(50));
        returnEntry.setQaRemarks("Some remarks");
    }

    @Test
    void dispositionReturn_Replace_ManualOrderId_ShouldSetReplacementOrderId() {
        // Given
        UUID replacementOrderId = UUID.randomUUID();
        when(repository.findWithDetailsById(1L)).thenReturn(Optional.of(returnEntry));
        when(repository.save(any(CustomerReturn.class))).thenReturn(returnEntry);

        // When
        CustomerReturn result = service.dispositionReturn(1L, ReturnDisposition.REPLACE, "Remarks", "Tester", null, replacementOrderId);

        // Then
        assertEquals(ReturnDisposition.REPLACE, result.getDisposition());
        assertEquals(replacementOrderId, result.getReplacementOrderId());
        verify(repository).save(returnEntry);
        verify(orderService, never()).createOrder(any());
    }

    @Test
    void dispositionReturn_Replace_AutoOrderId_ShouldCreateNewOrder() {
        // Given
        UUID newOrderId = UUID.randomUUID();
        when(repository.findWithDetailsById(1L)).thenReturn(Optional.of(returnEntry));
        when(repository.save(any(CustomerReturn.class))).thenReturn(returnEntry);
        when(orderService.createOrder(any(OrderCreateRequest.class))).thenReturn(OrderResponse.builder().id(newOrderId).orderNumber("ORD-NEW").build());

        // When
        CustomerReturn result = service.dispositionReturn(1L, ReturnDisposition.REPLACE, "Remarks", "Tester", null, null);

        // Then
        assertEquals(ReturnDisposition.REPLACE, result.getDisposition());
        assertEquals(newOrderId, result.getReplacementOrderId());
        verify(orderService).createOrder(any(OrderCreateRequest.class));
        verify(repository).save(returnEntry);
    }

    @Test
    void dispositionReturn_CreditNote_ShouldCreateCreditNote() {
        // Given
        UUID creditNoteId = UUID.randomUUID();
        BigDecimal creditAmount = BigDecimal.valueOf(500);
        when(repository.findWithDetailsById(1L)).thenReturn(Optional.of(returnEntry));
        when(repository.save(any(CustomerReturn.class))).thenReturn(returnEntry);
        
        CreditNoteResponse cnResponse = new CreditNoteResponse();
        cnResponse.setId(creditNoteId);
        cnResponse.setCreditNoteNumber("CN-001");
        
        when(creditNoteService.generateCreditNoteFromReturn(any(), any())).thenReturn(cnResponse);

        // When
        CustomerReturn result = service.dispositionReturn(1L, ReturnDisposition.CREDIT_NOTE, "Remarks", "Tester", creditAmount, null);

        // Then
        assertEquals(ReturnDisposition.CREDIT_NOTE, result.getDisposition());
        assertEquals(creditAmount, result.getCreditAmount());
        assertEquals(creditNoteId, result.getCreditNoteId());
        verify(creditNoteService).generateCreditNoteFromReturn(returnEntry, creditAmount);
        verify(repository).save(returnEntry);
    }
}
