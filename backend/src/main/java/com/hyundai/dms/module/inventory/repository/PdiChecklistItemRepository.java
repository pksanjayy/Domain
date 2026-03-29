package com.hyundai.dms.module.inventory.repository;

import com.hyundai.dms.module.inventory.entity.PdiChecklistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PdiChecklistItemRepository extends JpaRepository<PdiChecklistItem, Long> {

    List<PdiChecklistItem> findByChecklistId(Long checklistId);
}
