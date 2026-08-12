package com.kami.cloud.lease.common.feign.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 后台系统用户（内部 DTO，供认证服务登录校验使用）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SystemUserDTO {
    private Long id;
    private String username;
    private String password;
    private String name;
    private String phone;
    private String avatarUrl;
    private Long postId;
    private Integer status;
}