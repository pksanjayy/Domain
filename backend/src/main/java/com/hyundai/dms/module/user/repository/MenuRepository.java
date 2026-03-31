package com.hyundai.dms.module.user.repository;

import com.hyundai.dms.module.user.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Long>, QuerydslPredicateExecutor<Menu> {

    @Query("SELECT m FROM Menu m WHERE m.parent IS NULL ORDER BY m.displayOrder")
    List<Menu> findAllRootMenus();

    List<Menu> findByParentIdOrderByDisplayOrder(Long parentId);

    List<Menu> findByIsActiveTrueOrderByDisplayOrder();
}
