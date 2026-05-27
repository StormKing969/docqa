package com.sajana.docqa;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class IngestController {

    private final Chunker chunker;
    private final EmbeddingService embeddingService;
    private final JdbcTemplate jdbcTemplate;

    @PostMapping("/documents")
    public ResponseEntity<IngestResponse> ingest(@Valid @RequestBody IngestRequest request) throws Exception {
        long start = System.currentTimeMillis();
        log.info("Ingesting document id={}, length={} chars",
                request.getDocumentId(), request.getContent().length());

        List<String> chunks = chunker.chunk(request.getContent());
        log.info("Split into {} chunks", chunks.size());

        for (int i = 0; i < chunks.size(); i++) {
            String chunkText = chunks.get(i);
            float[] vector = embeddingService.embed(chunkText);
            String vectorString = VectorUtils.toVectorString(vector);

            jdbcTemplate.update(
                    "INSERT INTO document_chunks " +
                            "(document_id, chunk_index, content, embedding, created_at) " +
                            "VALUES (?, ?, ?, CAST(? AS vector), ?)",
                    request.getDocumentId(),
                    i,
                    chunkText,
                    vectorString,
                    Timestamp.from(Instant.now())
            );
        }

        long latency = System.currentTimeMillis() - start;
        log.info("Ingested {} chunks in {} ms", chunks.size(), latency);

        return ResponseEntity.ok(new IngestResponse(
                request.getDocumentId(),
                chunks.size(),
                latency
        ));
    }
}