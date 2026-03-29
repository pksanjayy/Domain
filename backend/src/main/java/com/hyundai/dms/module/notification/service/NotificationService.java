package com.hyundai.dms.module.notification.service;

import com.hyundai.dms.common.CursorPageResponse;
import com.hyundai.dms.common.enums.RoleName;
import com.hyundai.dms.common.logging.LogExecution;
import com.hyundai.dms.exception.BusinessRuleException;
import com.hyundai.dms.exception.ResourceNotFoundException;
import com.hyundai.dms.module.notification.dto.NotificationDto;
import com.hyundai.dms.module.notification.entity.Notification;
import com.hyundai.dms.module.notification.enums.NotificationPriority;
import com.hyundai.dms.module.notification.repository.NotificationRepository;
import com.hyundai.dms.module.user.entity.User;
import com.hyundai.dms.module.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


/**
 * Notification service — persists to DB and pushes via STOMP WebSocket.
 * CRITICAL priority notifications are pushed immediately.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SimpMessageSendingOperations messagingTemplate;

    // ── Send notifications ──

    @LogExecution
    @Transactional
    public void sendToUser(Long userId, NotificationDto dto) {
        User recipient = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        NotificationPriority priority = dto.getPriority() != null
                ? NotificationPriority.valueOf(dto.getPriority().toUpperCase())
                : NotificationPriority.LOW;

        Notification notification = Notification.builder()
                .title(dto.getTitle())
                .message(dto.getMessage())
                .module(dto.getModule())
                .priority(priority)
                .recipient(recipient)
                .isRead(false)
                .deepLink(dto.getDeepLink())
                .build();

        notification = notificationRepository.save(notification);

        // Build response DTO with persisted data
        NotificationDto responseDto = toDto(notification);

        // Push via WebSocket
        messagingTemplate.convertAndSend(
                "/topic/notifications/" + userId, responseDto);

        log.info("Notification sent to user={}: {}", userId, dto.getTitle());
    }

    @LogExecution
    @Transactional
    public void sendToRole(RoleName role, NotificationDto dto, Long excludeUserId) {
        List<User> users = userRepository.findAll().stream()
                .filter(u -> u.getRole() != null && u.getRole().getName() == role)
                .filter(u -> Boolean.TRUE.equals(u.getIsActive()))
                .filter(u -> excludeUserId == null || !Objects.equals(u.getId(), excludeUserId))
                .collect(Collectors.toList());

        for (User user : users) {
            sendToUser(user.getId(), dto);
        }

        // Removed global broadcast to prevent self-notification leak via role topics
        log.info("Notification sent to role={}: {} (excluded={})", role, dto.getTitle(), excludeUserId);
    }


    @LogExecution
    @Transactional
    public void sendToAllUsers(NotificationDto dto) {
        List<User> users = userRepository.findAll().stream()
                .filter(u -> Boolean.TRUE.equals(u.getIsActive()))
                .collect(Collectors.toList());

        for (User user : users) {
            sendToUser(user.getId(), dto);
        }

        log.info("Notification sent to all users: {}", dto.getTitle());
    }

    // ── Mark as read ──

    @LogExecution
    @Transactional
    public void markAsRead(Long notificationId, Long currentUserId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", notificationId));

        if (!notification.getRecipient().getId().equals(currentUserId)) {
            throw new BusinessRuleException("You can only mark your own notifications as read");
        }

        notification.setIsRead(true);
        notificationRepository.save(notification);
        log.info("Notification {} marked as read by user={}", notificationId, currentUserId);
    }

    @LogExecution
    @Transactional
    public void markAllAsRead(Long currentUserId) {
        int count = notificationRepository.markAllAsReadByRecipientId(currentUserId);
        log.info("Marked {} notifications as read for user={}", count, currentUserId);
    }

    // ── Read ──

    @LogExecution
    @Transactional(readOnly = true)
    public CursorPageResponse<NotificationDto> getUnread(Long userId, String cursor, int size) {
        if (size <= 0 || size > 50) size = 20;

        Long cursorId = decodeCursor(cursor);

        List<Notification> notifications = notificationRepository.findUnreadByRecipientCursor(
                userId, cursorId, PageRequest.of(0, size + 1));

        boolean hasMore = notifications.size() > size;
        if (hasMore) {
            notifications = notifications.subList(0, size);
        }

        List<NotificationDto> dtos = notifications.stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        String nextCursor = null;
        if (hasMore && !notifications.isEmpty()) {
            Notification last = notifications.get(notifications.size() - 1);
            nextCursor = encodeCursor(last.getId());
        }

        return CursorPageResponse.of(dtos, nextCursor, hasMore);
    }

    @LogExecution
    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByRecipientIdAndIsReadFalse(userId);
    }

    // ── Helpers ──

    private NotificationDto toDto(Notification notification) {
        return NotificationDto.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .module(notification.getModule())
                .priority(notification.getPriority().name())
                .recipientId(notification.getRecipient().getId())
                .isRead(notification.getIsRead())
                .deepLink(notification.getDeepLink())
                .createdAt(notification.getCreatedAt())
                .build();
    }

    private String encodeCursor(Long id) {
        return Base64.getEncoder().encodeToString(String.valueOf(id).getBytes());
    }

    private Long decodeCursor(String cursor) {
        if (cursor == null || cursor.isBlank()) return null;
        try {
            String decoded = new String(Base64.getDecoder().decode(cursor));
            return Long.parseLong(decoded);
        } catch (Exception e) {
            log.warn("Invalid cursor: {}", cursor);
            return null;
        }
    }
}
