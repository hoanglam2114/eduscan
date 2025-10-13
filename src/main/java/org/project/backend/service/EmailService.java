package org.project.backend.service;

import org.project.backend.model.SubscriptionPlan;
import org.project.backend.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendPaymentSuccessEmail(User user, SubscriptionPlan plan, BigDecimal amount, Timestamp activationDate, Timestamp endDate) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(user.getEmail());
            helper.setSubject("Hóa đơn thanh toán - Gói " + plan.getName());

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            String activationStr = sdf.format(new java.util.Date(activationDate.getTime()));
            String endStr = sdf.format(new java.util.Date(endDate.getTime()));

            String emailBody = buildEmailBody(user.getUsername(), plan.getName(), amount, activationStr, endStr);

            helper.setText(emailBody, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            System.err.println("Failed to send email: " + e.getMessage());
            throw new RuntimeException("Failed to send email", e);
        }
    }

    private String buildEmailBody(String username, String planName, BigDecimal amount, String activationDate, String endDate) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<meta charset=\"UTF-8\">" +
                "<style>" +
                "body { font-family: Arial, sans-serif; background-color: #f5f5f5; }" +
                ".container { max-width: 600px; margin: 0 auto; background-color: #fff; padding: 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }" +
                ".header { border-bottom: 2px solid #007bff; padding-bottom: 15px; margin-bottom: 20px; }" +
                ".header h2 { color: #007bff; margin: 0; }" +
                ".content { line-height: 1.6; color: #333; }" +
                ".bill-item { display: flex; justify-content: space-between; padding: 10px 0; border-bottom: 1px solid #eee; }" +
                ".bill-item-label { font-weight: bold; }" +
                ".bill-item-value { text-align: right; }" +
                ".total { display: flex; justify-content: space-between; padding: 15px 0; background-color: #f0f0f0; border-radius: 5px; padding: 10px; margin: 10px 0; }" +
                ".total-label { font-weight: bold; font-size: 16px; }" +
                ".total-value { font-weight: bold; font-size: 16px; color: #007bff; }" +
                ".footer { margin-top: 20px; padding-top: 15px; border-top: 1px solid #eee; font-size: 12px; color: #666; text-align: center; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class=\"container\">" +
                "<div class=\"header\">" +
                "<h2>🎉 Thanh toán thành công!</h2>" +
                "</div>" +
                "<div class=\"content\">" +
                "<p>Xin chào <strong>" + username + "</strong>,</p>" +
                "<p>Cảm ơn bạn đã thanh toán. Dưới đây là chi tiết hóa đơn của bạn:</p>" +
                "<div class=\"bill-item\">" +
                "<span class=\"bill-item-label\">Tên gói:</span>" +
                "<span class=\"bill-item-value\">" + planName + "</span>" +
                "</div>" +
                "<div class=\"bill-item\">" +
                "<span class=\"bill-item-label\">Số tiền:</span>" +
                "<span class=\"bill-item-value\">" + amount + " VND</span>" +
                "</div>" +
                "<div class=\"bill-item\">" +
                "<span class=\"bill-item-label\">Ngày kích hoạt:</span>" +
                "<span class=\"bill-item-value\">" + activationDate + "</span>" +
                "</div>" +
                "<div class=\"bill-item\">" +
                "<span class=\"bill-item-label\">Ngày kết thúc đăng ký:</span>" +
                "<span class=\"bill-item-value\">" + endDate + "</span>" +
                "</div>" +
                "<div class=\"total\">" +
                "<span class=\"total-label\">Tổng cộng:</span>" +
                "<span class=\"total-value\">" + amount + " VND</span>" +
                "</div>" +
                "<p style=\"color: #666; font-size: 14px; margin-top: 20px;\">" +
                "Gói của bạn đã được kích hoạt. Bạn có thể bắt đầu sử dụng dịch vụ ngay bây giờ!" +
                "</p>" +
                "</div>" +
                "<div class=\"footer\">" +
                "<p>© 2024 Edu Scan. All rights reserved.</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }
}