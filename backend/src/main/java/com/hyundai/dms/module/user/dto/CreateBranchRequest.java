package com.hyundai.dms.module.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBranchRequest {

    @NotBlank(message = "Branch code is required")
    @Size(max = 20)
    private String code;

    @NotBlank(message = "Branch name is required")
    @Size(max = 100)
    private String name;

    @Size(max = 50)
    private String region;

    @Size(max = 15)
    private String gstin;
}
