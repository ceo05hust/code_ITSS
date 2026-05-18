package com.aims.aimsbackend.repository.user;

import com.aims.aimsbackend.entity.user.UserRole;
import com.aims.aimsbackend.entity.user.UserRoleId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {
}
