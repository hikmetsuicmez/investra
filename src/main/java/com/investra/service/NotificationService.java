package com.investra.service;

import com.investra.dtos.response.NotificationDTO;

public interface NotificationService {

    void sendEmail(NotificationDTO notificationDTO);
}
