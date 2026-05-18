package com.aims.aimsbackend.entity.user;

import jakarta.persistence.Embeddable;
import lombok.Data;
import java.io.Serializable;

@Embeddable
@Data
public class UserRoleId implements Serializable {
    private Long userId;
    private Long roleId;
}