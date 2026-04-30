package org.toanehihi.botcv.application.token.service;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenRedisServiceImpl implements TokenService {

    @Qualifier("customStringRedisTemplate")
    private final RedisTemplate<String, String> redisTemplate;
    private static final String BLACKLIST_PREFIX = "token_blacklist:";

    @Override
    public void addToBlacklist(String token, long expiryTime) {
        String key = BLACKLIST_PREFIX + token;
        long ttl = expiryTime / 1000;
        if (ttl > 0) {
            redisTemplate.opsForValue().set(key, "blacklisted", ttl, TimeUnit.SECONDS);
            log.info("Token blacklisted with TTL: {} second", ttl);
        }
    }

    @Override
    public void addToken(String prefix, String token, String value) {
        redisTemplate.opsForValue().set(prefix + token, value, 15, TimeUnit.MINUTES);
    }

    @Override
    public boolean isBlacklisted(String token) {
        String key = BLACKLIST_PREFIX + token;
        Boolean existed = redisTemplate.hasKey(key);
        return Boolean.TRUE.equals(existed);
    }

    @Override
    public String getValue(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    @Override
    public void deleteValue(String key) {
        redisTemplate.delete(key);
    }

}
