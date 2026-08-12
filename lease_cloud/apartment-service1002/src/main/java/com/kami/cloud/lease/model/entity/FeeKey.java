package com.kami.cloud.lease.model.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Schema(description = "杂项费用名称表")
@TableName(value = "fee_key")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeeKey extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Schema(description = "付款项key")
    @TableField(value = "name")
    private String name;


}