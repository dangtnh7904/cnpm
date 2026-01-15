package com.nhom33.quanlychungcu.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Service gửi email thông báo.
 */
@Service
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@quanlychungcu.vn}")
    private String fromEmail;

    /**
     * Gửi email thông báo mật khẩu mới.
     */
    public void sendPasswordResetEmail(String toEmail, String fullName, String newPassword) {
        // Log để debug
        System.out.println("=================================================");
        System.out.println("[EMAIL NOTIFICATION] - NO-REPLY");
        System.out.println("To: " + toEmail);
        System.out.println("Subject: Mật khẩu của bạn đã được đặt lại");
        System.out.println("-------------------------------------------------");

        if (mailSender == null) {
            System.out.println("[WARNING] JavaMailSender not configured - Email NOT sent");
            System.out.println("Xin chào " + fullName + ",");
            System.out.println("Mật khẩu mới: " + newPassword);
            System.out.println("=================================================");
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Mật khẩu của bạn đã được đặt lại - Hệ thống Quản lý Chung cư");
            message.setText(
                    "Xin chào " + fullName + ",\n\n" +
                            "Mật khẩu tài khoản của bạn đã được đặt lại bởi quản trị viên.\n\n" +
                            "Thông tin đăng nhập:\n" +
                            "- Mật khẩu mới: " + newPassword + "\n\n" +
                            "Vui lòng đăng nhập và đổi mật khẩu ngay sau khi nhận được email này.\n\n" +
                            "Trân trọng,\n" +
                            "Hệ thống Quản lý Chung cư\n\n" +
                            "---\n" +
                            "Đây là email tự động, vui lòng không trả lời email này.");

            mailSender.send(message);
            System.out.println("[SUCCESS] Email sent to: " + toEmail);
            System.out.println("=================================================");
        } catch (Exception e) {
            System.out.println("[ERROR] Failed to send email: " + e.getMessage());
            System.out.println("Password was: " + newPassword);
            System.out.println("=================================================");
        }
    }
}
