package com.aims.aimsbackend.repository.product;

import com.aims.aimsbackend.entity.product.ProductChangeHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductChangeHistoryRepository extends JpaRepository<ProductChangeHistory, Long> {
}
