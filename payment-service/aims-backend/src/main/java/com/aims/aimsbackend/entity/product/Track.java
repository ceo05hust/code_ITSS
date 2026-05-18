package com.aims.aimsbackend.entity.product;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "track")
@Data
public class Track {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long trackId;

    @ManyToOne
    @JoinColumn(nullable = false)
    private CD cd;

    @Column(nullable = false, length = 255)
    private String trackTitle;

    @Column(nullable = false, length = 50)
    private String trackLength;
}
