package com.aims.aimsbackend.entity.product;

import com.aims.aimsbackend.entity.enums.ProductStatus;
import com.aims.aimsbackend.entity.order.OrderItem;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Table(name = "product")
@Data
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, length = 100)
    private String category;

    @Column(nullable = false, length = 100)
    private String barcode;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal height;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal width;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal length;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal weight;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal originalValue;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal sellingPrice;

    @Column(nullable = false)
    private Integer stockQuantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ProductStatus status;

    @OneToMany(mappedBy = "product")
    private java.util.List<OrderItem> orderItems;

    @OneToMany(mappedBy = "product")
    private java.util.List<ProductChangeHistory> changeHistories;

    public Product() {
        this.status = ProductStatus.ACTIVE;
    }

    public boolean isInStock() {
        return this.stockQuantity > 0;
    }

    public boolean isActive() {
        return this.status == ProductStatus.ACTIVE;
    }

    public boolean checkStock(int qty) {
        return this.stockQuantity >= qty;
    }

    public void addChangeHistory(ProductChangeHistory history) {
        this.changeHistories.add(history);
    }
}