package com.kami.cloud.lease.common.feign.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * C 端用户创建入参（app 邮箱登录自动注册）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateDTO {
    private String email;
    private String nickname;
}