package com.kami.cloud.lease.common.feign.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * C 端用户信息（内部 Feign DTO）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoDTO {
    private Long id;
    private String phone;
    private String email;
    private String nickname;
    private String avatarUrl;
    private Integer status;
}