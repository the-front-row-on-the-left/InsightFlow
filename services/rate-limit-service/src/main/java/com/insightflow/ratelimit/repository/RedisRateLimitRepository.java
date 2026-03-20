package com.insightflow.ratelimit.repository;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(name = "insightflow.rate-limit.repository-type", havingValue = "redis", matchIfMissing = true)
public class RedisRateLimitRepository implements RateLimitRepository {

    private final StringRedisTemplate redisTemplate;

    public RedisRateLimitRepository(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public int currentUserCount(String userId) {
        return current("user", userId);
    }

    @Override
    public int currentTeamCount(String teamId) {
        return current("team", teamId);
    }

    @Override
    public void incrementUser(String userId) {
        increment("user", userId);
    }

    @Override
    public void incrementTeam(String teamId) {
        increment("team", teamId);
    }

    @Override
    public Map<String, Integer> snapshot() {
        Set<String> keys = redisTemplate.keys("rate_limit:*");
        Map<String, Integer> snapshot = new LinkedHashMap<>();
        if (keys == null) {
            return snapshot;
        }
        for (String key : keys) {
            String rawValue = redisTemplate.opsForValue().get(key);
            snapshot.put(key.replaceFirst("^rate_limit:", ""), rawValue == null ? 0 : Integer.parseInt(rawValue));
        }
        return snapshot;
    }

    @Override
    public void clear() {
        Set<String> keys = redisTemplate.keys("rate_limit:*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    private int current(String scope, String scopeId) {
        String value = redisTemplate.opsForValue().get(key(scope, scopeId));
        if (value == null || value.isBlank()) {
            return 0;
        }
        return Integer.parseInt(value);
    }

    private void increment(String scope, String scopeId) {
        String key = key(scope, scopeId);
        Long updated = redisTemplate.opsForValue().increment(key);
        if (updated != null && updated == 1L) {
            redisTemplate.expire(key, ttlUntilTomorrowUtc());
        }
    }

    private String key(String scope, String scopeId) {
        return "rate_limit:" + scope + ":" + LocalDate.now(ZoneOffset.UTC) + ":" + scopeId;
    }

    private Duration ttlUntilTomorrowUtc() {
        return Duration.between(java.time.Instant.now(), LocalDate.now(ZoneOffset.UTC).plusDays(1).atStartOfDay()
                .toInstant(ZoneOffset.UTC));
    }
}
