package com.sajana.docqa;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AskResponse {
    private String answer;
    private long latencyMs;
    private int inputTokens;
    private int outputTokens;
}
