package bioCanteenApp.email.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @Test
    void shouldSendResetEmail() {
        emailService.sendEmail("test@test.com", "http://reset-link");

        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void shouldSendSupplierWelcomeEmail() {
        emailService.sendSupplierWelcomeEmail("test@test.com", "token123");

        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void shouldSendLockNotification() {
        emailService.sendLockNotification("test@test.com", 15, 3);

        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void shouldSendNewDeviceAlert() {
        emailService.sendNewDeviceAlert(
                "test@test.com",
                "Chrome / Windows",
                LocalDateTime.now()
        );

        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void shouldSendSupplierRejectionEmail() {
        emailService.sendSupplierRejectionEmail(
                "test@test.com",
                "Invalid documents"
        );

        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void shouldHandleExceptionInLockNotification() {
        doThrow(new RuntimeException("Mail failed"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        emailService.sendLockNotification("test@test.com", 10, 2);

        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void shouldBuildCorrectResetEmailContent() {
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        emailService.sendEmail("test@test.com", "http://reset-link");

        verify(mailSender).send(captor.capture());

        SimpleMailMessage message = captor.getValue();

        assertTrue(message.getSubject().contains("Password Reset"));
        assertTrue(message.getText().contains("http://reset-link"));
    }
}