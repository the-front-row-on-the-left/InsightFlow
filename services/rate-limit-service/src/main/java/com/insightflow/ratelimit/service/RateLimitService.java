package com.insightflow.ratelimit.service;

import java.util.Map;

import com.insightflow.ratelimit.controller.dto.RateLimitCheckRequest;
import com.insightflow.ratelimit.controller.dto.RateLimitCheckResponse;
import com.insightflow.ratelimit.config.RateLimitProperties;
import com.insightflow.ratelimit.event.RateLimitEventPublisher;
import com.insightflow.ratelimit.repository.RateLimitRepository;
import com.insightflow.common.error.BusinessException;
import com.insightflow.common.error.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class RateLimitService {

    private final RateLimitProperties properties;
    private final RateLimitRepository rateLimitRepository;
    private final RateLimitEventPublisher eventPublisher;

    RateLimitService(RateLimitProperties properties,
                     RateLimitRepository rateLimitRepository,
                     RateLimitEventPublisher eventPublisher) {
        this.properties = properties;
        this.rateLimitRepository = rateLimitRepository;
        this.eventPublisher = eventPublisher;
    }

    public RateLimitCheckResponse check(RateLimitCheckRequest request) {
        validate(request);

        int currentUserCount = rateLimitRepository.currentUserCount(request.userId());
        if (currentUserCount >= properties.userDailyLimit()) {
            RateLimitCheckResponse response = new RateLimitCheckResponse(
                    false,
                    "user",
                    request.userId(),
                    0,
                    "BLOCKED",
                    "USER_DAILY_LIMIT"
            );
            eventPublisher.publish(request, response);
            return response;
        }

        int currentTeamCount = rateLimitRepository.currentTeamCount(request.teamId());
        if (currentTeamCount >= properties.teamDailyLimit()) {
            RateLimitCheckResponse response = new RateLimitCheckResponse(
                    false,
                    "team",
                    request.teamId(),
                    0,
                    "BLOCKED",
                    "TEAM_DAILY_LIMIT"
            );
            eventPublisher.publish(request, response);
            return response;
        }

        rateLimitRepository.incrementUser(request.userId());
        rateLimitRepository.incrementTeam(request.teamId());

        int remainingQuota = Math.min(
                properties.userDailyLimit() - rateLimitRepository.currentUserCount(request.userId()),
                properties.teamDailyLimit() - rateLimitRepository.currentTeamCount(request.teamId())
        );
        RateLimitCheckResponse response = new RateLimitCheckResponse(
                true,
                "user",
                request.userId(),
                Math.max(remainingQuota, 0),
                "PASSED",
                "USER_DAILY_LIMIT"
        );
        eventPublisher.publish(request, response);
        return response;
    }

    public Map<String, Object> counters() {
        return Map.of(
                "user_daily_limit", properties.userDailyLimit(),
                "team_daily_limit", properties.teamDailyLimit(),
                "repository_type", properties.repositoryType(),
                "counters", rateLimitRepository.snapshot(),
                "events", eventPublisher.snapshot()
        );
    }

    private void validate(RateLimitCheckRequest request) {
        if (request == null || isBlank(request.requestId()) || isBlank(request.userId()) || isBlank(request.teamId())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ErrorCode.INVALID_REQUEST,
                    "request_id, user_id, and team_id are required.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
