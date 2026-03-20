package com.insightflow.common.web;

import java.io.IOException;
import java.util.UUID;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class RequestContextFilter extends OncePerRequestFilter {

    public static final String REQUEST_ID_HEADER = "X-Request-Id";
    public static final String USER_ID_HEADER = "X-User-Id";
    public static final String TEAM_ID_HEADER = "X-Team-Id";

    @Value("${insightflow.defaults.user-id:u_demo_001}")
    private String defaultUserId;

    @Value("${insightflow.defaults.team-id:t_demo}")
    private String defaultTeamId;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String requestId = resolveOrDefault(request.getHeader(REQUEST_ID_HEADER), "req_");
        String userId = resolveOrDefault(request.getHeader(USER_ID_HEADER), defaultUserId);
        String teamId = resolveOrDefault(request.getHeader(TEAM_ID_HEADER), defaultTeamId);

        response.setHeader(REQUEST_ID_HEADER, requestId);
        InsightRequestContextHolder.set(new RequestContext(requestId, userId, teamId));

        try {
            filterChain.doFilter(request, response);
        } finally {
            InsightRequestContextHolder.clear();
        }
    }

    private String resolveOrDefault(String value, String defaultValue) {
        if (value == null || value.isBlank()) {
            if (defaultValue.endsWith("_")) {
                return defaultValue + UUID.randomUUID();
            }
            return defaultValue;
        }
        return value;
    }
}
