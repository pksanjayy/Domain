package com.hyundai.dms.module.testdrive.repository;

import com.hyundai.dms.module.testdrive.entity.TestDriveBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TestDriveBookingRepository extends JpaRepository<TestDriveBooking, Long>, QuerydslPredicateExecutor<TestDriveBooking> {
    Optional<TestDriveBooking> findByBookingId(String bookingId);
    boolean existsByBookingId(String bookingId);
    List<TestDriveBooking> findByFleetIdAndTestDriveDate(Long fleetId, LocalDate testDriveDate);
}
