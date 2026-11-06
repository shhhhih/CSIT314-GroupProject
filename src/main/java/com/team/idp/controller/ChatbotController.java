
package com.team.idp.controller;

import com.team.idp.service.ChatbotService;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chatbot")
public class ChatbotController {

    private final ChatbotService chatbotService;

    public ChatbotController(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    public record ChatRequest(
        String message,
        Map<String, String> context
    ) {}

    @PostMapping("/message")
    public ResponseEntity<Map<String, Object>> sendMessage(
        @RequestBody ChatRequest request
    ) {
        if (request == null ||
            request.message() == null ||
            request.message().isBlank()) {

            return ResponseEntity.badRequest().body(
                Map.of(
                    "reply", "Please type a message first. 💚",
                    "context", Map.of(),
                    "projects", List.of(),
                    "quickReplies", List.of()
                )
            );
        }

        if (request.message().length() > 500) {
            return ResponseEntity.badRequest().body(
                Map.of(
                    "reply", "Please keep your message under 500 characters. 💚",
                    "context", Map.of(),
                    "projects", List.of(),
                    "quickReplies", List.of()
                )
            );
        }

        return ResponseEntity.ok(
            chatbotService.reply(
                request.message(),
                request.context()
            )
        );
    }
}



