package com.multillm.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccessTokenBlacklistService {
    private final RedisTemplate<String, String> redisTemplate;
    private static final String PREFIX = "blacklist:";

    public void blacklist(String jti, long expirationMills){
        long ttlSeconds = expirationMills/1000;
        redisTemplate.opsForValue().set(PREFIX + jti, "revoked", ttlSeconds, TimeUnit.SECONDS);
        log.debug("Stored jti {} in blacklist for {} seconds", jti, ttlSeconds);
    }

    public boolean isBlacklist(String jti){
        boolean result = redisTemplate.hasKey(PREFIX + jti);
        if(result) {
            log.trace("jti {} is blacklisted", jti);
        }
        return result;
    }
}
