package com.kalibyte.foundry.order.entity;

import com.kalibyte.foundry.common.base.BaseEntity;
import com.kalibyte.foundry.customer.entity.Customer;
import com.kalibyte.foundry.order.entity.ENUM.OrderStatus;
import com.kalibyte.foundry.order.entity.ENUM.OrderType;
import com.kalibyte.foundry.pattern.entity.Pattern;
import com.kalibyte.foundry.quotation.entity.Quotation;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
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
    @NotBlank
    private String orderNumber;

    //------------------------------------------------
    // CUSTOMER
    //------------------------------------------------

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    //------------------------------------------------
    // ORDER TYPE
    //------------------------------------------------

    @Enumerated(EnumType.STRING)
    @Column(name = "order_type", nullable = false)
    private OrderType orderType;

    //------------------------------------------------
    // QUOTATION
    //------------------------------------------------

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quotation_id", unique = true)
    private Quotation quotation;

    //------------------------------------------------
    // ORDER DATA
    //------------------------------------------------

    private LocalDate orderDate;

    @Column(length = 150)
    private String placeOfSupply;

    @Column(length = 150)
    private String poReference;

    private LocalDate deliveryDate;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Column(precision = 19, scale = 2)
    private BigDecimal totalAmount;

    //------------------------------------------------
    // ITEMS
    //------------------------------------------------

    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<OrderItem> orderItems = new ArrayList<>();

    //------------------------------------------------
    // HELPER METHODS
    //------------------------------------------------

    public Pattern getPattern() {
        if (!orderItems.isEmpty()) {
            return orderItems.get(0).getPattern();
        }
        return null;
    }

    public int getTotalQuantity() {
        return orderItems.stream()
                .mapToInt(OrderItem::getQuantity)
                .sum();
    }

    public List<OrderItem> getItems() {
        return orderItems;
    }
}