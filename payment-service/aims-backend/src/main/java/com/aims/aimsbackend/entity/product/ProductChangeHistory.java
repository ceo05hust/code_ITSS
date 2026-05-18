package com.aims.aimsbackend.entity.product;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import com.aims.aimsbackend.entity.user.*;

@Entity
@Table(name = "productchangehistory")
@Data
public class ProductChangeHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long changeId;

    @Column(nullable = false, length = 100)
    private String changeType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String changeDetail;

    @Column(columnDefinition = "TEXT")
    private String reasonForChange;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Product product;

    @ManyToOne
    @JoinColumn(nullable = false)
    private User user;
}