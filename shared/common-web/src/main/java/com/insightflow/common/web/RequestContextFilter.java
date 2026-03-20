package com.insightflow.common.web;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component("insightRequestContextFilter")
public class RequestContextFilter extends OncePerRequestFilter {

    private static final Pattern REQUEST_ID_PATTERN = Pattern.compile("^[A-Za-z0-9_-]{8,100}$");

    public static final String REQUEST_ID_HEADER = "X-Request-Id";
    public static final String USER_ID_HEADER = "X-User-Id";
    public static final String TEAM_ID_HEADER = "X-Team-Id";
    public static final String USER_ROLE_HEADER = "X-User-Role";
    public static final String ACTOR_TYPE_HEADER = "X-Actor-Type";
    public static final String AUTHENTICATED_BY_HEADER = "X-Authenticated-By";
    public static final String DEBUG_USER_ID_HEADER = "X-Debug-User-Id";
    public static final String DEBUG_TEAM_ID_HEADER = "X-Debug-Team-Id";
    public static final String DEBUG_USER_ROLE_HEADER = "X-Debug-User-Role";

    @Value("${insightflow.defaults.user-id:u_demo_001}")
    private String defaultUserId;

    @Value("${insightflow.defaults.team-id:t_demo}")
    private String defaultTeamId;

    @Value("${insightflow.defaults.user-role:platform_user}")
    private String defaultUserRole;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String requestId = resolveRequestId(request.getHeader(REQUEST_ID_HEADER));
        String userId = resolveOrDefault(firstPresent(request.getHeader(DEBUG_USER_ID_HEADER), request.getHeader(USER_ID_HEADER)),
                defaultUserId);
        String teamId = resolveOrDefault(firstPresent(request.getHeader(DEBUG_TEAM_ID_HEADER), request.getHeader(TEAM_ID_HEADER)),
                defaultTeamId);
        String userRole = resolveOrDefault(
                firstPresent(request.getHeader(DEBUG_USER_ROLE_HEADER), request.getHeader(USER_ROLE_HEADER)),
                defaultUserRole
        );

        response.setHeader(REQUEST_ID_HEADER, requestId);
        InsightRequestContextHolder.set(new RequestContext(requestId, userId, teamId, userRole));

        try {
            filterChain.doFilter(request, response);
        } finally {
            InsightRequestContextHolder.clear();
        }
    }

    private String resolveRequestId(String requestIdHeader) {
        if (requestIdHeader == null || requestIdHeader.isBlank()) {
            return "req_" + UUID.randomUUID();
        }
        if (!REQUEST_ID_PATTERN.matcher(requestIdHeader).matches()) {
            return "req_" + UUID.randomUUID();
        }
        return requestIdHeader;
    }

    private String resolveOrDefault(String value, String defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value;
    }

    private String firstPresent(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        return second;
    }
}
