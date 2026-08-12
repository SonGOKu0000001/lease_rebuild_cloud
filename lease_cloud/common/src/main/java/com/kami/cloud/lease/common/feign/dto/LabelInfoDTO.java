package com.kami.cloud.lease.common.feign.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 标签内部 DTO。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LabelInfoDTO {
    private Long id;
    private String name;
    private String type;
}