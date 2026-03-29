package com.hyundai.dms.audit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogDto {

    private Long id;
    private String entityName;
    private Long entityId;
    private String action;
    private String oldValue;
    private String newValue;
    private Long performedBy;
    private String performedByUsername;
    private LocalDateTime performedAt;
    private String ipAddress;
    private String correlationId;
}
