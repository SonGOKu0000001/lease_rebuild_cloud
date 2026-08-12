package com.kami.cloud.lease.common.feign.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 支付方式（内部 DTO）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentTypeDTO {
    private Long id;
    private String name;
    private String payMonthCount;
    private String additionalInfo;
}