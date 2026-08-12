package com.kami.cloud.lease.web.app.service;

import com.kami.cloud.lease.web.app.vo.user.LoginVo;
import com.kami.cloud.lease.web.app.vo.user.UserInfoVo;

public interface LoginService {

    String login(LoginVo loginVo);

    UserInfoVo getUserInfoVo(Long userId);

    String getEmailCode(String email);

    String refreshToken(String oldToken);
}
