package com.kalibyte.foundry.order.entity;

import com.kalibyte.foundry.common.base.BaseEntity;
import com.kalibyte.foundry.customer.entity.Customer;
import com.kalibyte.foundry.order.entity.enums.OrderStatus;
import com.kalibyte.foundry.order.entity.enums.OrderType;
import com.kalibyte.foundry.quotation.entity.Quotation;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String orderNumber;

    //------------------------------------------------
    // CUSTOMER
    //------------------------------------------------
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    //------------------------------------------------
    // TYPE
    //------------------------------------------------
    @Enumerated(EnumType.STRING)
    @Column(name = "order_type", nullable = false)
    private OrderType orderType;

    //------------------------------------------------
    // QUOTATION (OPTIONAL)
    //------------------------------------------------
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quotation_id", unique = true)
    private Quotation quotation;

    //------------------------------------------------
    // DETAILS
    //------------------------------------------------
    private LocalDate orderDate;
    private LocalDate deliveryDate;

    private String placeOfSupply;
    private String poReference;

    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.CREATED;

    //------------------------------------------------
    // AMOUNTS
    //------------------------------------------------
    private BigDecimal subTotal = BigDecimal.ZERO;
    private BigDecimal discount = BigDecimal.ZERO;
    private BigDecimal tax = BigDecimal.ZERO;
    private BigDecimal totalAmount = BigDecimal.ZERO;

    //------------------------------------------------
    // ITEMS
    //------------------------------------------------
    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<OrderItem> items = new ArrayList<>();

    //------------------------------------------------
    // HELPERS
    //------------------------------------------------
    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }

    public void clearItems() {
        items.forEach(i -> i.setOrder(null));
        items.clear();
    }
}