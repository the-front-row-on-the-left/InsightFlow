package com.insightflow.ratelimit.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RateLimitProperties {

    private final int userDailyLimit;
    private final int teamDailyLimit;
    private final String repositoryType;
    private final String limitAppliedTopic;

    RateLimitProperties(@Value("${insightflow.rate-limit.user-daily-limit:3}") int userDailyLimit,
                        @Value("${insightflow.rate-limit.team-daily-limit:5}") int teamDailyLimit,
                        @Value("${insightflow.rate-limit.repository-type:redis}") String repositoryType,
                        @Value("${insightflow.kafka.topics.limit-applied:limit.applied}") String limitAppliedTopic) {
        this.userDailyLimit = userDailyLimit;
        this.teamDailyLimit = teamDailyLimit;
        this.repositoryType = repositoryType;
        this.limitAppliedTopic = limitAppliedTopic;
    }

    public int userDailyLimit() {
        return userDailyLimit;
    }

    public int teamDailyLimit() {
        return teamDailyLimit;
    }

    public String repositoryType() {
        return repositoryType;
    }

    public String limitAppliedTopic() {
        return limitAppliedTopic;
    }
}
