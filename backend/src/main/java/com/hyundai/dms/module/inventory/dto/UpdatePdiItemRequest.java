package com.hyundai.dms.module.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePdiItemRequest {

    @NotBlank(message = "Result is required")
    private String result;

    private String photoUrl;

    private String remark;
}
