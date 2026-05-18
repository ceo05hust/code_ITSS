package com.aims.aimsbackend.entity.user;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;
import  com.aims.aimsbackend.entity.product.*;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false, length = 100)
    private String username;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(nullable = false)
    private Boolean isBlocked;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<UserRole> userRoles;

    @OneToMany(mappedBy = "user")
    private List<ProductChangeHistory> changeHistories;
}