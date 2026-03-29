package com.hyundai.dms.module.sales.dto;

import com.hyundai.dms.common.validation.ValidMobile;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCustomerRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 100)
    private String name;

    @NotBlank(message = "Mobile is required")
    @ValidMobile
    private String mobile;

    @Size(max = 150)
    private String email;

    private LocalDate dob;

    @Size(max = 150)
    private String location;

    @NotNull(message = "Branch ID is required")
    private Long branchId;
}
