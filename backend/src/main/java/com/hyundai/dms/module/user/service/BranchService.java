package com.hyundai.dms.module.user.service;

import com.hyundai.dms.common.PageResponse;
import com.hyundai.dms.common.PageUtils;
import com.hyundai.dms.exception.ResourceNotFoundException;
import com.hyundai.dms.module.user.dto.BranchDto;
import com.hyundai.dms.module.user.dto.CreateBranchRequest;
import com.hyundai.dms.module.user.dto.UpdateBranchRequest;
import com.hyundai.dms.module.user.entity.Branch;
import com.hyundai.dms.module.user.repository.BranchRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.PathBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BranchService {

    private final BranchRepository branchRepository;

    @Transactional(readOnly = true)
    public PageResponse<BranchDto> getAllBranches(String search, Boolean isActive, Pageable pageable) {
        BooleanBuilder builder = new BooleanBuilder();
        PathBuilder<Branch> branchPath = new PathBuilder<>(Branch.class, "branch");

        if (search != null && !search.isBlank()) {
            String searchLower = search.toLowerCase();
            builder.and(
                branchPath.getString("name").containsIgnoreCase(searchLower)
                    .or(branchPath.getString("code").containsIgnoreCase(searchLower))
                    .or(branchPath.getString("gstin").containsIgnoreCase(searchLower))
            );
        }

        if (isActive != null) {
            builder.and(branchPath.getBoolean("isActive").eq(isActive));
        }

        Page<Branch> page = branchRepository.findAll(builder, pageable);
        return PageUtils.toPageResponse(page.map(this::toDto));
    }

    @Transactional(readOnly = true)
    public List<BranchDto> getBranchDropdownList() {
        return branchRepository.findAll().stream()
                .filter(b -> Boolean.TRUE.equals(b.getIsActive()))
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BranchDto getBranchById(Long id) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Branch", id));
        return toDto(branch);
    }

    @Transactional
    public BranchDto createBranch(CreateBranchRequest request) {
        Branch branch = Branch.builder()
                .code(request.getCode())
                .name(request.getName())
                .region(request.getRegion())
                .gstin(request.getGstin())
                .isActive(true)
                .build();
        branch = branchRepository.save(branch);
        log.info("Created branch: {}", branch.getCode());
        return toDto(branch);
    }

    @Transactional
    public BranchDto updateBranch(Long id, UpdateBranchRequest request) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Branch", id));
        branch.setName(request.getName());
        branch.setRegion(request.getRegion());
        branch.setGstin(request.getGstin());
        if (request.getIsActive() != null) {
            branch.setIsActive(request.getIsActive());
        }
        branch = branchRepository.save(branch);
        log.info("Updated branch: {}", branch.getCode());
        return toDto(branch);
    }

    @Transactional
    public void deleteBranch(Long id) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Branch", id));
        branch.setIsActive(false);
        branchRepository.save(branch);
        log.info("Deactivated branch: {}", branch.getCode());
    }

    private BranchDto toDto(Branch branch) {
        return BranchDto.builder()
                .id(branch.getId())
                .name(branch.getName())
                .code(branch.getCode())
                .region(branch.getRegion())
                .gstin(branch.getGstin())
                .isActive(Boolean.TRUE.equals(branch.getIsActive()))
                .build();
    }
}
