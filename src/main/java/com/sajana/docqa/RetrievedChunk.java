package com.sajana.docqa;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RetrievedChunk {
    private String documentId;
    private Integer chunkIndex;
    private String content;
}