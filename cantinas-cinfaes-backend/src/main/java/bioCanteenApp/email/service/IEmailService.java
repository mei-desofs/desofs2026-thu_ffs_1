package bioCanteenApp.email.service;

public interface IEmailService {
    void sendEmail(String toEmail, String resetLink);
    void sendSupplierWelcomeEmail(String toEmail, String temporaryPassword);
}
