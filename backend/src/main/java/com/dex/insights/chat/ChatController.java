package com.dex.insights.chat;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/chat")
@Tag(name = "Chat", description = "Grounded question answering over the dataset")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    @Operation(summary = "Ask a grounded question",
            description = "Retrieves matching dataset records, assembles them into context, and answers "
                    + "from that context only. Every answer returns the records it was derived from.")
    public ChatResponse ask(@Valid @RequestBody ChatRequest request) {
        return chatService.answer(request);
    }
}
