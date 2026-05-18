package com.aims.aimsbackend.service.product;

import com.aims.aimsbackend.entity.product.Product;
import com.aims.aimsbackend.repository.product.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public Product getProductById(Long productId){
        return productRepository.findById(productId)
                .orElseThrow(()-> new RuntimeException("Product not found" + productId));
    }

    public boolean checkStock(Long productId, int qty){
        Product product = getProductById(productId);
        return product.checkStock(qty);
    }
}
