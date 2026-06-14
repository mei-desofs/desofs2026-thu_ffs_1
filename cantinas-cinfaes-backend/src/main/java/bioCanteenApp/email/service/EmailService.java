package bioCanteenApp.email.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class EmailService implements IEmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendSupplierWelcomeEmail(String toEmail, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("BioCantinas — Supplier Approved");
        message.setText(
                "Hello,\n\n"
                        + "Congratulations! Your supplier application has been approved.\n\n"
                        + "To activate your account, please set your password using the link below. "
                        + "This link is valid for 20 minutes:\n\n"
                        + "http://localhost:8080/api/passwords/activate-account?token=" + token + "\n"
                        + "If you have any questions, please contact us.\n\n"
                        + "— The BioCantinas Team"
        );
        mailSender.send(message);
    }

    @Override
    public void sendLockNotification(String toEmail, int lockMinutes, int failedAttempts) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("BioCantinas — Account Temporarily Locked");
            message.setText(
                    "Hello,\n\n"
                            + "We detected " + failedAttempts + " consecutive failed login attempts on your account.\n\n"
                            + "For your security, your account has been temporarily locked.\n"
                            + "You may try again in " + lockMinutes + " minutes.\n\n"
                            + "If this was not you, we recommend changing your password after regaining access.\n\n"
                            + "— The BioCantinas Team"
            );
            mailSender.send(message);
        } catch (Exception e){
            System.out.println("Failed to send lock notification email: " + e.getMessage());
        }
    }

    @Override
    public void sendNewDeviceAlert(String toEmail, String deviceInfo, LocalDateTime loginTime) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("BioCantinas — New Login Detected");
        message.setText(
                "Hello,\n\n"
                        + "A login to your account was detected from a new device or location:\n\n"
                        + "  Device:  " + deviceInfo + "\n"
                        + "  Time:    " + loginTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + "\n\n"
                        + "If this was you, no action is needed.\n"
                        + "If you do not recognise this login, change your password immediately.\n\n"
                        + "— The BioCantinas Team"
        );
        mailSender.send(message);
    }

    public void sendSupplierRejectionEmail(String toEmail, String name) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("BioCantinas — Supplier Application Update");
        message.setText(
                "Hello " + name + ",\n\n"
                        + "Thank you for your interest in becoming a BioCantinas supplier.\n\n"
                        + "After careful review, we regret to inform you that your application "
                        + "has not been approved at this time.\n\n"
                        + "— The BioCantinas Team"
        );
        mailSender.send(message);
    }
}
