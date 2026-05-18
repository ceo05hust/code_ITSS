package com.aims.aimsbackend.repository.order;

import com.aims.aimsbackend.entity.order.OrderItem;
import com.aims.aimsbackend.entity.order.OrderItemId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, OrderItemId> {
}
