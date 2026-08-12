package com.kami.cloud.lease.model.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "省份信息表")
@TableName(value = "province_info")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProvinceInfo extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Schema(description = "省份名称")
    @TableField(value = "name")
    private String name;

}