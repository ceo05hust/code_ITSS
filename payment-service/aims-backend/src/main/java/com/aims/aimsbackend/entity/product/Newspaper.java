package com.aims.aimsbackend.entity.product;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Entity
@Table(name = "newspaper")
@Data
@EqualsAndHashCode(callSuper = true)
public class Newspaper extends Product {
    @Column(nullable = false, length = 255)
    private String editorInChief;

    @Column(nullable = false, length = 255)
    private String publisher;

    @Column(nullable = false)
    private LocalDate publicationDate;

    @Column(length = 100)
    private String issueNumber;

    @Column(length = 100)
    private String publicationFrequency;

    @Column(length = 100)
    private String issn;

    @Column(length = 100)
    private String language;

    @Column(columnDefinition = "TEXT")
    private String sections;
}
