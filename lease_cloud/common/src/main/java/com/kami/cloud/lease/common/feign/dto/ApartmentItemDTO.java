package com.kami.cloud.lease.common.feign.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 公寓明细（含图片/标签/起租价）内部 DTO，供预约详情、租约详情引用。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApartmentItemDTO {
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
    private BigDecimal minRent;
    private List<LabelInfoDTO> labelInfoList;
    private List<GraphDTO> graphVoList;
}