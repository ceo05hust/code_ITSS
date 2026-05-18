package com.aims.aimsbackend.service.order;

import com.aims.aimsbackend.entity.order.Order;
import com.aims.aimsbackend.exception.order.EmailSendingException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Async
    public void sendInvoice(String toEmail, Order order) {
        try {
            String subject = "AIMS - Confirmed order #" + order.getOrderId();
            String content = buildInvoiceEmail(order);
            sendEmail(toEmail, subject, content);
        } catch (Exception e) {
            log.error("Failed to send invoice email to {}: {}", toEmail, e.getMessage());
        }
    }

    private String buildInvoiceEmail(Order order) {
        String cancelToken = Base64.getEncoder().encodeToString(
                (order.getOrderId() + ":" + order.getDeliveryInfo().getEmail()).getBytes()
        );
        String trackLink = "http://localhost:5173/order/track?token=" + cancelToken;

        String sb = "Thank you for your order at AIMS!\n\n" +
                "Order ID: " + order.getOrderId() + "\n" +
                "Recipient: " + order.getDeliveryInfo().getRecipientName() + "\n" +
                "Address: " + order.getDeliveryInfo().getAddress() + "\n" +
                "Total Amount: " + order.getInvoice().getTotalAmount() + " VND\n" +
                "\nTransaction ID: " + order.getInvoice().getTransactionInfo().getExternalTransactionId() + "\n" +
                "You can track your order status and cancel it here: " + trackLink;
        return sb;
    }

    private void sendEmail(String toEmail, String subject, String content) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(content, false);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new EmailSendingException(toEmail, e);
        }
    }
}