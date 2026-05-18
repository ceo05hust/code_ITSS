package com.aims.aimsbackend.repository.product;

import com.aims.aimsbackend.entity.product.DVD;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DVDRepository extends JpaRepository<DVD, Long> {
}
