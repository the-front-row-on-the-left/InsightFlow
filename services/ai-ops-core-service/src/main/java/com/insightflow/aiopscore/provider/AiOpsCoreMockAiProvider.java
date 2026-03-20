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
        String text = String.valueOf(input.getOrDefault("text", "No input provided."));
        int promptTokens = Math.max(12, inputSize / 4);
        int completionTokens = Math.max(24, Math.min(256, promptTokens / 2 + 20));
        long latencyMs = 120L + inputSize;

        return new ExecutionResult(
                "mock-ai-provider",
                "Mock response for " + command.serviceId() + ": " + text,
                promptTokens,
                completionTokens,
                promptTokens + completionTokens,
                latencyMs
        );
    }
}
