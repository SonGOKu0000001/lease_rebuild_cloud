package com.kami.cloud.lease.common.feign.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 房间在租信息（内部 DTO）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomRentInfoDTO {
    private Long roomId;
    private Boolean isCheckIn;
    private Date leaseEndDate;
}
