package com.aims.aimsbackend.entity.order;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "delivery_info")
@Data
public class DeliveryInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}
