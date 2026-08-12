package com.kami.cloud.lease.web.admin.service;

import com.kami.cloud.lease.web.admin.vo.login.CaptchaVo;
import com.kami.cloud.lease.web.admin.vo.login.LoginVo;
import com.kami.cloud.lease.web.admin.vo.system.user.SystemUserInfoVo;

public interface LoginService {

    CaptchaVo getCaptcha();

    String login(LoginVo loginVo);

    SystemUserInfoVo getUserInfoById(Long userId);
}
