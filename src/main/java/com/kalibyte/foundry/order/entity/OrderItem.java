package com.kalibyte.foundry.order.entity;

import com.kalibyte.foundry.common.base.BaseEntity;
import com.kalibyte.foundry.pattern.entity.Pattern;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "order_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem extends BaseEntity {

    //------------------------------------------------
    // ORDER
    //------------------------------------------------

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    //------------------------------------------------
    // PRODUCT
    //------------------------------------------------

    @Column(nullable = false)
    private String productName;

    @Column(nullable = false)
    private String metalType;

    //------------------------------------------------
    // PATTERN (REAL ASSOCIATION)
    //------------------------------------------------

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pattern_id")
    private Pattern pattern;

    //------------------------------------------------
    // QUANTITY
    //------------------------------------------------

    @Min(1)
    @Column(name = "quantity", nullable = false)
    private int quantity;

    //------------------------------------------------
    // PRICE
    //------------------------------------------------

    @Column(precision = 19, scale = 2)
    private BigDecimal unitPrice;

    @Column(precision = 19, scale = 2)
    private BigDecimal totalPrice;

}