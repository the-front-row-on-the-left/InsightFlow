package com.insightflow.aiopscore.repository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.insightflow.aiopscore.domain.ExecutionRecord;
import org.springframework.stereotype.Component;

@Component
public class AiOpsCoreExecutionRepository {

    private final ConcurrentHashMap<String, ExecutionRecord> records = new ConcurrentHashMap<>();

    public void save(ExecutionRecord record) {
        records.put(record.executionId(), record);
    }

    public Optional<ExecutionRecord> findById(String executionId) {
        return Optional.ofNullable(records.get(executionId));
    }

    public List<ExecutionRecord> findAll() {
        List<ExecutionRecord> items = new ArrayList<>(records.values());
        items.sort(Comparator.comparing(ExecutionRecord::createdAt).reversed());
        return items;
    }

    public void clear() {
        records.clear();
    }
}
