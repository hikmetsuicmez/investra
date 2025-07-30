package com.investra.dtos.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.investra.enums.NotificationType;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public class NotificationDTO {

    private Long id;

    private String subject;

    @NotBlank(message = "Recipient is required")
    private String recipient;

    private String content;

    private NotificationType type;

    private LocalDateTime createdAt;

    private boolean isHtml;

}
