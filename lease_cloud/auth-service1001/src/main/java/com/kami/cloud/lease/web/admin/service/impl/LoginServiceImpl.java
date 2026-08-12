package com.kami.cloud.lease.web.admin.service.impl;

import com.kami.cloud.lease.common.constant.RedisConstant;
import com.kami.cloud.lease.common.exception.LeaseException;
import com.kami.cloud.lease.common.feign.UserFeignApi;
import com.kami.cloud.lease.common.feign.dto.SystemUserDTO;
import com.kami.cloud.lease.common.result.Result;
import com.kami.cloud.lease.common.result.ResultCodeEnum;
import com.kami.cloud.lease.common.utils.JwtUtil;
import com.kami.cloud.lease.common.utils.MD5Util;
import com.kami.cloud.lease.web.admin.service.LoginService;
import com.kami.cloud.lease.web.admin.vo.login.CaptchaVo;
import com.kami.cloud.lease.web.admin.vo.login.LoginVo;
import com.kami.cloud.lease.web.admin.vo.system.user.SystemUserInfoVo;
import com.wf.captcha.SpecCaptcha;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class LoginServiceImpl implements LoginService {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private UserFeignApi userFeignApi;

    @Override
    public CaptchaVo getCaptcha() {
        SpecCaptcha captcha = new SpecCaptcha(130, 48, 4);
        String text = captcha.text().toLowerCase();
        String key = RedisConstant.ADMIN_LOGIN_PREFIX + UUID.randomUUID();
        stringRedisTemplate.opsForValue().set(key, text, RedisConstant.ADMIN_LOGIN_CAPTCHA_TTL_SEC, TimeUnit.SECONDS);
        return new CaptchaVo(captcha.toBase64(), key);
    }

    @Override
    public String login(LoginVo loginVo) {
        if(!StringUtils.hasText(loginVo.getCaptchaCode())){
            throw new LeaseException(ResultCodeEnum.ADMIN_CAPTCHA_CODE_NOT_FOUND);
        }
        String code = stringRedisTemplate.opsForValue().get(loginVo.getCaptchaKey().trim());
        if (code == null) {
            throw new LeaseException(ResultCodeEnum.ADMIN_CAPTCHA_CODE_EXPIRED);
        }
        if (!code.equals(loginVo.getCaptchaCode().toLowerCase())) {
            throw new LeaseException(ResultCodeEnum.ADMIN_CAPTCHA_CODE_ERROR);
        }
        Result<SystemUserDTO> result = userFeignApi.systemUserByUsername(loginVo.getUsername());
        SystemUserDTO systemUser = result.getData();
        if (systemUser == null) {
            throw new LeaseException(ResultCodeEnum.ADMIN_ACCOUNT_NOT_EXIST_ERROR);
        }
        if (systemUser.getStatus() == null || systemUser.getStatus().equals(0)) {
            throw new LeaseException(ResultCodeEnum.ADMIN_ACCOUNT_DISABLED_ERROR);
        }
        if (!systemUser.getPassword().equals(MD5Util.md5Hex(loginVo.getPassword()))) {
            throw new LeaseException(ResultCodeEnum.ADMIN_ACCOUNT_ERROR);
        }
        return JwtUtil.createToken(systemUser.getId(), systemUser.getUsername());
    }

    @Override
    public SystemUserInfoVo getUserInfoById(Long userId) {
        Result<SystemUserDTO> result = userFeignApi.systemUserById(userId);
        SystemUserDTO systemUser = result.getData();
        if (systemUser == null) {
            throw new LeaseException(ResultCodeEnum.ADMIN_USER_NOT_EXIST_ERROR);
        }
        SystemUserInfoVo systemUserInfoVo = new SystemUserInfoVo();
        systemUserInfoVo.setName(systemUser.getName());
        systemUserInfoVo.setAvatarUrl(systemUser.getAvatarUrl());
        return systemUserInfoVo;
    }
}