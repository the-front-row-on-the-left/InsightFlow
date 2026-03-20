package com.insightflow.usage.domain;

public enum UsageScopeType {
    USER("user") {
        @Override
        public boolean matches(UsageRecord record, String scopeId) {
            return record.userId().equals(scopeId);
        }
    },
    TEAM("team") {
        @Override
        public boolean matches(UsageRecord record, String scopeId) {
            return record.teamId().equals(scopeId);
        }
    },
    SERVICE("service") {
        @Override
        public boolean matches(UsageRecord record, String scopeId) {
            return record.serviceId().equals(scopeId);
        }
    };

    private final String apiValue;

    UsageScopeType(String apiValue) {
        this.apiValue = apiValue;
    }

    public String apiValue() {
        return apiValue;
    }

    public abstract boolean matches(UsageRecord record, String scopeId);
}
