package com.kami.cloud.lease.common.feign.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 图片内部 DTO。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GraphDTO {
    private Long id;
    private String name;
    private String url;
    private Long itemId;
}