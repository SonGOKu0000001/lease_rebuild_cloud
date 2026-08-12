package com.kami.cloud.lease.common.utils;

import cn.hutool.crypto.digest.DigestUtil;

/**
 * MD5 工具（与 lease 原 password 存储一致）
 */
public class MD5Util {

    public static String md5Hex(String text) {
        return DigestUtil.md5Hex(text);
    }
}