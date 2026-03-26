package com.intellicart.notificationservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendWelcomeEmail(String to, String firstName) {
        log.info("Sending welcome email to {}", to);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Welcome to Intellicart!");
        message.setText("Hello " + firstName + ",\n\nWelcome to Intellicart! Your account has been successfully created.");
        try {
            mailSender.send(message);
            log.info("Welcome email sent successfully to {}", to);
        } catch (Exception e) {
            log.error("Failed to send welcome email to {}", to, e);
        }
    }

    public void sendOrderConfirmation(String to, Long orderId) {
        log.info("Sending order confirmation email to {} for order {}", to, orderId);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Order Confirmation - #" + orderId);
        message.setText("Your order #" + orderId + " has been received and is being processed.");
        try {
            mailSender.send(message);
            log.info("Order confirmation email sent successfully to {}", to);
        } catch (Exception e) {
            log.error("Failed to send order confirmation email to {}", to, e);
        }
    }

    public void sendPaymentConfirmation(String to, Long orderId) {
        log.info("Sending payment success email for order {}", orderId);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Payment Success - Order #" + orderId);
        message.setText("Good news! Your payment for order #" + orderId + " was successful.");
        try {
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Failed to send payment confirmation email", e);
        }
    }

    public void sendPaymentFailure(String to, Long orderId) {
        log.info("Sending payment failure email for order {}", orderId);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Payment Failed - Order #" + orderId);
        message.setText("Unfortunately, the payment for your order #" + orderId + " failed. Please try again.");
        try {
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Failed to send payment failure email", e);
        }
    }
}
