package bioCanteenApp.email.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService implements IEmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendEmail(String toEmail, String resetLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("BioCantinas — Password Reset Request");
        message.setText(
                "Hello,\n\n"
                        + "We received a request to reset your BioCantinas password.\n\n"
                        + "Click the link below to set a new password. "
                        + "This link is valid for 20 minutes:\n\n"
                        + resetLink + "\n\n"
                        + "If you did not request a password reset, please ignore this email. "
                        + "Your password will remain unchanged.\n\n"
                        + "— The BioCantinas Team"
        );
        mailSender.send(message);
    }

    @Override
    public void sendSupplierWelcomeEmail(String toEmail, String temporaryPassword) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("BioCantinas — Your Supplier Account Has Been Approved");
        message.setText(
                "Hello,\n\n"
                        + "Congratulations! Your supplier application has been approved.\n\n"
                        + "Your account credentials are:\n"
                        + "  Email: " + toEmail + "\n"
                        + "  Temporary Password: " + temporaryPassword + "\n\n"
                        + "Please log in and change your password immediately.\n\n"
                        + "— The BioCantinas Team"
        );
        mailSender.send(message);
    }

    @Override
    public void sendRejectionEmail(String toEmail, String reason) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("BioCantinas — Supplier Application Status");
        message.setText(
                "Hello,\n\n"
                        + "Thank you for applying to be a supplier for BioCantinas.\n\n"
                        + "After careful consideration, we regret to inform you that your application has not been approved at this time.\n\n"
                        + "Reason for rejection:\n"
                        + reason + "\n\n"
                        + "If you have any questions or believe this was a mistake, please reach out to our support team.\n\n"
                        + "— The BioCantinas Team"
        );
        mailSender.send(message);
    }
}
