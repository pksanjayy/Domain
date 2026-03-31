package com.hyundai.dms.module.sales.repository;

import com.hyundai.dms.module.sales.entity.Lead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface LeadRepository extends JpaRepository<Lead, Long>, QuerydslPredicateExecutor<Lead> {
}
