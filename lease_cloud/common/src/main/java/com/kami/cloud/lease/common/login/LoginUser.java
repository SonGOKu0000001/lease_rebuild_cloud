package com.kami.cloud.lease.common.login;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author kami
 * @description
 * @createDate 2026-07-21 11:42
 */
@Data
@AllArgsConstructor
public class LoginUser {

    private Long userId;
    private String username;
}