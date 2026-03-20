package com.insightflow.gateway;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

@Component
class DocumentSearchEngine {

    private static final Pattern TOKEN_SPLITTER = Pattern.compile("[^a-zA-Z0-9가-힣]+");
    private final OpenAiDocumentSearchSummarizer openAiDocumentSearchSummarizer;

    private final List<KnowledgeDocument> corpus = List.of(
            new KnowledgeDocument(
                    "doc_ops_001",
                    "AI 운영 비용 정책",
                    "2.1",
                    List.of("ops", "billing", "policy"),
                    "비용 계산은 토큰 사용량, 서비스 호출 횟수, 팀별 정책 가중치를 기준으로 집계한다. "
                            + "문서 검색 실행은 citation을 포함해야 하며 운영팀 검토 후 월간 집계에 반영한다."
            ),
            new KnowledgeDocument(
                    "doc_bill_018",
                    "월간 Billing Runbook",
                    "4.3",
                    List.of("billing", "finance"),
                    "청구 집계는 일 단위 잠정 계산 후 월말 확정 상태로 전환된다. "
                            + "요청별 request_id와 execution_id를 연결해 비용 산정 근거를 남겨야 한다."
            ),
            new KnowledgeDocument(
                    "doc_policy_004",
                    "검색 응답 품질 가이드",
                    "3.2",
                    List.of("policy", "quality", "search"),
                    "문서 검색 결과는 answer_summary, top_chunks, citations를 함께 제공해야 한다. "
                            + "검색 범위(document_scope)가 주어지면 우선 필터링 후 관련도 순으로 정렬한다."
            ),
            new KnowledgeDocument(
                    "doc_security_013",
                    "보안 운영 핸드북",
                    "5.1",
                    List.of("security", "ops"),
                    "운영팀은 정책 변경 시 승인 기준과 예외 절차를 문서화해야 한다. "
                            + "문서 검색은 scope가 security일 때 보안 핸드북과 관련 회의록을 우선 탐색한다."
            ),
            new KnowledgeDocument(
                    "doc_release_022",
                    "릴리스 운영 체크리스트",
                    "1.4",
                    List.of("release", "ops"),
                    "릴리스 전후 운영 체크리스트에는 정책 변경, 비용 영향, 장애 대응 포인트가 포함된다. "
                            + "문서 검색 결과는 다음 workflow step에서 재사용 가능한 context로 전달할 수 있다."
            )
    );

    DocumentSearchEngine(OpenAiDocumentSearchSummarizer openAiDocumentSearchSummarizer) {
        this.openAiDocumentSearchSummarizer = openAiDocumentSearchSummarizer;
    }

    DocSearchPayload search(String rawQuery, String rawScope) {
        String query = requireQuery(rawQuery);
        Set<String> scopes = normalizeScopes(rawScope);
        List<String> queryTokens = tokenize(query);

        List<ScoredDocument> rankedDocuments = corpus.stream()
                .filter(document -> scopes.isEmpty() || document.matchesAnyScope(scopes))
                .map(document -> new ScoredDocument(document, score(document, queryTokens)))
                .sorted(Comparator.comparingDouble(ScoredDocument::score).reversed())
                .limit(3)
                .toList();

        if (rankedDocuments.isEmpty()) {
            throw new GatewayApiException(
                    com.insightflow.common.error.ErrorCode.INVALID_REQUEST,
                    org.springframework.http.HttpStatus.BAD_REQUEST,
                    "검색 범위에 해당하는 문서를 찾지 못했습니다."
            );
        }

        List<TopChunkPayload> topChunks = rankedDocuments.stream()
                .map(scored -> new TopChunkPayload(
                        scored.document().docId(),
                        scored.document().snippetFor(queryTokens),
                        round(scored.score())
                ))
                .toList();

        List<CitationPayload> citations = rankedDocuments.stream()
                .map(scored -> new CitationPayload(
                        scored.document().docId(),
                        scored.document().title(),
                        scored.document().section()
                ))
                .toList();

        String normalizedScope = scopes.isEmpty() ? "all-documents" : String.join(", ", scopes);
        String summary = openAiDocumentSearchSummarizer
                .summarize(query, normalizedScope, rankedDocuments)
                .orElseGet(() -> buildSummary(query, rankedDocuments, normalizedScope));

        return new DocSearchPayload("doc_search", summary, topChunks, citations, normalizedScope);
    }

    private String requireQuery(String rawQuery) {
        if (rawQuery == null || rawQuery.isBlank()) {
            throw new GatewayApiException(
                    com.insightflow.common.error.ErrorCode.INVALID_REQUEST,
                    org.springframework.http.HttpStatus.BAD_REQUEST,
                    "문서 검색에는 query 입력이 필요합니다."
            );
        }
        return rawQuery.trim();
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

    private List<String> tokenize(String rawText) {
        List<String> tokens = new ArrayList<>();
        for (String part : TOKEN_SPLITTER.split(rawText.toLowerCase(Locale.ROOT))) {
            if (!part.isBlank()) {
                tokens.add(part);
            }
        }
        return tokens;
    }

    private String buildSummary(String query, List<ScoredDocument> rankedDocuments, String normalizedScope) {
        List<String> titles = rankedDocuments.stream()
                .map(scored -> scored.document().title())
                .distinct()
                .toList();
        String primaryFinding = rankedDocuments.get(0).document().snippetFor(tokenize(query));

        return "질의 '" + query + "'는 " + normalizedScope + " 범위에서 " + titles.size()
                + "개의 핵심 문서를 기준으로 해석되었습니다. 주요 근거는 " + String.join(", ", titles)
                + "에 나타나며, 핵심 내용은 " + primaryFinding;
    }

    private double round(double score) {
        return Math.round(score * 100.0) / 100.0;
    }
}

record DocSearchPayload(
        String type,
        String answer_summary,
        List<TopChunkPayload> top_chunks,
        List<CitationPayload> citations,
        String document_scope
) {
}

record TopChunkPayload(
        String doc_id,
        String snippet,
        double score
) {
}

record CitationPayload(
        String doc_id,
        String title,
        String section
) {
}

record KnowledgeDocument(
        String docId,
        String title,
        String section,
        List<String> scopes,
        String content
) {
    String titleLower() {
        return title.toLowerCase(Locale.ROOT);
    }

    String contentLower() {
        return content.toLowerCase(Locale.ROOT);
    }

    boolean matchesAnyScope(Set<String> requestedScopes) {
        return scopes.stream().anyMatch(requestedScopes::contains);
    }

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

record ScoredDocument(
        KnowledgeDocument document,
        double score
) {
}
