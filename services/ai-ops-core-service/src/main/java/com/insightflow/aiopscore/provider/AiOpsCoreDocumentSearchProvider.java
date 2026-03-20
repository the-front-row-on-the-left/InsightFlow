package com.insightflow.aiopscore.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insightflow.aiopscore.domain.ExecutionCommand;
import com.insightflow.aiopscore.domain.ExecutionResult;
import com.insightflow.common.error.BusinessException;
import com.insightflow.common.error.ErrorCode;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

@Component
public class AiOpsCoreDocumentSearchProvider {

    private static final Pattern TOKEN_SPLITTER = Pattern.compile("[^a-zA-Z0-9가-힣]+");

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final boolean openAiEnabled;
    private final String apiKey;
    private final String baseUrl;
    private final String model;
    private final Duration timeout;

    private final List<KnowledgeDocument> corpus = List.of(
            new KnowledgeDocument("doc_ops_001", "AI 운영 비용 정책", "2.1", List.of("ops", "billing", "policy"),
                    "비용 계산은 토큰 사용량, 서비스 호출 횟수, 팀별 정책 가중치를 기준으로 집계한다. 문서 검색 실행은 citation을 포함해야 하며 운영팀 검토 후 월간 집계에 반영한다."),
            new KnowledgeDocument("doc_bill_018", "월간 Billing Runbook", "4.3", List.of("billing", "finance"),
                    "청구 집계는 일 단위 잠정 계산 후 월말 확정 상태로 전환된다. 요청별 request_id와 execution_id를 연결해 비용 산정 근거를 남겨야 한다."),
            new KnowledgeDocument("doc_policy_004", "검색 응답 품질 가이드", "3.2", List.of("policy", "quality", "search"),
                    "문서 검색 결과는 answer_summary, top_chunks, citations를 함께 제공해야 한다. 검색 범위(document_scope)가 주어지면 우선 필터링 후 관련도 순으로 정렬한다."),
            new KnowledgeDocument("doc_security_013", "보안 운영 핸드북", "5.1", List.of("security", "ops"),
                    "운영팀은 정책 변경 시 승인 기준과 예외 절차를 문서화해야 한다. 문서 검색은 scope가 security일 때 보안 핸드북과 관련 회의록을 우선 탐색한다."),
            new KnowledgeDocument("doc_release_022", "릴리스 운영 체크리스트", "1.4", List.of("release", "ops"),
                    "릴리스 전후 운영 체크리스트에는 정책 변경, 비용 영향, 장애 대응 포인트가 포함된다. 문서 검색 결과는 다음 workflow step에서 재사용 가능한 context로 전달할 수 있다.")
    );

