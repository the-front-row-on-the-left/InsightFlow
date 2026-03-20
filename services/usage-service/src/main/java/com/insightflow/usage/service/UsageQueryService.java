package com.insightflow.usage.service;

import com.insightflow.usage.domain.UsageQuery;
import com.insightflow.usage.domain.UsageRecord;
import com.insightflow.usage.domain.UsageScopeType;
import com.insightflow.usage.dto.UsageItem;
import com.insightflow.usage.dto.UsagePeriod;
import com.insightflow.usage.dto.UsageScopeResponse;
import com.insightflow.usage.dto.UsageSummary;
import com.insightflow.usage.repository.UsageRecordRepository;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.function.ToIntFunction;
import org.springframework.stereotype.Service;

@Service
public class UsageQueryService {

    private static final String DEFAULT_UNIT = "day";

    private final UsageRecordRepository usageRecordRepository;

    public UsageQueryService(UsageRecordRepository usageRecordRepository) {
        this.usageRecordRepository = usageRecordRepository;
    }

    public UsageScopeResponse getUserUsage(String userId) {
        return getUserUsage(userId, null, null, DEFAULT_UNIT);
    }

    public UsageScopeResponse getUserUsage(String userId, LocalDate from, LocalDate to, String unit) {
        return getUsage(new UsageQuery(UsageScopeType.USER, userId, from, to, normalizeUnit(unit)));
    }

    public UsageScopeResponse getTeamUsage(String teamId) {
        return getTeamUsage(teamId, null, null, DEFAULT_UNIT);
    }

    public UsageScopeResponse getTeamUsage(String teamId, LocalDate from, LocalDate to, String unit) {
        return getUsage(new UsageQuery(UsageScopeType.TEAM, teamId, from, to, normalizeUnit(unit)));
    }

    public UsageScopeResponse getServiceUsage(String serviceId) {
        return getServiceUsage(serviceId, null, null, DEFAULT_UNIT);
    }

    public UsageScopeResponse getServiceUsage(String serviceId, LocalDate from, LocalDate to, String unit) {
        return getUsage(new UsageQuery(UsageScopeType.SERVICE, serviceId, from, to, normalizeUnit(unit)));
    }

    private UsageScopeResponse getUsage(UsageQuery query) {
        List<UsageRecord> allRecords = usageRecordRepository.findAll();
        LocalDate defaultFrom = allRecords.stream()
                .map(UsageRecord::requestedOn)
                .min(LocalDate::compareTo)
                .orElse(LocalDate.now());
        LocalDate defaultTo = allRecords.stream()
                .map(UsageRecord::requestedOn)
                .max(LocalDate::compareTo)
                .orElse(defaultFrom);
        LocalDate resolvedFrom = query.from() != null ? query.from() : defaultFrom;
        LocalDate resolvedTo = query.to() != null ? query.to() : defaultTo;
        if (resolvedTo.isBefore(resolvedFrom)) {
            LocalDate swapped = resolvedFrom;
            resolvedFrom = resolvedTo;
            resolvedTo = swapped;
        }
        LocalDate finalResolvedFrom = resolvedFrom;
        LocalDate finalResolvedTo = resolvedTo;

        List<UsageRecord> filteredRecords = allRecords.stream()
                .filter(record -> query.scopeType().matches(record, query.scopeId()))
                .filter(record -> !record.requestedOn().isBefore(finalResolvedFrom))
                .filter(record -> !record.requestedOn().isAfter(finalResolvedTo))
                .sorted(Comparator.comparing(UsageRecord::requestedAt).reversed())
                .toList();

        return new UsageScopeResponse(
                query.scopeType().apiValue(),
                query.scopeId(),
                new UsagePeriod(finalResolvedFrom.toString(), finalResolvedTo.toString(), query.unit()),
                buildSummary(filteredRecords),
                filteredRecords.stream()
                        .map(this::toUsageItem)
                        .toList()
        );
    }

    private UsageSummary buildSummary(List<UsageRecord> filteredRecords) {
        return new UsageSummary(
                filteredRecords.size(),
                filteredRecords.stream().mapToInt(UsageRecord::totalTokens).sum(),
                average(filteredRecords, UsageRecord::totalTokens),
                average(filteredRecords, UsageRecord::latencyMs),
                countByStatus(filteredRecords, "SUCCEEDED"),
                countByStatus(filteredRecords, "FAILED"),
                countByStatus(filteredRecords, "BLOCKED")
        );
    }

    private UsageItem toUsageItem(UsageRecord record) {
        return new UsageItem(
                record.requestId(),
                record.serviceId(),
                record.workflowId(),
                record.model(),
                record.status(),
                record.policyResult(),
                record.limitResult(),
                record.promptTokens(),
                record.completionTokens(),
                record.totalTokens(),
                record.latencyMs(),
                record.requestedAt().toString()
        );
    }

    private int average(List<UsageRecord> filteredRecords, ToIntFunction<UsageRecord> valueExtractor) {
        if (filteredRecords.isEmpty()) {
            return 0;
        }
        return (int) Math.round(filteredRecords.stream()
                .mapToInt(valueExtractor)
                .average()
                .orElse(0));
    }

    private int countByStatus(List<UsageRecord> filteredRecords, String status) {
        return (int) filteredRecords.stream()
                .filter(record -> status.equals(record.status()))
                .count();
    }

    private String normalizeUnit(String unit) {
        if (unit == null || unit.isBlank()) {
            return DEFAULT_UNIT;
        }
        return unit;
    }
}
