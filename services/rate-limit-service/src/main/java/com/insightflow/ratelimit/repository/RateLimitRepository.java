package com.insightflow.ratelimit.repository;

import java.util.Map;

public interface RateLimitRepository {

    int currentUserCount(String userId);

    int currentTeamCount(String teamId);

    void incrementUser(String userId);

    void incrementTeam(String teamId);

    Map<String, Integer> snapshot();

    void clear();
}
