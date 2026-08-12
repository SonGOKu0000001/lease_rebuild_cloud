package com.kami.cloud.lease.common.exception;

import com.kami.cloud.lease.common.result.Result;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author kami
 * @description 处理全局异常
 * @createDate 2026-07-17 16:20
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Result handleException(Exception e) {
        e.printStackTrace();
        return Result.fail();
    }
    @ExceptionHandler(LeaseException.class)
    @ResponseBody
    public Result handleLeaseException(LeaseException e) {
        return Result.fail(e.getCode(), e.getMessage());
    }
}
