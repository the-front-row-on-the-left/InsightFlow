package com.insightflow.ratelimit.repository;

import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(name = "insightflow.rate-limit.repository-type", havingValue = "in-memory")
public class InMemoryRateLimitRepository implements RateLimitRepository {

    private final ConcurrentHashMap<String, AtomicInteger> counters = new ConcurrentHashMap<>();

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
        return counters.entrySet().stream()
                .collect(java.util.stream.Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().get()));
    }

    @Override
    public void clear() {
        counters.clear();
    }

    private int current(String scope, String scopeId) {
        return counters.getOrDefault(key(scope, scopeId), new AtomicInteger()).get();
    }

    private void increment(String scope, String scopeId) {
        counters.computeIfAbsent(key(scope, scopeId), ignored -> new AtomicInteger()).incrementAndGet();
    }

    private String key(String scope, String scopeId) {
        return scope + ":" + LocalDate.now() + ":" + scopeId;
    }
}
