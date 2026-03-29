package com.hyundai.dms.module.notification.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotificationDto {

    private Long id;
    private String title;
    private String message;
    private String module;
    private String priority;
    private Long recipientId;
    private Boolean isRead;
    private String deepLink;
    private LocalDateTime createdAt;
}
