package com.dobrosav.matches.api;

import com.dobrosav.matches.api.model.request.ChatMessageRequest;
import com.dobrosav.matches.api.model.response.ChatMessageResponse;
import com.dobrosav.matches.db.entities.ChatMessage;
import com.dobrosav.matches.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
@Hidden
public class ChatController {

    private final ChatService chatService;

    @Autowired
    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @MessageMapping("/chat/{matchId}/send")
    @SendTo("/topic/messages/{matchId}")
    public ChatMessageResponse sendMessage(@DestinationVariable Integer matchId, ChatMessageRequest chatMessageRequest) {
        return chatService.saveMessage(matchId, chatMessageRequest.getSenderId(), chatMessageRequest.getContent());
    }
}
