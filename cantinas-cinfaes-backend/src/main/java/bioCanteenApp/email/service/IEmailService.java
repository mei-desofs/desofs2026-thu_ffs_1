package bioCanteenApp.email.service;

public interface IEmailService {
    void sendEmail(String toEmail, String resetLink);
}
