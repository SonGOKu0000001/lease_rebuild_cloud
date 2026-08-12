package com.kami.cloud.lease.common.feign.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 公寓信息（Feign 内部 DTO，供 agreement 服务消费）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApartmentInfoDTO {
    private Long id;
    private String name;
    private String introduction;
    private Long districtId;
    private String districtName;
    private Long cityId;
    private String cityName;
    private Long provinceId;
    private String provinceName;
    private String addressDetail;
    private String latitude;
    private String longitude;
    private String phone;
    private Integer isRelease;
}