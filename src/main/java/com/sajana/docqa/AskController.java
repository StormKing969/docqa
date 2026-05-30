package com.sajana.docqa;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.Model;
import com.anthropic.models.messages.TextBlock;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@Slf4j
public class AskController {

    private static final int TOP_K = 3;
    private static final String MODEL_NAME = "claude-haiku-4-5";

    private final EmbeddingService embeddingService;
    private final DocumentChunkRepository repository;
    private final LlmCallRepository llmCallRepository;
    private final AnthropicClient client = AnthropicOkHttpClient.fromEnv();

    @PostMapping("/ask")
    public ResponseEntity<AskResponse> ask(@Valid @RequestBody AskRequest request) {
        String requestId = UUID.randomUUID().toString();
        long start = System.currentTimeMillis();
        String prompt = null;
        List<RetrievedChunk> retrievedDtos = List.of();

        try {
            float[] questionVector = embeddingService.embed(request.getQuestion());
            String questionVectorString = VectorUtils.toVectorString(questionVector);

            List<DocumentChunk> retrieved = repository.findNearestNeighbors(questionVectorString, TOP_K);
            log.info("[{}] Retrieved {} chunks for question='{}'",
                    requestId, retrieved.size(), request.getQuestion());

            retrievedDtos = retrieved.stream()
                    .map(c -> new RetrievedChunk(c.getDocumentId(), c.getChunkIndex(), c.getContent()))
                    .toList();

            String context = retrieved.stream()
                    .map(chunk -> "[Chunk %d from %s]\n%s".formatted(
                            chunk.getChunkIndex(), chunk.getDocumentId(), chunk.getContent()))
                    .collect(Collectors.joining("\n\n"));

            prompt = """
                    You are answering a question using ONLY the context provided below.
                    If the answer is not in the context, say so honestly. Do not make up information.

                    === CONTEXT ===
                    %s
                    === END CONTEXT ===

                    Question: %s
                    """.formatted(context, request.getQuestion());

            MessageCreateParams params = MessageCreateParams.builder()
                    .model(Model.CLAUDE_HAIKU_4_5)
                    .maxTokens(1024)
                    .addUserMessage(prompt)
                    .build();

            Message response = client.messages().create(params);

            String answer = response.content().getFirst().text()
                    .map(TextBlock::text)
                    .orElse("(no response)");

            long latency = System.currentTimeMillis() - start;
            int inputTokens = (int) response.usage().inputTokens();
            int outputTokens = (int) response.usage().outputTokens();

            log.info("[{}] ask: latency={}ms, in={}t, out={}t, retrieved={}c",
                    requestId, latency, inputTokens, outputTokens, retrieved.size());

            // Persist the call
            llmCallRepository.save(LlmCall.builder()
                    .requestId(requestId)
                    .operation("ask")
                    .model(MODEL_NAME)
                    .question(request.getQuestion())
                    .prompt(prompt)
                    .answer(answer)
                    .inputTokens(inputTokens)
                    .outputTokens(outputTokens)
                    .latencyMs((int) latency)
                    .retrievedChunks(retrievedDtos)
                    .status("success")
                    .build());

            return ResponseEntity.ok(new AskResponse(answer, latency, inputTokens, outputTokens, retrievedDtos));

        } catch (Exception e) {
            long latency = System.currentTimeMillis() - start;
            log.error("[{}] ask failed: {}", requestId, e.getMessage(), e);

            // Persist the failure
            llmCallRepository.save(LlmCall.builder()
                    .requestId(requestId)
                    .operation("ask")
                    .model(MODEL_NAME)
                    .question(request.getQuestion())
                    .prompt(prompt)
                    .inputTokens(0)
                    .outputTokens(0)
                    .latencyMs((int) latency)
                    .retrievedChunks(retrievedDtos)
                    .status("error")
                    .errorMessage(e.getMessage())
                    .build());

            throw new RuntimeException("Failed to process question", e);
        }
    }
}