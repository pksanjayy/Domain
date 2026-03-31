package com.hyundai.dms.module.user.repository;

import com.hyundai.dms.module.user.entity.Permission;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long>, QuerydslPredicateExecutor<Permission> {

    List<Permission> findByRoleId(Long roleId);

    @Modifying
    @Query("DELETE FROM Permission p WHERE p.role.id = :roleId")
    void deleteByRoleId(Long roleId);
}
