package com.hyundai.dms.module.notification.repository;

import com.hyundai.dms.module.notification.entity.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Cursor-based pagination: returns unread notifications for a user
     * with id < cursorId (newest first). When cursorId is null, returns from the top.
     */
    @Query("SELECT n FROM Notification n WHERE n.recipient.id = :recipientId " +
           "AND n.isRead = false " +
           "AND (:cursorId IS NULL OR n.id < :cursorId) " +
           "ORDER BY n.id DESC")
    List<Notification> findUnreadByRecipientCursor(
            @Param("recipientId") Long recipientId,
            @Param("cursorId") Long cursorId,
            Pageable pageable);

    long countByRecipientIdAndIsReadFalse(Long recipientId);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.recipient.id = :recipientId AND n.isRead = false")
    int markAllAsReadByRecipientId(@Param("recipientId") Long recipientId);
}
