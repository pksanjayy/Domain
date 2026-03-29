package com.hyundai.dms.config;

import com.hyundai.dms.common.validation.*;
import com.hyundai.dms.common.enums.RoleName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO used by DemoController's POST /validation-test endpoint.
 * Demonstrates all custom validators.
 *
 * Client-side validation mirrors these rules using Angular Reactive Forms —
 * field lengths, patterns, and required fields are duplicated in the frontend in Phase 6.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DemoValidationRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    @NoHtmlContent
    private String name;

    @NotBlank(message = "VIN is required")
    @ValidVin
    private String vin;

    @NotBlank(message = "Mobile is required")
    @ValidMobile
    private String mobile;

    @ValidGstin
    private String gstin;

    @ValidEnum(enumClass = RoleName.class, message = "Role must be a valid RoleName")
    private String roleName;

    @NoHtmlContent
    private String description;
}
