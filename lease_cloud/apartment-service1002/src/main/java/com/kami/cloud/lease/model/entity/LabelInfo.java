package com.kami.cloud.lease.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.xiaoymin.knife4j.annotations.Ignore;
import io.swagger.v3.oas.annotations.media.Schema;
import com.kami.cloud.lease.model.enums.ItemType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "标签信息表")
@TableName(value = "label_info")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LabelInfo extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Schema(description = "类型")
    @TableField(value = "type")
    private ItemType type;

    @Schema(description = "标签名称")
    @TableField(value = "name")
    private String name;

    // 新增：用于接收中间表 room_label 的 room_id
    // exist = false 表示表中没有该列，但 MyBatis 可以映射
    @TableField(exist = false)
    @Schema(description = "房间ID")
    @JsonIgnore
    private Long roomId;

}