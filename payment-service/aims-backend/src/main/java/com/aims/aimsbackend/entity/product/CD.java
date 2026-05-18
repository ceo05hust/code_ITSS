package com.aims.aimsbackend.entity.product;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "cd")
@Data
@EqualsAndHashCode(callSuper = true)
public class CD extends Product {
    @Column(nullable = false, columnDefinition = "TEXT")
    private String artists;

    @Column(nullable = false, length = 255)
    private String recordLabel;

    @Column(nullable = false, length = 100)
    private String genre;

    private LocalDate releaseDate;

    @OneToMany(mappedBy = "cd", cascade = CascadeType.ALL)
    private List<Track> tracks;
}