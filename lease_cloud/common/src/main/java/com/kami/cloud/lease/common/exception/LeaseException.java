package com.kami.cloud.lease.common.exception;

import com.kami.cloud.lease.common.result.ResultCodeEnum;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * @author kami
 * @description
 * @createDate 2026-07-18 15:09
 */
@Slf4j
@Data
public class LeaseException extends RuntimeException {
    private Integer code;
    public LeaseException(Integer code, String message) {
        super(message);
        this.code = code;
    }
    public LeaseException(ResultCodeEnum resultCodeEnum) {
        super(resultCodeEnum.getMessage());
        this.code = resultCodeEnum.getCode();
    }
}
