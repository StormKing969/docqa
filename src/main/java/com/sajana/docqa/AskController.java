package com.sajana.docqa;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.Model;
import com.anthropic.models.messages.TextBlock;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class AskController {
    private final AnthropicClient client = AnthropicOkHttpClient.fromEnv();

    @PostMapping("/ask")
    public ResponseEntity<AskResponse> ask(@Valid @RequestBody AskRequest request) {
        long start = System.currentTimeMillis();

        String prompt = """
                    You are answering a question about a document.
                    Use ONLY the information in the document below. If the answer is not in the document, say so honestly.
                    
                    === START DOCUMENT ===
                    %s
                    === END DOCUMENT ===
                    
                    Question: %s
                """.formatted(request.getDocument(), request.getQuestion());

        MessageCreateParams params = MessageCreateParams.builder().model(Model.CLAUDE_HAIKU_4_5).maxTokens(1024).addUserMessage(prompt).build();

        Message response = client.messages().create(params);

        String answer = response.content().getFirst().text().map(TextBlock::text).orElse("(no response)");

        long latency = System.currentTimeMillis() - start;
        int inputToken = (int) response.usage().inputTokens();
        int outputToken = (int) response.usage().outputTokens();

        log.info("ask: latency = {}ms, in = {} tokens, out = {} tokens", latency, inputToken, outputToken);

        return ResponseEntity.ok(new AskResponse(answer, latency, inputToken, outputToken));
    }
}
