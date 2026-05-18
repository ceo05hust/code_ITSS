package com.aims.aimsbackend.repository.product;

import com.aims.aimsbackend.entity.product.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
}
