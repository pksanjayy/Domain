package com.hyundai.dms.module.user.repository;

import com.hyundai.dms.module.user.entity.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Long>, QuerydslPredicateExecutor<Branch> {

    Optional<Branch> findByCode(String code);

    boolean existsByCode(String code);
}
