package com.kami.cloud.lease.common.utils;


import com.kami.cloud.lease.common.exception.LeaseException;
import com.kami.cloud.lease.common.result.ResultCodeEnum;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author kami
 * @description JWT工具类，提供创建token的方法等
 * @createDate 2026-07-20 20:25
 */
@Component
public class JwtUtil {
    private static long tokenExpiration = 60 * 60 * 1000L;
    private static String tokenSignKeyStr = "asiduhgiauwfhaisufhasasdasdfdasdawwsdw215";


    private static SecretKey tokenSignKey() {
        return Keys.hmacShaKeyFor(tokenSignKeyStr.getBytes());
    }

    public static String createToken(Long userId, String username) {
        String token = Jwts.builder().
                setSubject("LOGIN_USER_INFO").
                setExpiration(new Date(System.currentTimeMillis() + tokenExpiration)).
                claim("userId", userId).
                claim("username", username).
                signWith(tokenSignKey()).
                compact();
        return token;
    }

    public static String refreshToken(String oldToken) {
        Claims claims = parseToken(oldToken);
        Long userId = claims.get("userId", Long.class);
        String username = claims.get("username", String.class);
        return createToken(userId, username);
    }

    public static Claims parseToken(String token){
        if (token==null){
            throw new LeaseException(ResultCodeEnum.ADMIN_LOGIN_AUTH);
        }
        try{
            JwtParser jwtParser = Jwts.parser().setSigningKey(tokenSignKey()).build();
            return jwtParser.parseClaimsJws(token).getBody();
        }catch (ExpiredJwtException e){
            throw new LeaseException(ResultCodeEnum.TOKEN_EXPIRED);
        }catch (JwtException e){
            throw new LeaseException(ResultCodeEnum.TOKEN_INVALID);
        }
    }

    public static void main(String[] args) {
        System.out.println(createToken(1L, "13888888888"));
    }
}
