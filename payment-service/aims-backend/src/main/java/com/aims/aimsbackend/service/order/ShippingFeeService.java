package com.aims.aimsbackend.service.order;

import com.aims.aimsbackend.entity.cart.Cart;
import com.aims.aimsbackend.entity.order.DeliveryInfo;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class ShippingFeeService {
    private static final BigDecimal HANOI_HCM_BASE_FEE = BigDecimal.valueOf(22000);
    private static final BigDecimal OTHER_PROVINCE_BASE_FEE = BigDecimal.valueOf(30000);
    private static final BigDecimal EXTRA_FEE_PER_HALF_KG = BigDecimal.valueOf(2500);
    private static final BigDecimal FREE_SHIPPING_THRESHOLD = BigDecimal.valueOf(100000);
    private static final BigDecimal MAX_SHIPPING_DISCOUNT = BigDecimal.valueOf(25000);

    public BigDecimal calculateShippingFee(Cart cart, DeliveryInfo deliveryInfo) {
        double totalWeight = calculateTotalWeight(cart);
        BigDecimal fee;

        if (isHanoiOrHoChiMinh(deliveryInfo.getProvince())) {
            fee = calculateHanoiHCMFee(totalWeight);
        } else {
            fee = calculateOtherProvinceFee(totalWeight);
        }

        if (cart.getTotalPrice().compareTo(FREE_SHIPPING_THRESHOLD) > 0) {
            BigDecimal discount = fee.min(MAX_SHIPPING_DISCOUNT);
            fee = fee.subtract(discount);
        }

        return fee;
    }

    public double calculateTotalWeight(Cart cart) {
        return cart.getCartItems().stream()
                .mapToDouble(item -> item.getProduct().getWeight().doubleValue() * item.getQuantity())
                .sum();
    }

    private boolean isHanoiOrHoChiMinh(String province) {
        return province != null
                && (province.contains("Hà Nội") || province.contains("Hồ Chí Minh"));
    }

    private BigDecimal calculateHanoiHCMFee(double weight) {
        BigDecimal fee = HANOI_HCM_BASE_FEE;

        if (weight > 3) {
            double extraWeight = weight - 3;
            int extraUnits = (int) Math.ceil(extraWeight / 0.5);
            fee = fee.add(EXTRA_FEE_PER_HALF_KG.multiply(BigDecimal.valueOf(extraUnits)));
        }

        return fee;
    }

    private BigDecimal calculateOtherProvinceFee(double weight) {
        BigDecimal fee = OTHER_PROVINCE_BASE_FEE;

        if (weight > 0.5) {
            double extraWeight = weight - 0.5;
            int extraUnits = (int) Math.ceil(extraWeight / 0.5);
            fee = fee.add(EXTRA_FEE_PER_HALF_KG.multiply(BigDecimal.valueOf(extraUnits)));
        }

        return fee;
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    public void validateDeliveryInfo(DeliveryInfo deliveryInfo) {
        if (isBlank(deliveryInfo.getRecipientName())) {
            throw new IllegalArgumentException("Recipient name cannot be empty");
        }
        if (isBlank(deliveryInfo.getEmail())) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        if (isBlank(deliveryInfo.getAddress())) {
            throw new IllegalArgumentException("Address cannot be empty");
        }
        if (isBlank(deliveryInfo.getProvince())) {
            throw new IllegalArgumentException("Province cannot be empty");
        }
        if (isBlank(deliveryInfo.getPhone())) {
            throw new IllegalArgumentException("Phone cannot be empty");
        }
    }
}