package com.dobrosav.matches.mapper;

import com.dobrosav.matches.api.model.response.ChatMessageResponse;
import com.dobrosav.matches.db.entities.ChatMessage;
import com.dobrosav.matches.db.entities.User;
import com.dobrosav.matches.db.entities.UserMatch;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ChatMessageMapperTest {

    private final ChatMessageMapper mapper = new ChatMessageMapper();

    @Test
    void testToDto_NullMessage() {
        assertNull(mapper.toDto(null));
    }

    @Test
    void testToDto_ValidMessage() {
        User sender = new User();
        sender.setId(5);
        sender.setUsername("senderUser");

        UserMatch match = new UserMatch();
        match.setId(10);

        ChatMessage message = new ChatMessage();
        message.setId(100);
        message.setSender(sender);
        message.setMatch(match);
        message.setContent("Hello!");
        Date now = new Date();
        message.setTimestamp(now);

        ChatMessageResponse response = mapper.toDto(message);

        assertEquals(100, response.getId());
        assertEquals(10, response.getMatchId());
        assertEquals(5, response.getSenderId());
        assertEquals("senderUser", response.getSenderUsername());
        assertEquals("Hello!", response.getContent());
        assertEquals(now, response.getTimestamp());
    }
}
