package com.investra.service;

import com.investra.dtos.response.NotificationDTO;
import com.investra.entity.Order;
import com.investra.entity.User;

public interface NotificationService {

    void sendEmail(NotificationDTO notificationDTO);
    void sendOrderCompletedNotification(User user, Order order);
    void sendOrderCancelledNotification(User user, Order order) ;

}
