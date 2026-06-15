package bioCanteenApp.email.service;

import java.time.LocalDateTime;

public interface IEmailService {
    void sendEmail(String toEmail, String resetLink);
    void sendSupplierWelcomeEmail(String toEmail, String temporaryPassword);
    void sendLockNotification(String toEmail, int lockMinutes, int failedAttempts);
    void sendNewDeviceAlert(String toEmail, String deviceInfo, LocalDateTime loginTime);
    void sendSupplierRejectionEmail(String email, String name);
}
