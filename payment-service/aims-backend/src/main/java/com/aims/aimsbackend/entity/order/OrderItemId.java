package com.aims.aimsbackend.entity.order;

import jakarta.persistence.Embeddable;
import lombok.Data;
import java.io.Serializable;

@Embeddable
@Data
public class OrderItemId implements Serializable {
    private Long orderId;
    private Long productId;
}