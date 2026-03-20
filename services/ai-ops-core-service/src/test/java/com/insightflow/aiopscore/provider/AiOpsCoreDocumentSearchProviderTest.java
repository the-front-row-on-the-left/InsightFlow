package com.insightflow.aiopscore.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AiOpsCoreDocumentSearchProviderTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void extractsActualUsageFromOpenAiResponsesPayload() throws Exception {
        JsonNode responseBody = objectMapper.readTree("""
                {
                  "output_text": "요약 응답",
                  "usage": {
                    "input_tokens": 321,
                    "output_tokens": 123,
                    "total_tokens": 444
                  }
                }
                """);

        AiOpsCoreDocumentSearchProvider.OpenAiSummaryResult result =
                AiOpsCoreDocumentSearchProvider.parseOpenAiSummaryResult(responseBody);

        assertThat(result.summary()).isEqualTo("요약 응답");
        assertThat(result.promptTokens()).isEqualTo(321);
        assertThat(result.completionTokens()).isEqualTo(123);
        assertThat(result.totalTokens()).isEqualTo(444);
    }

    @Test
    void derivesTotalTokensWhenOnlyPartialUsageFieldsExist() throws Exception {
        JsonNode responseBody = objectMapper.readTree("""
                {
                  "output": [
                    {
                      "content": [
                        {
                          "text": "부분 응답"
                        }
                      ]
                    }
                  ],
                  "usage": {
                    "input_tokens": 210,
                    "output_tokens": 90
                  }
                }
                """);

        AiOpsCoreDocumentSearchProvider.OpenAiSummaryResult result =
                AiOpsCoreDocumentSearchProvider.parseOpenAiSummaryResult(responseBody);

        assertThat(result.summary()).isEqualTo("부분 응답");
        assertThat(result.promptTokens()).isEqualTo(210);
        assertThat(result.completionTokens()).isEqualTo(90);
        assertThat(result.totalTokens()).isEqualTo(300);
    }
}
