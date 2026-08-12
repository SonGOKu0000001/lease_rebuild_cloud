package com.kami.cloud.lease.web.app.controller.login;


import com.kami.cloud.lease.common.exception.LeaseException;
import com.kami.cloud.lease.common.login.LoginUser;
import com.kami.cloud.lease.common.login.LoginUserHolder;
import com.kami.cloud.lease.common.result.Result;
import com.kami.cloud.lease.common.result.ResultCodeEnum;
import com.kami.cloud.lease.web.app.service.LoginService;
import com.kami.cloud.lease.web.app.vo.user.LoginVo;
import com.kami.cloud.lease.web.app.vo.user.UserInfoVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "登录管理")
@RestController
@RequestMapping("/app/")
public class LoginController {
    @Autowired
    private LoginService service;

    @GetMapping("login/getEmailCode")
    @Operation(summary = "获取邮箱验证码")
    public Result<String> getEmailCode(@RequestParam String email) {
        String code = service.getEmailCode(email);
        return Result.ok(code);
    }

    @PostMapping("login")
    @Operation(summary = "登录")
    public Result<String> login(@RequestBody LoginVo loginVo) {
        String result = service.login(loginVo);
        return Result.ok(result);
    }

    @GetMapping("info")
    @Operation(summary = "获取登录用户信息")
    public Result<UserInfoVo> info() {
        Long userId = LoginUserHolder.getLoginUser().getUserId();
        UserInfoVo userInfoVo = service.getUserInfoVo(userId);
        return Result.ok(userInfoVo);
    }

    @PostMapping("login/refresh")
    @Operation(summary = "刷新token")
    public Result<String> refreshToken(@RequestHeader(value = "access-token", required = false) String token) {
        String newToken = service.refreshToken(token);
        return Result.ok(newToken);
    }
}

