package com.aims.aimsbackend.service.order;

import com.aims.aimsbackend.entity.cart.Cart;
import com.aims.aimsbackend.entity.order.DeliveryInfo;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;

@Service
public class ShippingFeeService {
    public BigDecimal calculateShippingFee(Cart cart, DeliveryInfo deliveryInfo) {
        return new BigDecimal("22000.00"); // Mock/stub shipping fee
    }
}
