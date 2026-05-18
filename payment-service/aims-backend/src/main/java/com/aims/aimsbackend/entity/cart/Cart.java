package com.aims.aimsbackend.entity.cart;

import com.aims.aimsbackend.entity.product.Product;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class Cart {
    private List<CartItem> cartItems = new ArrayList<>();

    public void addItem(CartItem item) {
        cartItems.add(item);
    }

    public void removeItem(Product product) {
        cartItems.removeIf(i -> i.getProduct().equals(product));
    }

    public void emptyCart() {
        cartItems.clear();
    }

    public BigDecimal getTotalPrice() {
        return cartItems.stream()
                .map(i -> i.getProduct().getSellingPrice()
                        .multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}