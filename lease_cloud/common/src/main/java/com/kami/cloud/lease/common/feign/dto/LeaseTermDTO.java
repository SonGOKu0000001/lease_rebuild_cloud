package com.kami.cloud.lease.common.feign.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 租期（内部 DTO）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaseTermDTO {
    private Long id;
    private Integer monthCount;
    private String unit;
}