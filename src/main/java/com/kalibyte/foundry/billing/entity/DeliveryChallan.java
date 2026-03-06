package com.kalibyte.foundry.billing.entity;

import com.kalibyte.foundry.billing.ENUM.DCStatus;
import com.kalibyte.foundry.common.base.BaseEntity;
import com.kalibyte.foundry.customer.entity.Customer;
import com.kalibyte.foundry.order.entity.Order;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "delivery_challans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryChallan extends BaseEntity {

    @Column(name = "dc_number", nullable = false, unique = true)
    private String dcNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    private LocalDate dispatchDate;

    private String vehicleNumber;

    private String transportName;

    private String lrNumber;

    private Integer totalQuantity;

    private BigDecimal totalWeight;

    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    private DCStatus status;

    @OneToMany(
            mappedBy = "deliveryChallan",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<DeliveryChallanItem> items;

    public void addItem(DeliveryChallanItem item) {
        items.add(item);
        item.setDeliveryChallan(this);
    }
}