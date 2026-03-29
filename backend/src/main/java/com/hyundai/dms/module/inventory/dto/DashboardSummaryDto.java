package com.hyundai.dms.module.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryDto {
    private long totalStock;
    private long available;
    private long onHold;
    private long booked;
    private List<AgeingBucketDto> ageingBuckets;
    private Map<String, Long> statusBreakdown;
    private List<BranchDistributionDto> branchDistribution;
    private Map<String, Map<String, Long>> branchBreakdown;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BranchDistributionDto {
        private String branchName;
        private long count;
    }
}
