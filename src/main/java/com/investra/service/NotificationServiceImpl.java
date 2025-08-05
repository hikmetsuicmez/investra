package com.investra.service;

import com.investra.dtos.response.NotificationDTO;
import com.investra.entity.Notification;
import com.investra.entity.Order;
import com.investra.entity.User;
import com.investra.enums.NotificationType;
import com.investra.exception.NotificationException;
import com.investra.repository.NotificationRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

            log.debug("MimeMessage hazırlandı - To: {}, Subject: {}, HTML: {}",
                    notificationDTO.getRecipient(), notificationDTO.getSubject(), notificationDTO.isHtml());

            javaMailSender.send(mimeMessage);
            log.info("E-posta başarıyla gönderildi - Alıcı: {}", notificationDTO.getRecipient());

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
            log.error("E-posta gönderimi sırasında hata oluştu: {}", e.getMessage());
            throw new NotificationException("E-posta gönderimi sırasında bir hata oluştu", e);
        } catch (Exception e) {
            log.error("Bildirim işlemi sırasında beklenmeyen bir hata oluştu: {}", e.getMessage());
            throw new NotificationException("Bildirim işlemi sırasında beklenmeyen bir hata oluştu", e);
        }
    }

    @Transactional
    public void sendOrderCompletedNotification(User user, Order order) {
        Notification notification = Notification.builder()
                .recipient(user.getUsername()) // User nesnesi yerine username kullanıyoruz
                .subject("Emir Gerçekleşti") // title yerine subject kullanıyoruz
                .content(order.getStockSymbol() + " hissesi için " + order.getOrderType().getDisplayName() +
                        " emriniz başarıyla gerçekleşmiştir.") // message yerine content kullanıyoruz
                .type(NotificationType.ORDER)
                .build();

        notificationRepository.save(notification);
    }

    @Transactional
    public void sendOrderCancelledNotification(User user, Order order) {
        Notification notification = Notification.builder()
                .recipient(user.getUsername()) // User nesnesi yerine username kullanıyoruz
                .subject("Emir İptal Edildi") // title yerine subject kullanıyoruz
                .content(order.getStockSymbol() + " hissesi için " + order.getOrderType().getDisplayName() +
                        " emriniz iptal edilmiştir.") // message yerine content kullanıyoruz
                .type(NotificationType.ORDER)
                .build();

        notificationRepository.save(notification);
    }
}