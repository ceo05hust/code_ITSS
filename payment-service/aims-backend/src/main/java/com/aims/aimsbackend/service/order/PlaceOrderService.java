package com.aims.aimsbackend.service.order;

import com.aims.aimsbackend.entity.cart.Cart;
import com.aims.aimsbackend.entity.cart.CartItem;
import com.aims.aimsbackend.entity.enums.OrderStatus;
import com.aims.aimsbackend.entity.order.DeliveryInfo;
import com.aims.aimsbackend.entity.order.Invoice;
import com.aims.aimsbackend.entity.order.Order;
import com.aims.aimsbackend.entity.order.TransactionInfo;
import com.aims.aimsbackend.repository.order.DeliveryInfoRepository;
import com.aims.aimsbackend.repository.order.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PlaceOrderService {
    @Autowired private OrderRepository orderRepository;
    @Autowired private DeliveryInfoRepository deliveryInfoRepository;
    @Autowired private CartService cartService;
    @Autowired private InvoiceService invoiceService;
    @Autowired private OrderService orderService;
    @Autowired private EmailService emailService;

    public List<CartItem> checkStock(Cart cart) {
        return cartService.checkStock(cart);
    }

    @Transactional
    public Order confirmOrder(Cart cart, DeliveryInfo deliveryInfo, TransactionInfo transactionInfo) {
        Invoice invoice = invoiceService.buildInvoice(cart, deliveryInfo, transactionInfo);
        Invoice savedInvoice = invoiceService.saveInvoice(invoice);

        DeliveryInfo savedDeliveryInfo = deliveryInfoRepository.save(deliveryInfo);

        Order order = new Order();
        order.setInvoice(savedInvoice);
        order.setDeliveryInfo(savedDeliveryInfo);
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());
        Order savedOrder = orderRepository.save(order);

        orderService.createOrderItems(savedOrder, cart);
        cartService.emptyCart(cart);
        emailService.sendInvoice(savedDeliveryInfo.getEmail(), savedOrder);

        return savedOrder;
    }
}