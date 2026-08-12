package com.kami.cloud.lease.common.feign;

import com.kami.cloud.lease.common.feign.dto.SystemUserDTO;
import com.kami.cloud.lease.common.feign.dto.UserCreateDTO;
import com.kami.cloud.lease.common.feign.dto.UserInfoDTO;
import com.kami.cloud.lease.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 用户服务内部 Feign API（不走网关，供认证服务调用）
 */
@FeignClient(name = "lease-user-service", contextId = "userInnerApi")
public interface UserFeignApi {

    @GetMapping("/inner/system-user/by-username")
    Result<SystemUserDTO> systemUserByUsername(@RequestParam("username") String username);

    @GetMapping("/inner/system-user/{id}")
    Result<SystemUserDTO> systemUserById(@PathVariable("id") Long id);

    @GetMapping("/inner/user/by-email")
    Result<UserInfoDTO> userByEmail(@RequestParam("email") String email);

    @GetMapping("/inner/user/{userId}")
    Result<UserInfoDTO> userById(@PathVariable("userId") Long userId);

    @PostMapping("/inner/user/create")
    Result<Long> createUser(@RequestBody UserCreateDTO createDTO);
}