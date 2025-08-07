package com.investra.service;

import com.investra.dtos.response.NotificationDTO;
import com.investra.entity.Notification;
import com.investra.enums.NotificationType;
import com.investra.exception.ErrorCode;
import com.investra.exception.NotificationException;
import com.investra.repository.NotificationRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NotificationServiceImplTest {

    @Mock
    private JavaMailSender javaMailSender;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private NotificationDTO createSampleNotificationDTO() {
        return NotificationDTO.builder()
                .recipient("test@example.com")
                .subject("Test Subject")
                .content("<b>This is a test email</b>")
                .type(NotificationType.INFO)
                .isHtml(true)
                .build();
    }

    @Test
    public void sendEmail_shouldSendAndSaveNotification() throws Exception {
        NotificationDTO dto = createSampleNotificationDTO();
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        notificationService.sendEmail(dto);

        verify(javaMailSender, times(1)).send(mimeMessage);
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    public void sendEmail_shouldThrowNotificationException_whenMessagingFails() throws Exception {
        NotificationDTO dto = createSampleNotificationDTO();
        when(javaMailSender.createMimeMessage()).thenThrow(new NotificationException("Mail error"));

        NotificationException exception = assertThrows(NotificationException.class, () ->
                notificationService.sendEmail(dto));

        assertEquals(ErrorCode.NOTIFICATION_ERROR, exception.getErrorCode());
    }

    @Test
    public void sendEmail_shouldThrowNotificationException_whenUnexpectedExceptionOccurs() throws Exception {
        NotificationDTO dto = createSampleNotificationDTO();
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        doThrow(new RuntimeException("Unexpected error"))
                .when(javaMailSender).send(any(MimeMessage.class));

        NotificationException exception = assertThrows(NotificationException.class, () ->
                notificationService.sendEmail(dto));

        assertTrue(exception.getMessage().contains("Bildirim işlemi sırasında beklenmeyen bir hata oluştu"));
    }
}