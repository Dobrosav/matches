package com.dobrosav.matches.service;

import com.dobrosav.matches.api.model.response.ChatMessageResponse;
import com.dobrosav.matches.db.entities.ChatMessage;
import com.dobrosav.matches.db.entities.User;
import com.dobrosav.matches.db.entities.UserMatch;
import com.dobrosav.matches.db.repos.ChatMessageRepo;
import com.dobrosav.matches.db.repos.UserMatchRepo;
import com.dobrosav.matches.db.repos.UserRepo;
import com.dobrosav.matches.mapper.ChatMessageMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatService {

    private final ChatMessageRepo chatMessageRepo;
    private final UserMatchRepo userMatchRepo;
    private final UserRepo userRepo;
    private final ChatMessageMapper chatMessageMapper;

    @Autowired
    public ChatService(ChatMessageRepo chatMessageRepo, UserMatchRepo userMatchRepo, UserRepo userRepo, ChatMessageMapper chatMessageMapper) {
        this.chatMessageRepo = chatMessageRepo;
        this.userMatchRepo = userMatchRepo;
        this.userRepo = userRepo;
        this.chatMessageMapper = chatMessageMapper;
    }

    @Transactional
    public ChatMessageResponse saveMessage(Integer matchId, Integer senderId, String content) {
        UserMatch match = userMatchRepo.findById(matchId).orElseThrow(() -> new RuntimeException("Match not found"));
        User sender = userRepo.findById(senderId).orElseThrow(() -> new RuntimeException("Sender not found"));
        
        User recipient;
        if (match.getUser1().getId().equals(senderId)) {
            recipient = match.getUser2();
        } else {
            recipient = match.getUser1();
        }

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setMatch(match);
        chatMessage.setSender(sender);
        chatMessage.setRecipient(recipient);
        chatMessage.setContent(content);
        chatMessage.setTimestamp(new Date());

        return chatMessageMapper.toDto(chatMessageRepo.save(chatMessage));
    }

    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getChatHistory(Integer matchId) {
        UserMatch match = userMatchRepo.findById(matchId).orElseThrow(() -> new RuntimeException("Match not found"));
        return chatMessageRepo.findByMatchOrderByTimestampAsc(match).stream()
                .map(chatMessageMapper::toDto)
                .collect(Collectors.toList());
    }
}
