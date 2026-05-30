package com.sajana.docqa;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "llm_calls")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LlmCall {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_id", nullable = false)
    private String requestId;

    @Column(name = "operation", nullable = false)
    private String operation;

    @Column(name = "model", nullable = false)
    private String model;

    @Column(name = "question", columnDefinition = "TEXT")
    private String question;

    @Column(name = "prompt", columnDefinition = "TEXT")
    private String prompt;

    @Column(name = "answer", columnDefinition = "TEXT")
    private String answer;

    @Column(name = "input_tokens", nullable = false)
    private Integer inputTokens;

    @Column(name = "output_tokens", nullable = false)
    private Integer outputTokens;

    @Column(name = "latency_ms", nullable = false)
    private Integer latencyMs;

    @Type(JsonType.class)
    @Column(name = "retrieved_chunks", columnDefinition = "jsonb")
    private List<RetrievedChunk> retrievedChunks;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();
}