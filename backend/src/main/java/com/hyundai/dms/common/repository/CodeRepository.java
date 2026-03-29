package com.hyundai.dms.common.repository;

import com.hyundai.dms.common.entity.Code;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CodeRepository extends JpaRepository<Code, Long>, JpaSpecificationExecutor<Code> {

    List<Code> findByCategoryOrderByDisplayOrder(String category);

    boolean existsByCategoryAndCode(String category, String code);
}
