package com.dobrosav.matches.mapper;

import com.dobrosav.matches.api.model.response.ChatMessageResponse;
import com.dobrosav.matches.db.entities.ChatMessage;
import org.springframework.stereotype.Component;

@Component
public class ChatMessageMapper {

    public ChatMessageResponse toDto(ChatMessage chatMessage) {
        if (chatMessage == null) {
            return null;
        }
        return new ChatMessageResponse(
                chatMessage.getId(),
                chatMessage.getMatch().getId(),
                chatMessage.getSender().getId(),
                chatMessage.getSender().getUsername(),
                chatMessage.getContent(),
                chatMessage.getTimestamp()
        );
    }
}
