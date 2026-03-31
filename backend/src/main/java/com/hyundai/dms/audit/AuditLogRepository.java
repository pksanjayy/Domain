package com.hyundai.dms.audit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long>, QuerydslPredicateExecutor<AuditLog> {

    List<AuditLog> findByEntityNameAndEntityIdOrderByPerformedAtDesc(String entityName, Long entityId);

    List<AuditLog> findByPerformedByOrderByPerformedAtDesc(Long performedBy);

    List<AuditLog> findByPerformedAtBetweenOrderByPerformedAtDesc(LocalDateTime start, LocalDateTime end);
}
