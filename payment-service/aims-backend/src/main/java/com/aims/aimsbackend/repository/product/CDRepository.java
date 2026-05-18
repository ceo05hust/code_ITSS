package com.aims.aimsbackend.repository.product;

import com.aims.aimsbackend.entity.product.CD;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CDRepository extends JpaRepository<CD, Long> {
}
