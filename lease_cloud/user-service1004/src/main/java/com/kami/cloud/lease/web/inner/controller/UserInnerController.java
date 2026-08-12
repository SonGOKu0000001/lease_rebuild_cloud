package com.kami.cloud.lease.web.inner.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kami.cloud.lease.common.feign.dto.SystemUserDTO;
import com.kami.cloud.lease.common.feign.dto.UserCreateDTO;
import com.kami.cloud.lease.common.feign.dto.UserInfoDTO;
import com.kami.cloud.lease.common.result.Result;
import com.kami.cloud.lease.model.entity.SystemUser;
import com.kami.cloud.lease.model.entity.UserInfo;
import com.kami.cloud.lease.web.admin.mapper.SystemUserMapper;
import com.kami.cloud.lease.web.admin.mapper.UserInfoMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户服务内部接口（仅供其他服务 Feign 调用）
 */
@Tag(name = "内部接口")
@RestController
@RequestMapping("/inner")
public class UserInnerController {

    @Autowired
    private SystemUserMapper systemUserMapper;
    @Autowired
    private UserInfoMapper userInfoMapper;

    @GetMapping("/system-user/by-username")
    public Result<SystemUserDTO> systemUserByUsername(@RequestParam("username") String username) {
        if (!StringUtils.hasText(username)) {
            return Result.ok(null);
        }
        return Result.ok(convertSystemUser(systemUserMapper.selectOneByUsername(username)));
    }

    @GetMapping("/system-user/{id}")
    public Result<SystemUserDTO> systemUserById(@PathVariable("id") Long id) {
        return Result.ok(convertSystemUser(systemUserMapper.selectById(id)));
    }

    @GetMapping("/user/by-email")
    public Result<UserInfoDTO> userByEmail(@RequestParam("email") String email) {
        if (!StringUtils.hasText(email)) {
            return Result.ok(null);
        }
        LambdaQueryWrapper<UserInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserInfo::getEmail, email);
        return Result.ok(convertUserInfo(userInfoMapper.selectOne(wrapper)));
    }

    @GetMapping("/user/{userId}")
    public Result<UserInfoDTO> userById(@PathVariable("userId") Long userId) {
        return Result.ok(convertUserInfo(userInfoMapper.selectById(userId)));
    }

    @PostMapping("/user/create")
    @Transactional(rollbackFor = Exception.class)
    public Result<Long> createUser(@RequestBody UserCreateDTO createDTO) {
        UserInfo userInfo = new UserInfo();
        userInfo.setEmail(createDTO.getEmail());
        userInfo.setNickname(createDTO.getNickname());
        userInfo.setPhone("");
        userInfo.setStatus(com.kami.cloud.lease.model.enums.BaseStatus.ENABLE);
        userInfoMapper.insert(userInfo);
        return Result.ok(userInfo.getId());
    }

    private SystemUserDTO convertSystemUser(SystemUser user) {
        if (user == null) {
            return null;
        }
        SystemUserDTO dto = new SystemUserDTO();
        BeanUtils.copyProperties(user, dto);
        dto.setStatus(user.getStatus() == null ? null : user.getStatus().getCode());
        return dto;
    }

    private UserInfoDTO convertUserInfo(UserInfo user) {
        if (user == null) {
            return null;
        }
        UserInfoDTO dto = new UserInfoDTO();
        BeanUtils.copyProperties(user, dto);
        dto.setStatus(user.getStatus() == null ? null : user.getStatus().getCode());
        return dto;
    }
}