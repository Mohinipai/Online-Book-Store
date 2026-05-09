package com.bookstore.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender javaMailSender;

    public void sendOrderConfirmationEmail(String toEmail, String userName, Long orderId, String totalAmount) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Order Confirmation - Online Bookstore");
        message.setText("Dear " + userName + ",\n\n" +
                "Thank you for your order!\n" +
                "Your Order ID is: " + orderId + "\n" +
                "Total Amount: $" + totalAmount + "\n\n" +
                "We will process your order shortly.\n\n" +
                "Best regards,\nOnline Bookstore Team");

        try {
            javaMailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send email to " + toEmail + ". Ensure correct SMTP credentials are provided in application.properties.");
        }
    }
}
