package com.aims.aimsbackend.entity.order;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import com.aims.aimsbackend.entity.product.*;

@Entity
@Table(name = "order_item")
@Data
public class OrderItem {
    @EmbeddedId
    private OrderItemId id;

    @ManyToOne
    @MapsId("orderId")
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne
    @MapsId("productId")
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal sellingPrice;
}