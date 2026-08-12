package com.kami.cloud.lease.common.feign.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 公寓在租租约数量（内部 DTO）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApartmentRentCountDTO {
    private Long apartmentId;
    private Long rentCount;
}
