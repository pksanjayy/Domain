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
import org.springframework.data.jpa.domain.Specification;
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

        Specification<AuditLog> spec = Specification.where(null);

        if (from != null && to != null) {
            spec = spec.and((root, query, cb) ->
                    cb.between(root.get("performedAt"), from, to));
        } else if (from != null) {
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("performedAt"), from));
        } else if (to != null) {
            spec = spec.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("performedAt"), to));
        }

        if (entityName != null && !entityName.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("entityName"), entityName));
        }

        if (action != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("action"), action));
        }

        if (performedBy != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("performedBy"), performedBy));
        }

        Page<AuditLog> page = auditLogRepository.findAll(spec, pageable);

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

        Specification<AuditLog> spec = Specification.where(null);

        if (from != null) {
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("performedAt"), from));
        }
        if (to != null) {
            spec = spec.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("performedAt"), to));
        }
        if (entityName != null && !entityName.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("entityName"), entityName));
        }
        if (action != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("action"), action));
        }
        if (performedBy != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("performedBy"), performedBy));
        }

        var logs = auditLogRepository.findAll(spec);
        var userIds = logs.stream()
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
