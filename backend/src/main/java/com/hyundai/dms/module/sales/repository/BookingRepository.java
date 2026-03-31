package com.hyundai.dms.module.sales.repository;

import com.hyundai.dms.module.sales.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long>, QuerydslPredicateExecutor<Booking> {

    Optional<Booking> findByLeadId(Long leadId);

    boolean existsByLeadId(Long leadId);
}
