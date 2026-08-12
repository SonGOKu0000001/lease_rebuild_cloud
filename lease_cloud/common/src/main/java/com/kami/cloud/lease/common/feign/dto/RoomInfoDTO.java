package com.kami.cloud.lease.common.feign.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 房间信息（内部 DTO）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomInfoDTO {
    private Long id;
    private String roomNumber;
    private BigDecimal rent;
    private Long apartmentId;
    private Integer isRelease;
}