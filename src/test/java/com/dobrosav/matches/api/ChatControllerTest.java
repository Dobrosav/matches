package com.dobrosav.matches.api;

import com.dobrosav.matches.api.model.request.ChatMessageRequest;
import com.dobrosav.matches.api.model.response.ChatMessageResponse;
import com.dobrosav.matches.service.ChatService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatControllerTest {

    @Mock
    private ChatService chatService;

    @InjectMocks
    private ChatController chatController;

    @Test
    void testSendMessage() {
        // Since ChatController is just a simple @MessageMapping delegating to ChatService,
        // we can unit test it directly without loading the full WebSocket context or MockMvc,
        // which makes it much faster and less prone to configuration issues.

        Integer matchId = 1;
        ChatMessageRequest request = new ChatMessageRequest();
        request.setSenderId(10);
        request.setContent("Hello there");

        ChatMessageResponse response = new ChatMessageResponse(100, 1, 10, "testuser", "Hello there", new Date());

        when(chatService.saveMessage(matchId, 10, "Hello there")).thenReturn(response);

        ChatMessageResponse result = chatController.sendMessage(matchId, request);

        assertEquals(100, result.getId());
        assertEquals("Hello there", result.getContent());
        assertEquals("testuser", result.getSenderUsername());
    }
}
