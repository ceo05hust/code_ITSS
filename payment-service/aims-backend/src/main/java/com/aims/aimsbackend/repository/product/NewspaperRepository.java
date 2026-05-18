package com.aims.aimsbackend.repository.product;

import com.aims.aimsbackend.entity.product.Newspaper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NewspaperRepository extends JpaRepository<Newspaper, Long> {
}
