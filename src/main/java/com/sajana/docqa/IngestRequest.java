package com.sajana.docqa;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class IngestRequest {
    @NotBlank
    private String documentId;

    @NotBlank
    private String content;
}