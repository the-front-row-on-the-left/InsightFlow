package com.insightflow.gateway;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insightflow.common.error.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Component
class OpenAiDocumentSearchSummarizer {

    private static final Logger log = LoggerFactory.getLogger(OpenAiDocumentSearchSummarizer.class);

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final boolean enabled;
    private final String apiKey;
    private final String baseUrl;
    private final String model;
    private final Duration timeout;

    OpenAiDocumentSearchSummarizer(ObjectMapper objectMapper,
                                   @Value("${insightflow.ai.openai.enabled:true}") boolean enabled,
                                   @Value("${insightflow.ai.openai.api-key:}") String apiKey,
                                   @Value("${insightflow.ai.openai.base-url:https://api.openai.com/v1}") String baseUrl,
                                   @Value("${insightflow.ai.openai.model:gpt-5-mini}") String model,
                                   @Value("${insightflow.ai.openai.timeout-seconds:45}") long timeoutSeconds) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(Math.max(5, timeoutSeconds)))
                .build();
        this.enabled = enabled;
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.baseUrl = (baseUrl == null || baseUrl.isBlank()) ? "https://api.openai.com/v1" : baseUrl.replaceAll("/+$", "");
        this.model = (model == null || model.isBlank()) ? "gpt-5-mini" : model.trim();
        this.timeout = Duration.ofSeconds(Math.max(5, timeoutSeconds));
    }

    Optional<String> summarize(String query, String normalizedScope, List<ScoredDocument> rankedDocuments) {
        if (!enabled || apiKey.isBlank()) {
            return Optional.empty();
        }

        try {
            String requestBody = objectMapper.writeValueAsString(new ResponsesRequest(
                    model,
                    """
                    You are generating grounded Korean summaries for an enterprise document-search product.
                    Use only the retrieved document evidence provided by the user.
                    Write 2-3 concise sentences in Korean.
                    Do not invent facts, doc IDs, teams, or policies that are not present in the evidence.
                    Mention uncertainty briefly if the evidence is partial.
                    """,
                    buildPrompt(query, normalizedScope, rankedDocuments),
                    240,
                    new ResponseTextFormat(new ResponseFormat("text"))
            ));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/responses"))
                    .timeout(timeout)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                log.warn("OpenAI summary request failed with status {} and body {}", response.statusCode(), abbreviate(response.body()));
                throw providerException(response.statusCode());
            }

            String summary = extractOutputText(objectMapper.readTree(response.body()));
            if (summary == null || summary.isBlank()) {
                log.warn("OpenAI summary response completed without extractable text. Falling back to local summary. body={}",
                        abbreviate(response.body()));
                return Optional.empty();
            }
            return Optional.of(summary.trim());
        } catch (GatewayApiException exception) {
            throw exception;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new GatewayApiException(
                    ErrorCode.AI_PROVIDER_ERROR,
                    HttpStatus.BAD_GATEWAY,
                    "문서 검색 요약 생성 중 AI provider 호출이 중단되었습니다."
            );
        } catch (IOException | RuntimeException exception) {
            throw new GatewayApiException(
                    ErrorCode.AI_PROVIDER_ERROR,
                    HttpStatus.BAD_GATEWAY,
                    "문서 검색 요약 생성 중 AI provider 오류가 발생했습니다."
            );
        }
    }

    private String buildPrompt(String query, String normalizedScope, List<ScoredDocument> rankedDocuments) {
        StringBuilder builder = new StringBuilder();
        builder.append("질의: ").append(query).append('\n');
        builder.append("검색 범위: ").append(normalizedScope).append("\n\n");
        builder.append("검색된 문서 근거:\n");

        int order = 1;
        for (ScoredDocument rankedDocument : rankedDocuments) {
            KnowledgeDocument document = rankedDocument.document();
            builder.append(order)
                    .append(". 제목: ").append(document.title()).append('\n')
                    .append("   문서 ID: ").append(document.docId()).append('\n')
                    .append("   섹션: ").append(document.section()).append('\n')
                    .append("   관련도: ").append(Math.round(rankedDocument.score() * 100.0) / 100.0).append('\n')
                    .append("   본문: ").append(document.content()).append("\n\n");
            order += 1;
        }

        return builder.toString();
    }

    private String extractOutputText(JsonNode responseBody) {
        JsonNode outputText = responseBody.path("output_text");
        if (outputText.isTextual() && !outputText.asText().isBlank()) {
            return outputText.asText();
        }

        for (JsonNode item : responseBody.path("output")) {
            JsonNode content = item.path("content");
            if (!content.isArray()) {
                continue;
            }
            for (JsonNode entry : content) {
                if ("output_text".equals(entry.path("type").asText())) {
                    JsonNode typedTextNode = entry.path("text");
                    if (typedTextNode.isTextual() && !typedTextNode.asText().isBlank()) {
                        return typedTextNode.asText();
                    }
                    JsonNode typedNestedTextNode = typedTextNode.path("value");
                    if (typedNestedTextNode.isTextual() && !typedNestedTextNode.asText().isBlank()) {
                        return typedNestedTextNode.asText();
                    }
                }
                JsonNode textNode = entry.path("text");
                if (textNode.isTextual() && !textNode.asText().isBlank()) {
                    return textNode.asText();
                }
                JsonNode nestedTextNode = entry.path("text").path("value");
                if (nestedTextNode.isTextual() && !nestedTextNode.asText().isBlank()) {
                    return nestedTextNode.asText();
                }
            }
        }

        return null;
    }

    private GatewayApiException providerException(int statusCode) {
        if (statusCode == 401 || statusCode == 403) {
            return new GatewayApiException(
                    ErrorCode.AI_PROVIDER_ERROR,
                    HttpStatus.BAD_GATEWAY,
                    "OpenAI API 인증에 실패했습니다. OPENAI_API_KEY를 확인하세요."
            );
        }
        if (statusCode == 429) {
            return new GatewayApiException(
                    ErrorCode.AI_PROVIDER_ERROR,
                    HttpStatus.BAD_GATEWAY,
                    "OpenAI API 요청 제한에 도달했습니다. 잠시 후 다시 시도하세요."
            );
        }
        return new GatewayApiException(
                ErrorCode.AI_PROVIDER_ERROR,
                HttpStatus.BAD_GATEWAY,
                "문서 검색 요약 생성 중 AI provider 오류가 발생했습니다."
        );
    }

    private String abbreviate(String value) {
        if (value == null || value.isBlank()) {
            return "<empty>";
        }
        return value.length() <= 500 ? value : value.substring(0, 500);
    }
}

record ResponsesRequest(
        String model,
        String instructions,
        String input,
        int max_output_tokens,
        ResponseTextFormat text
) {
}

record ResponseTextFormat(
        ResponseFormat format
) {
}

record ResponseFormat(
        String type
) {
}
