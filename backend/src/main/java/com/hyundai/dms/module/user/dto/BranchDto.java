package com.hyundai.dms.module.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BranchDto {

    private Long id;
    private String name;
    private String code;
    private String region;
    private String gstin;
    private Boolean isActive;
}
