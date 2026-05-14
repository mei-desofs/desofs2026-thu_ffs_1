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
}
