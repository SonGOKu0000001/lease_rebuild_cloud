package com.kami.cloud.lease.common.interceptor;

import com.kami.cloud.lease.common.exception.LeaseException;
import com.kami.cloud.lease.common.login.LoginUser;
import com.kami.cloud.lease.common.login.LoginUserHolder;
import com.kami.cloud.lease.common.result.ResultCodeEnum;
import com.kami.cloud.lease.common.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT 校验拦截器：读取 access-token 头，解析后写入 LoginUserHolder
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String uri = request.getRequestURI();
        if (uri.startsWith("/inner/") || uri.startsWith("/error")) {
            return true;
        }
        String token = request.getHeader("access-token");
        Claims claims = JwtUtil.parseToken(token);
        Long userId = claims.get("userId", Long.class);
        String username = claims.get("username", String.class);
        LoginUserHolder.setLoginUser(new LoginUser(userId, username));
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        LoginUserHolder.clear();
    }
}