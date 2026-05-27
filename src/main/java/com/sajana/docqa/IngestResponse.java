package com.sajana.docqa;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class IngestResponse {
    private String documentId;
    private int chunksCreated;
    private long latencyMs;
}