package com.hyundai.dms.module.user.service;

import com.hyundai.dms.common.PageResponse;
import com.hyundai.dms.common.entity.Code;
import com.hyundai.dms.common.repository.CodeRepository;
import com.hyundai.dms.exception.DuplicateResourceException;
import com.hyundai.dms.exception.ResourceNotFoundException;
import com.hyundai.dms.module.user.dto.CodeDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CodeService {

    private final CodeRepository codeRepository;

    @Transactional(readOnly = true)
    public PageResponse<CodeDto> getAllCodes(Pageable pageable) {
        Page<Code> page = codeRepository.findAll(pageable);
        return PageResponse.<CodeDto>builder()
                .content(page.getContent().stream().map(this::toDto).collect(Collectors.toList()))
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "codes", key = "#category")
    public List<CodeDto> getByCategory(String category) {
        return codeRepository.findByCategoryOrderByDisplayOrder(category).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @CacheEvict(value = "codes", allEntries = true)
    public CodeDto createCode(CodeDto request) {
        String correlationId = MDC.get("correlationId");

        if (codeRepository.existsByCategoryAndCode(request.getCategory(), request.getCode())) {
            throw new DuplicateResourceException("Code", "category+code",
                    request.getCategory() + "/" + request.getCode());
        }

        Code code = Code.builder()
                .category(request.getCategory())
                .code(request.getCode())
                .label(request.getLabel())
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .build();

        code = codeRepository.save(code);
        log.info("[{}] Created code: {}/{}", correlationId, code.getCategory(), code.getCode());
        return toDto(code);
    }

    @Transactional
    @CacheEvict(value = "codes", allEntries = true)
    public CodeDto updateCode(Long id, CodeDto request) {
        String correlationId = MDC.get("correlationId");

        Code code = codeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Code", id));

        if (request.getCategory() != null) code.setCategory(request.getCategory());
        if (request.getCode() != null) code.setCode(request.getCode());
        if (request.getLabel() != null) code.setLabel(request.getLabel());
        if (request.getDisplayOrder() != null) code.setDisplayOrder(request.getDisplayOrder());

        code = codeRepository.save(code);
        log.info("[{}] Updated code: {}/{}", correlationId, code.getCategory(), code.getCode());
        return toDto(code);
    }

    @Transactional
    @CacheEvict(value = "codes", allEntries = true)
    public void deleteCode(Long id) {
        String correlationId = MDC.get("correlationId");

        Code code = codeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Code", id));

        codeRepository.delete(code);
        log.info("[{}] Deleted code: {}/{}", correlationId, code.getCategory(), code.getCode());
    }

    private CodeDto toDto(Code code) {
        return CodeDto.builder()
                .id(code.getId())
                .category(code.getCategory())
                .code(code.getCode())
                .label(code.getLabel())
                .displayOrder(code.getDisplayOrder())
                .build();
    }
}
