package com.investra.service;

import com.investra.dtos.response.NotificationDTO;
import com.investra.entity.Notification;
import com.investra.enums.NotificationType;
import com.investra.repository.NotificationRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final JavaMailSender javaMailSender;
    private final NotificationRepository notificationRepository;

    @Override
    public void sendEmail(NotificationDTO notificationDTO) {
        log.info("sendEmail() içinde çalışılıyor");

        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(
                    mimeMessage,
                    MimeMessageHelper.MULTIPART_MODE_RELATED,
                    StandardCharsets.UTF_8.name());

            helper.setTo(notificationDTO.getRecipient());
            helper.setSubject(notificationDTO.getSubject());
            helper.setText(notificationDTO.getContent(), notificationDTO.isHtml());

            javaMailSender.send(mimeMessage);
            Notification notificationToSave = Notification.builder()
                    .recipient(notificationDTO.getRecipient())
                    .subject(notificationDTO.getSubject())
                    .content(notificationDTO.getContent())
                    .type(notificationDTO.getType())
                    .isHtml(notificationDTO.isHtml())
                    .build();

            notificationRepository.save(notificationToSave);
            log.info("Notification table'ına kaydedildi: {}", notificationToSave);

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }

    }
}
