package com.aims.aimsbackend.entity.cart;

import com.aims.aimsbackend.entity.product.Product;
import lombok.Data;

@Data
public class CartItem {
    private Product product;
    private Integer quantity;
}