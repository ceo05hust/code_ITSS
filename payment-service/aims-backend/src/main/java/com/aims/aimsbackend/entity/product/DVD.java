package com.aims.aimsbackend.entity.product;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Entity
@Table(name = "dvd")
@Data
@EqualsAndHashCode(callSuper = true)
public class DVD extends Product{
    @Column(nullable = false, length = 100)
    private String discType;

    @Column(nullable = false, length = 255)
    private String director;

    @Column(nullable = false)
    private Integer runtime;

    @Column(nullable = false, length = 255)
    private String studio;

    @Column(nullable = false, length = 100)
    private String language;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String subtitles;

    private LocalDate releaseDate;

    @Column(length = 100)
    private String genre;
}