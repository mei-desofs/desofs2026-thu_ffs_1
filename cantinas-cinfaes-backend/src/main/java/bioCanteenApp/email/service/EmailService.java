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
    public void sendLockNotification(String toEmail, int lockMinutes) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("BioCantinas — Account Temporarily Locked");
        message.setText(
                "Hello,\n\n"
                        + "We detected " + lockMinutes + " consecutive failed login attempts on your account.\n\n"
                        + "For your security, your account has been temporarily locked.\n"
                        + "You may try again in " + lockMinutes + " minutes.\n\n"
                        + "If this was not you, we recommend changing your password after regaining access.\n\n"
                        + "— The BioCantinas Team"
        );
        mailSender.send(message);
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
}
