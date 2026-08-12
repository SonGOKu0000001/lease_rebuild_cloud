package com.kami.cloud.lease.model.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "地区信息表")
@TableName(value = "district_info")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DistrictInfo extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Schema(description = "区域名称")
    @TableField(value = "name")
    private String name;

    @Schema(description = "所属城市id")
    @TableField(value = "city_id")
    private Integer cityId;

}