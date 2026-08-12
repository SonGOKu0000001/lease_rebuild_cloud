package com.kami.cloud.lease.web.app.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.kami.cloud.lease.common.constant.RedisConstant;
import com.kami.cloud.lease.common.exception.LeaseException;
import com.kami.cloud.lease.common.feign.UserFeignApi;
import com.kami.cloud.lease.common.feign.dto.UserCreateDTO;
import com.kami.cloud.lease.common.feign.dto.UserInfoDTO;
import com.kami.cloud.lease.common.result.Result;
import com.kami.cloud.lease.common.result.ResultCodeEnum;
import com.kami.cloud.lease.common.utils.JwtUtil;
import com.kami.cloud.lease.web.app.service.LoginService;
import com.kami.cloud.lease.web.app.vo.user.LoginVo;
import com.kami.cloud.lease.web.app.vo.user.UserInfoVo;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginServiceImpl implements LoginService {
    private final StringRedisTemplate redisTemplate;
    private final UserFeignApi userFeignApi;

    @Override
    @GlobalTransactional(name = "lease-cloud-tx-login", rollbackFor = Exception.class)
    public String login(LoginVo loginVo) {
        //1. 检查邮箱账号是否为空
        if (!StringUtils.hasText(loginVo.getEmail())) {
            throw new LeaseException(ResultCodeEnum.APP_LOGIN_EMAIL_EMPTY);
        }
        //2. 检查验证码
        if (!StringUtils.hasText(loginVo.getCode())) {
            throw new LeaseException(ResultCodeEnum.APP_LOGIN_CODE_EMPTY);
        }
        String key = RedisConstant.APP_LOGIN_PREFIX + loginVo.getEmail();
        String code = redisTemplate.opsForValue().get(key);
        if (code == null) {
            throw new LeaseException(ResultCodeEnum.APP_LOGIN_CODE_EXPIRED);
        }
        if (!code.equals(loginVo.getCode())) {
            throw new LeaseException(ResultCodeEnum.APP_LOGIN_CODE_ERROR);
        }
        //3.判断用户是否存在,不存在则注册（创建用户）
        Result<UserInfoDTO> result = userFeignApi.userByEmail(loginVo.getEmail());
        UserInfoDTO userInfo = result.getData();
        if (userInfo == null) {
            Long userId = userFeignApi.createUser(
                    new UserCreateDTO(loginVo.getEmail(), "用户-" + RandomUtil.randomNumbers(6))).getData();
            userInfo = userFeignApi.userById(userId).getData();
        }
        //4.判断用户是否被禁
        if (userInfo.getStatus() == null || userInfo.getStatus().equals(0)) {
            throw new LeaseException(ResultCodeEnum.APP_ACCOUNT_DISABLED_ERROR);
        }
        //5.生成token并返回
        return JwtUtil.createToken(userInfo.getId(), loginVo.getEmail());
    }

    @Override
    public UserInfoVo getUserInfoVo(Long userId) {
        Result<UserInfoDTO> result = userFeignApi.userById(userId);
        UserInfoDTO userInfo = result.getData();
        if (userInfo == null) {
            throw new LeaseException(ResultCodeEnum.APP_USER_NOT_EXIST_ERROR);
        }
        return new UserInfoVo(userInfo.getNickname(), userInfo.getAvatarUrl());
    }

    @Override
    public String refreshToken(String oldToken) {
        return JwtUtil.refreshToken(oldToken);
    }

    @Override
    public String getEmailCode(String email) {
        // 1. 检查邮箱是否为空
        if (!StringUtils.hasText(email)) {
            throw new LeaseException(ResultCodeEnum.APP_LOGIN_PHONE_EMPTY);
        }
        //2. 生成6位数字验证码
        String code = RandomUtil.randomNumbers(6);
        //3. 检查Redis中是否已经存在该邮箱的key
        String key = RedisConstant.APP_LOGIN_PREFIX + email;
        boolean hasKey = redisTemplate.hasKey(key);
        if (hasKey) {
            //若存在，则检查其存在的时间
            Long expire = redisTemplate.getExpire(key, TimeUnit.SECONDS);
            if (RedisConstant.APP_LOGIN_CODE_TTL_SEC - expire < RedisConstant.APP_LOGIN_CODE_RESEND_TIME_SEC) {
                //若存在时间不足1分钟，响应发送过于频繁
                throw new LeaseException(ResultCodeEnum.APP_SEND_SMS_TOO_OFTEN);
            }
        }
        //4. 将验证码存入Redis（演示模式：不真实发邮件，验证码直接返回给前端）
        redisTemplate.opsForValue().set(key, code, RedisConstant.APP_LOGIN_CODE_TTL_SEC, TimeUnit.SECONDS);
        return code;
    }
}