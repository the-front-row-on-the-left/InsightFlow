package com.insightflow.aiopscore.provider;

import java.util.Map;

import com.insightflow.aiopscore.domain.ExecutionCommand;
import com.insightflow.aiopscore.domain.ExecutionResult;
import com.insightflow.common.error.BusinessException;
import com.insightflow.common.error.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class AiOpsCoreMockAiProvider {

    public ExecutionResult execute(ExecutionCommand command) {
        if (command.model().toLowerCase().contains("fail")) {
            throw new BusinessException(HttpStatus.BAD_GATEWAY, ErrorCode.AI_PROVIDER_ERROR,
                    "Mock AI provider failed to process the request.");
        }

        int inputSize = command.inputSize();
        Map<String, Object> input = command.input();
        Map<String, Object> payload = buildPayload(command.serviceId(), input);
        String text = String.valueOf(payload.getOrDefault("answer", payload.getOrDefault("report_text", "No output provided.")));
        int promptTokens = Math.max(12, inputSize / 4);
        int completionTokens = Math.max(24, Math.min(256, promptTokens / 2 + 20));
        long latencyMs = 120L + inputSize;

        return new ExecutionResult(
                "mock-ai-provider",
                payload,
                "Mock response for " + command.serviceId() + ": " + text,
                promptTokens,
                completionTokens,
                promptTokens + completionTokens,
                latencyMs
        );
    }

    private Map<String, Object> buildPayload(String serviceId, Map<String, Object> input) {
        if ("svc_chat_assistant".equals(serviceId)) {
            return Map.of(
                    "type", "chat_assistant",
                    "answer", "질문 '" + String.valueOf(input.getOrDefault("question", "요청")) + "'에 대한 mock 응답입니다.",
                    "bullet_points", java.util.List.of("핵심 내용을 먼저 제시합니다.", "후속 액션을 간단히 정리합니다.", "워크플로우 재사용이 가능합니다.")
            );
        }

        return Map.of(
                "type", "report_generator",
                "report_text", String.valueOf(input.getOrDefault("topic", "운영 보고서")) + " 보고서 초안입니다. "
                        + String.valueOf(input.getOrDefault("context", "문서 검색 결과 없음")),
                "sections", java.util.List.of("요약", "핵심 발견", "권고사항"),
                "format", String.valueOf(input.getOrDefault("format", "executive_summary"))
        );
    }
}
