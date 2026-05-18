package com.aims.aimsbackend.service.order;

import com.aims.aimsbackend.entity.cart.Cart;
import com.aims.aimsbackend.entity.cart.CartItem;
import com.aims.aimsbackend.entity.product.Product;
import com.aims.aimsbackend.repository.product.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.availability.LivenessState;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartService {
    @Autowired
    private ProductRepository productRepository;

    private CartItem findCartItem(Cart cart, Product product) {
        return cart.getCartItems().stream()
                .filter(i -> i.getProduct().equals(product))
                .findFirst()
                .orElse(null);
    }

    public List<CartItem> checkStock(Cart cart){
        return cart.getCartItems().stream()
                .filter(item -> item.getProduct().getStockQuantity() < item.getQuantity())
                .toList();
    }

    public void emptyCart(Cart cart){
        cart.emptyCart();
    }

    public void addToCart(Cart cart, Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(()-> new RuntimeException("Product not found" + productId));

        CartItem existingItem = findCartItem(cart, product);

        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
        }
        else {
            CartItem item = new CartItem();
            item.setProduct(product);
            item.setQuantity(quantity);
            cart.addItem(item);
        }

    }

}