    public AiOpsCoreDocumentSearchProvider(
            ObjectMapper objectMapper,
            @Value("${insightflow.ai.openai.enabled:true}") boolean openAiEnabled,
            @Value("${insightflow.ai.openai.api-key:}") String apiKey,
            @Value("${insightflow.ai.openai.base-url:https://api.openai.com/v1}") String baseUrl,
            @Value("${insightflow.ai.openai.model:gpt-4.1-mini}") String model,
            @Value("${insightflow.ai.openai.timeout-seconds:45}") long timeoutSeconds
    ) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(Math.max(5, timeoutSeconds)))
                .build();
        this.openAiEnabled = openAiEnabled;
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.baseUrl = (baseUrl == null || baseUrl.isBlank()) ? "https://api.openai.com/v1" : baseUrl.replaceAll("/+$", "");
        this.model = (model == null || model.isBlank()) ? "gpt-4.1-mini" : model.trim();
        this.timeout = Duration.ofSeconds(Math.max(5, timeoutSeconds));
    }

    public ExecutionResult execute(ExecutionCommand command) {
        long startedAt = System.nanoTime();
        String query = String.valueOf(command.input().getOrDefault("query", "")).trim();
        if (query.isBlank()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ErrorCode.INVALID_REQUEST, "문서 검색에는 query 입력이 필요합니다.");
        }

        String rawScope = String.valueOf(command.input().getOrDefault("document_scope", "all"));
        Set<String> scopes = normalizeScopes(rawScope);
        List<String> queryTokens = tokenize(query);

        List<ScoredDocument> rankedDocuments = corpus.stream()
                .filter(document -> scopes.isEmpty() || document.matchesAnyScope(scopes))
                .map(document -> new ScoredDocument(document, score(document, queryTokens)))
                .sorted(Comparator.comparingDouble(ScoredDocument::score).reversed())
                .limit(3)
                .toList();

        if (rankedDocuments.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ErrorCode.INVALID_REQUEST, "검색 범위에 해당하는 문서를 찾지 못했습니다.");
        }

        List<Map<String, Object>> topChunks = rankedDocuments.stream()
                .map(scored -> Map.<String, Object>of(
                        "doc_id", scored.document().docId(),
                        "snippet", scored.document().snippetFor(queryTokens),
                        "score", round(scored.score())
                ))
                .toList();
        List<Map<String, Object>> citations = rankedDocuments.stream()
                .map(scored -> Map.<String, Object>of(
                        "doc_id", scored.document().docId(),
                        "title", scored.document().title(),
                        "section", scored.document().section()
                ))
                .toList();

        String normalizedScope = scopes.isEmpty() ? "all-documents" : String.join(", ", scopes);
        OpenAiSummaryResult openAiSummary = summarize(query, normalizedScope, rankedDocuments).orElse(null);
        String summary = openAiSummary == null
                ? buildSummary(query, normalizedScope, rankedDocuments)
                : openAiSummary.summary();
        long latencyMs = Math.max(120L, (System.nanoTime() - startedAt) / 1_000_000L);
        int promptTokens = openAiSummary == null
                ? Math.max(64, query.length() * 2 + rankedDocuments.stream().mapToInt(doc -> doc.document().content().length()).sum() / 6)
                : openAiSummary.promptTokens();
        int completionTokens = openAiSummary == null
                ? Math.max(48, summary.length() / 2)
                : openAiSummary.completionTokens();
        int totalTokens = openAiSummary == null
                ? promptTokens + completionTokens
                : openAiSummary.totalTokens();

        return new ExecutionResult(
                "document-search-provider",
                Map.of(
                        "type", "doc_search",
                        "answer_summary", summary,
                        "top_chunks", topChunks,
                        "citations", citations,
                        "document_scope", normalizedScope
                ),
                summary,
                promptTokens,
                completionTokens,
                totalTokens,
                latencyMs
        );
    }

    private Optional<OpenAiSummaryResult> summarize(String query, String normalizedScope, List<ScoredDocument> rankedDocuments) {
        if (!openAiEnabled || apiKey.isBlank()) {
            return Optional.empty();
        }
        try {
            String requestBody = objectMapper.writeValueAsString(Map.of(
                    "model", model,
                    "instructions", "You are generating grounded Korean summaries for enterprise document search. Use only the provided evidence. Write 2-3 concise Korean sentences.",
                    "input", buildPrompt(query, normalizedScope, rankedDocuments),
                    "max_output_tokens", 240,
                    "text", Map.of("format", Map.of("type", "text"))
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
                throw providerException(response.statusCode());
            }
            OpenAiSummaryResult result = parseOpenAiSummaryResult(objectMapper.readTree(response.body()));
            return result.summary() == null || result.summary().isBlank() ? Optional.empty() : Optional.of(result);
        } catch (BusinessException exception) {
            throw exception;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new BusinessException(HttpStatus.BAD_GATEWAY, ErrorCode.AI_PROVIDER_ERROR, "문서 검색 요약 생성 중 AI provider 호출이 중단되었습니다.");
        } catch (IOException exception) {
            throw new BusinessException(HttpStatus.BAD_GATEWAY, ErrorCode.AI_PROVIDER_ERROR, "문서 검색 요약 생성 중 AI provider 오류가 발생했습니다.");
        }
    }

    static OpenAiSummaryResult parseOpenAiSummaryResult(JsonNode responseBody) {
        String outputText = extractOutputText(responseBody);
        if (outputText == null || outputText.isBlank()) {
            return new OpenAiSummaryResult("", 0, 0, 0);
        }

        JsonNode usageNode = responseBody.path("usage");
        int promptTokens = usageNode.path("input_tokens").asInt(0);
        int completionTokens = usageNode.path("output_tokens").asInt(0);
        int totalTokens = usageNode.path("total_tokens").asInt(promptTokens + completionTokens);
        if (totalTokens == 0 && (promptTokens > 0 || completionTokens > 0)) {
            totalTokens = promptTokens + completionTokens;
        }
        return new OpenAiSummaryResult(outputText.trim(), promptTokens, completionTokens, totalTokens);
    }

    private String buildPrompt(String query, String normalizedScope, List<ScoredDocument> rankedDocuments) {
        StringBuilder builder = new StringBuilder();
        builder.append("질의: ").append(query).append('\n');
        builder.append("검색 범위: ").append(normalizedScope).append("\n\n");
        builder.append("검색된 문서 근거:\n");
        int order = 1;
        for (ScoredDocument rankedDocument : rankedDocuments) {
            builder.append(order++)
                    .append(". 제목: ").append(rankedDocument.document().title()).append('\n')
                    .append("   문서 ID: ").append(rankedDocument.document().docId()).append('\n')
                    .append("   섹션: ").append(rankedDocument.document().section()).append('\n')
                    .append("   본문: ").append(rankedDocument.document().content()).append("\n\n");
        }
        return builder.toString();
    }

    private static String extractOutputText(JsonNode responseBody) {
        JsonNode outputText = responseBody.path("output_text");
        if (outputText.isTextual() && !outputText.asText().isBlank()) {
            return outputText.asText();
        }
        for (JsonNode item : responseBody.path("output")) {
            for (JsonNode entry : item.path("content")) {
                JsonNode textNode = entry.path("text");
                if (textNode.isTextual() && !textNode.asText().isBlank()) {
                    return textNode.asText();
                }
                JsonNode nestedTextNode = textNode.path("value");
                if (nestedTextNode.isTextual() && !nestedTextNode.asText().isBlank()) {
                    return nestedTextNode.asText();
                }
            }
        }
        return null;
    }

    private BusinessException providerException(int statusCode) {
        if (statusCode == 401 || statusCode == 403) {
            return new BusinessException(HttpStatus.BAD_GATEWAY, ErrorCode.AI_PROVIDER_ERROR, "OpenAI API 인증에 실패했습니다. OPENAI_API_KEY를 확인하세요.");
        }
        if (statusCode == 429) {
            return new BusinessException(HttpStatus.BAD_GATEWAY, ErrorCode.AI_PROVIDER_ERROR, "OpenAI API 요청 제한에 도달했습니다. 잠시 후 다시 시도하세요.");
        }
        return new BusinessException(HttpStatus.BAD_GATEWAY, ErrorCode.AI_PROVIDER_ERROR, "문서 검색 요약 생성 중 AI provider 오류가 발생했습니다.");
    }

    private Set<String> normalizeScopes(String rawScope) {
        if (rawScope == null || rawScope.isBlank() || "all".equalsIgnoreCase(rawScope.trim())) {
            return Set.of();
        }
        Set<String> scopes = new LinkedHashSet<>();
        for (String part : rawScope.split(",")) {
            String normalized = part.trim().toLowerCase(Locale.ROOT);
            if (!normalized.isBlank()) {
                scopes.add(normalized);
            }
        }
        return scopes;
    }

    private List<String> tokenize(String rawText) {
        List<String> tokens = new ArrayList<>();
        for (String part : TOKEN_SPLITTER.split(rawText.toLowerCase(Locale.ROOT))) {
            if (!part.isBlank()) {
                tokens.add(part);
            }
        }
        return tokens;
    }

    private double score(KnowledgeDocument document, List<String> queryTokens) {
        if (queryTokens.isEmpty()) {
            return 0.10;
        }
        double score = 0.0;
        for (String token : queryTokens) {
            if (document.titleLower().contains(token)) {
                score += 0.45;
            }
            if (document.contentLower().contains(token)) {
                score += 0.35;
            }
            if (document.scopes().contains(token)) {
                score += 0.20;
            }
        }
        return Math.min(0.99, score / queryTokens.size());
    }

    private double round(double score) {
        return Math.round(score * 100.0) / 100.0;
    }

    private String buildSummary(String query, String normalizedScope, List<ScoredDocument> rankedDocuments) {
        List<String> titles = rankedDocuments.stream().map(scored -> scored.document().title()).distinct().toList();
        String primaryFinding = rankedDocuments.get(0).document().snippetFor(tokenize(query));
        return "질의 '" + query + "'는 " + normalizedScope + " 범위에서 " + titles.size()
                + "개의 핵심 문서를 기준으로 해석되었습니다. 주요 근거는 " + String.join(", ", titles)
                + "에 나타나며, 핵심 내용은 " + primaryFinding;
    }

    private record KnowledgeDocument(String docId, String title, String section, List<String> scopes, String content) {
        String titleLower() { return title.toLowerCase(Locale.ROOT); }
        String contentLower() { return content.toLowerCase(Locale.ROOT); }
        boolean matchesAnyScope(Set<String> requestedScopes) { return scopes.stream().anyMatch(requestedScopes::contains); }
        String snippetFor(List<String> tokens) {
            for (String token : tokens) {
                int index = contentLower().indexOf(token);
                if (index >= 0) {
                    int start = Math.max(0, index - 18);
                    int end = Math.min(content.length(), index + 92);
                    return content.substring(start, end).trim();
                }
            }
            return content.substring(0, Math.min(110, content.length())).trim();
        }
    }

    private record ScoredDocument(KnowledgeDocument document, double score) {
    }

    record OpenAiSummaryResult(
            String summary,
            int promptTokens,
            int completionTokens,
            int totalTokens
    ) {
    }
}
