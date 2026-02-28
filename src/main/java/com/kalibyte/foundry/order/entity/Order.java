package com.kalibyte.foundry.order.entity;

import com.kalibyte.foundry.common.base.BaseEntity;
import com.kalibyte.foundry.customer.entity.Customer;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quotation_id", nullable = false, unique = true)
    private Quotation quotation;

    private LocalDate orderDate;
    private LocalDate deliveryDate;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Column(precision = 19, scale = 2)
    private BigDecimal totalAmount;

    @OneToMany(mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();
}