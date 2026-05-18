package com.aims.aimsbackend.service.order;

import com.aims.aimsbackend.entity.cart.Cart;
import com.aims.aimsbackend.entity.cart.CartItem;
import com.aims.aimsbackend.entity.enums.OrderStatus;
import com.aims.aimsbackend.entity.order.*;
import com.aims.aimsbackend.entity.product.Product;
import com.aims.aimsbackend.repository.order.OrderItemRepository;
import com.aims.aimsbackend.repository.order.OrderRepository;
import com.aims.aimsbackend.repository.product.ProductRepository;
import com.aims.aimsbackend.service.product.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderService {

    @Autowired private OrderItemRepository orderItemRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private ProductRepository productRepository;

    public void createOrderItems(Order order, Cart cart) {
        for (CartItem cartItem : cart.getCartItems()) {
            OrderItem orderItem = new OrderItem();

            OrderItemId id = new OrderItemId();
            id.setOrderId(order.getOrderId());
            id.setProductId(cartItem.getProduct().getProductId());

            orderItem.setId(id);
            orderItem.setOrder(order);
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setSellingPrice(cartItem.getProduct().getSellingPrice());

            orderItemRepository.save(orderItem);
        }
    }

    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new RuntimeException("Order cannot be cancelled at this stage");
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }
}