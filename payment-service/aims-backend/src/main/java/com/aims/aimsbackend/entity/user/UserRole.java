package com.aims.aimsbackend.entity.user;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "userrole")
@Data
public class UserRole {
    @EmbeddedId
    private UserRoleId id;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "userid", nullable = false)
    private User user;

    @ManyToOne
    @MapsId("roleId")
    @JoinColumn(name = "roleid", nullable = false)
    private Role role;
}