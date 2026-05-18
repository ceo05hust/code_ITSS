package com.aims.aimsbackend.entity.order;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "delivery_info")
@Data
public class DeliveryInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long deliveryInfoId;

    @Column(nullable = false, length = 255)
    private String recipientName;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(nullable = false, length = 50)
    private String phone;

    @Column(nullable = false, length = 100)
    private String province;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String address;

    @Column(columnDefinition = "TEXT")
    private String deliveryInstruction;

    @OneToOne(mappedBy = "deliveryInfo")
    private Order order;
}