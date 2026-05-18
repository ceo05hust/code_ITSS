package com.aims.aimsbackend.entity.product;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Entity
@Table(name = "book")
@Data
@EqualsAndHashCode(callSuper = true)
public class Book extends Product {
    @Column(nullable = false, columnDefinition = "TEXT")
    private String authors;

    @Column(nullable = false, length = 100)
    private String coverType;

    @Column(nullable = false, length = 255)
    private String publisher;

    @Column(nullable = false)
    private LocalDate publicationDate;

    private Integer numPages;

    @Column(length = 100)
    private String language;

    @Column(length = 100)
    private String genre;
}