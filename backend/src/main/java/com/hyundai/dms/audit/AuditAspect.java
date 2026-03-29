package com.hyundai.dms.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyundai.dms.common.enums.ActionType;
import com.hyundai.dms.common.enums.RoleName;
import com.hyundai.dms.module.notification.dto.NotificationDto;
import com.hyundai.dms.module.notification.service.NotificationService;
import com.hyundai.dms.security.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

    private final AuditLogRepository auditLogRepository;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;


    @AfterReturning(
            pointcut = "@annotation(audited)",
            returning = "result",
            argNames = "joinPoint,audited,result"
    )
    public void afterAuditedMethod(JoinPoint joinPoint, Audited audited, Object result) {
        try {
            String entityName = audited.entity();
            ActionType action = audited.action();
            Long entityId = extractEntityId(result, joinPoint.getArgs());
            String newValue = serializeToJson(result);
            Long performedBy = getCurrentUserId();
            String ipAddress = getClientIpAddress();
            String correlationId = MDC.get("correlationId");

            persistAuditLog(entityName, entityId, action, null, newValue, performedBy, ipAddress, correlationId);
            
            // Trigger real-time notifications for CRUD changes
            triggerCrudNotification(entityName, entityId, action, performedBy);
        } catch (Exception e) {

            log.error("Failed to create audit log: {}", e.getMessage(), e);
        }
    }

    @Async("auditTaskExecutor")
    public void persistAuditLog(String entityName, Long entityId, ActionType action,
                                 String oldValue, String newValue, Long performedBy,
                                 String ipAddress, String correlationId) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .entityName(entityName)
                    .entityId(entityId != null ? entityId : 0L)
                    .action(action)
                    .oldValue(oldValue)
                    .newValue(newValue)
                    .performedBy(performedBy)
                    .performedAt(LocalDateTime.now())
                    .ipAddress(ipAddress)
                    .correlationId(correlationId)
                    .build();

            auditLogRepository.save(auditLog);
            log.debug("Audit log saved: {} {} on {}#{}", action, entityName, entityName, entityId);
        } catch (Exception e) {
            log.error("Failed to persist audit log: {}", e.getMessage(), e);
        }
    }

    private Long extractEntityId(Object result, Object[] args) {
        if (result != null) {
            try {
                Method getId = result.getClass().getMethod("getId");
                Object id = getId.invoke(result);
                if (id instanceof Long) {
                    return (Long) id;
                }
            } catch (Exception ignored) {
                // Result doesn't have getId(), try args
            }
        }

        // Try first Long argument as entity ID
        for (Object arg : args) {
            if (arg instanceof Long) {
                return (Long) arg;
            }
        }

        return null;
    }

    private String serializeToJson(Object obj) {
        try {
            if (obj == null) return null;
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.warn("Failed to serialize audit value: {}", e.getMessage());
            return null;
        }
    }

    private void triggerCrudNotification(String entityName, Long entityId, ActionType action, Long performedBy) {
        log.debug("Triggering notification for {} {} by user ID: {}", action, entityName, performedBy);
        try {

            // Determine target roles based on entity
            RoleName[] targetRoles;
            switch (entityName) {
                case "Lead":
                case "Customer":
                case "Booking":
                    targetRoles = new RoleName[]{RoleName.SALES_CRM_EXEC, RoleName.MASTER_USER, RoleName.SUPER_ADMIN};
                    break;
                case "Vehicle":
                case "StockTransfer":
                case "Transfer":
                case "ServiceRecord":
                case "TestDriveFleet":
                case "TestDriveBooking":
                case "GrnRecord":
                case "Grn":
                    targetRoles = new RoleName[]{RoleName.WORKSHOP_EXEC, RoleName.MASTER_USER, RoleName.SUPER_ADMIN};
                    break;
                case "User":
                case "Role":
                case "Branch":
                    targetRoles = new RoleName[]{RoleName.SUPER_ADMIN};
                    break;
                default:
                    targetRoles = new RoleName[]{RoleName.SUPER_ADMIN};
            }

            NotificationDto dto = NotificationDto.builder()
                    .title(String.format("%s: %s", action.name(), entityName))
                    .message(String.format("New activity: %s performed on %s (ID: %s)", 
                            action.name().toLowerCase(), entityName, entityId))
                    .module(entityName.toUpperCase())
                    .priority("LOW")
                    .build();

            for (RoleName role : targetRoles) {
                notificationService.sendToRole(role, dto, performedBy);
            }
        } catch (Exception e) {
            log.error("Failed to trigger CRUD notification: {}", e.getMessage());
        }
    }

    private Long getCurrentUserId() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails userDetails) {
            return userDetails.getId();
        }
        return null;
    }

    private String getClientIpAddress() {
        try {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String xForwardedFor = request.getHeader("X-Forwarded-For");
                if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                    return xForwardedFor.split(",")[0].trim();
                }
                return request.getRemoteAddr();
            }
        } catch (Exception ignored) {
            // Not in request context
        }
        return null;
    }
}
