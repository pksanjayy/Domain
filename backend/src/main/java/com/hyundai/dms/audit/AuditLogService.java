package com.hyundai.dms.audit;

import com.hyundai.dms.common.PageResponse;
import com.hyundai.dms.common.PageUtils;
import com.hyundai.dms.common.enums.ActionType;
import com.hyundai.dms.module.user.entity.User;
import com.hyundai.dms.module.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.PathBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public PageResponse<AuditLogDto> getAuditLogs(
            LocalDateTime from, LocalDateTime to,
            String entityName, ActionType action,
            Long performedBy, Pageable pageable) {

        BooleanBuilder builder = new BooleanBuilder();
        PathBuilder<AuditLog> auditPath = new PathBuilder<>(AuditLog.class, "auditLog");

        if (from != null && to != null) {
            builder.and(auditPath.getDateTime("performedAt", LocalDateTime.class).between(from, to));
        } else if (from != null) {
            builder.and(auditPath.getDateTime("performedAt", LocalDateTime.class).goe(from));
        } else if (to != null) {
            builder.and(auditPath.getDateTime("performedAt", LocalDateTime.class).loe(to));
        }

        if (entityName != null && !entityName.isBlank()) {
            builder.and(auditPath.getString("entityName").eq(entityName));
        }

        if (action != null) {
            builder.and(auditPath.get("action").eq(action));
        }

        if (performedBy != null) {
            builder.and(auditPath.getNumber("performedBy", Long.class).eq(performedBy));
        }

        Page<AuditLog> page = auditLogRepository.findAll(builder, pageable);

        // Resolve usernames for performedBy IDs
        var userIds = page.getContent().stream()
                .map(AuditLog::getPerformedBy)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        Map<Long, String> userNameMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, User::getUsername));

        Page<AuditLogDto> dtoPage = page.map(al -> toDto(al, userNameMap));
        return PageUtils.toPageResponse(dtoPage);
    }

    private AuditLogDto toDto(AuditLog al, Map<Long, String> userNameMap) {
        return AuditLogDto.builder()
                .id(al.getId())
                .entityName(al.getEntityName())
                .entityId(al.getEntityId())
                .action(al.getAction() != null ? al.getAction().name() : null)
                .oldValue(al.getOldValue())
                .newValue(al.getNewValue())
                .performedBy(al.getPerformedBy())
                .performedByUsername(al.getPerformedBy() != null
                        ? userNameMap.getOrDefault(al.getPerformedBy(), "Unknown")
                        : "System")
                .performedAt(al.getPerformedAt())
                .ipAddress(al.getIpAddress())
                .correlationId(al.getCorrelationId())
                .build();
    }

    @Transactional(readOnly = true)
    public byte[] exportAuditLogsCsv(
            LocalDateTime from, LocalDateTime to,
            String entityName, ActionType action, Long performedBy) {

        BooleanBuilder exportBuilder = new BooleanBuilder();
        PathBuilder<AuditLog> exportPath = new PathBuilder<>(AuditLog.class, "auditLog");

        if (from != null) {
            exportBuilder.and(exportPath.getDateTime("performedAt", LocalDateTime.class).goe(from));
        }
        if (to != null) {
            exportBuilder.and(exportPath.getDateTime("performedAt", LocalDateTime.class).loe(to));
        }
        if (entityName != null && !entityName.isBlank()) {
            exportBuilder.and(exportPath.getString("entityName").eq(entityName));
        }
        if (action != null) {
            exportBuilder.and(exportPath.get("action").eq(action));
        }
        if (performedBy != null) {
            exportBuilder.and(exportPath.getNumber("performedBy", Long.class).eq(performedBy));
        }

        var logs = auditLogRepository.findAll(exportBuilder);
        var userIds = java.util.stream.StreamSupport.stream(logs.spliterator(), false)
                .map(AuditLog::getPerformedBy)
                .filter(id -> id != null)
                .collect(java.util.stream.Collectors.toSet());
        Map<Long, String> userNameMap = userRepository.findAllById(userIds).stream()
                .collect(java.util.stream.Collectors.toMap(User::getId, User::getUsername));

        StringBuilder sb = new StringBuilder();
        sb.append("ID,Entity,EntityID,Action,PerformedBy,PerformedAt,IPAddress\n");
        for (AuditLog al : logs) {
            sb.append(String.join(",",
                    String.valueOf(al.getId()),
                    al.getEntityName() != null ? al.getEntityName() : "",
                    al.getEntityId() != null ? String.valueOf(al.getEntityId()) : "",
                    al.getAction() != null ? al.getAction().name() : "",
                    al.getPerformedBy() != null ? userNameMap.getOrDefault(al.getPerformedBy(), "Unknown") : "System",
                    al.getPerformedAt() != null ? al.getPerformedAt().toString() : "",
                    al.getIpAddress() != null ? al.getIpAddress() : ""
            )).append("\n");
        }
        return sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }
}
