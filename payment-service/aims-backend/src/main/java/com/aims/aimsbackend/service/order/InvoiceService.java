package com.aims.aimsbackend.service.order;

import com.aims.aimsbackend.entity.cart.Cart;
import com.aims.aimsbackend.entity.order.DeliveryInfo;
import com.aims.aimsbackend.entity.order.Invoice;
import com.aims.aimsbackend.entity.order.TransactionInfo;
import com.aims.aimsbackend.repository.order.InvoiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class InvoiceService {
    private static final BigDecimal TAX_RATE = new BigDecimal("0.10");

    @Autowired private InvoiceRepository invoiceRepository;

    @Autowired private ShippingFeeService shippingFeeService;

    public Invoice saveInvoice(Invoice invoice) {
        return invoiceRepository.save(invoice);
    }

    public Invoice buildInvoice(Cart cart, DeliveryInfo deliveryInfo, TransactionInfo transactionInfo) {
        BigDecimal subtotal = calculateSubtotal(cart);
        BigDecimal totalInclVAT = calculateTotalProductPriceInclVAT(subtotal);
        BigDecimal shippingFee = shippingFeeService.calculateShippingFee(cart, deliveryInfo);
        BigDecimal totalAmount = totalInclVAT.add(shippingFee);

        Invoice invoice = new Invoice();
        invoice.setTotalProductPriceExclVAT(subtotal);
        invoice.setTotalProductPriceInclVAT(totalInclVAT);
        invoice.setShippingFee(shippingFee);
        invoice.setTotalAmount(totalAmount);

        if (transactionInfo != null) {
            invoice.setTransactionInfo(transactionInfo);
        }

        return invoice;
    }

    public BigDecimal calculateSubtotal(Cart cart) {
        return cart.getCartItems().stream()
                .map(item -> item.getProduct().getSellingPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateTotalProductPriceInclVAT(BigDecimal subtotal) {
        return subtotal.multiply(BigDecimal.ONE.add(TAX_RATE))
                .setScale(2, RoundingMode.HALF_UP);
    }
}