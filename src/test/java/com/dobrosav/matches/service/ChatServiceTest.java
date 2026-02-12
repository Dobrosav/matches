package com.dobrosav.matches.service;

import com.dobrosav.matches.api.model.response.ChatMessageResponse;
import com.dobrosav.matches.db.entities.ChatMessage;
import com.dobrosav.matches.db.entities.User;
import com.dobrosav.matches.db.entities.UserMatch;
import com.dobrosav.matches.db.repos.ChatMessageRepo;
import com.dobrosav.matches.db.repos.UserMatchRepo;
import com.dobrosav.matches.db.repos.UserRepo;
import com.dobrosav.matches.mapper.ChatMessageMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private ChatMessageRepo chatMessageRepo;

    @Mock
    private UserMatchRepo userMatchRepo;

    @Mock
    private UserRepo userRepo;

    @Mock
    private ChatMessageMapper chatMessageMapper;

    @InjectMocks
    private ChatService chatService;

    @Test
    void whenSaveMessage_thenReturnsChatMessageResponse() {
        User sender = new User();
        sender.setId(1);
        User recipient = new User();
        recipient.setId(2);
        UserMatch match = new UserMatch(sender, recipient);

        when(userMatchRepo.findById(1)).thenReturn(Optional.of(match));
        when(userRepo.findById(1)).thenReturn(Optional.of(sender));
        when(chatMessageRepo.save(any(ChatMessage.class))).thenReturn(new ChatMessage());
        when(chatMessageMapper.toDto(any(ChatMessage.class))).thenReturn(new ChatMessageResponse(1, 1, 1, "test", "Hello", new Date()));

        ChatMessageResponse response = chatService.saveMessage(1, 1, "Hello");

        assertNotNull(response);
        verify(chatMessageRepo).save(any(ChatMessage.class));
    }

    @Test
    void whenGetChatHistory_thenReturnListOfMessages() {
        User sender = new User();
        sender.setId(1);
        User recipient = new User();
        recipient.setId(2);
        UserMatch match = new UserMatch(sender, recipient);
        ChatMessage chatMessage = new ChatMessage();

        when(userMatchRepo.findById(1)).thenReturn(Optional.of(match));
        when(chatMessageRepo.findByMatchOrderByTimestampAsc(match)).thenReturn(Collections.singletonList(chatMessage));
        when(chatMessageMapper.toDto(any(ChatMessage.class))).thenReturn(new ChatMessageResponse(1, 1, 1, "test", "Hello", new Date()));

        List<ChatMessageResponse> history = chatService.getChatHistory(1);

        assertEquals(1, history.size());
        verify(chatMessageRepo).findByMatchOrderByTimestampAsc(match);
    }
}
