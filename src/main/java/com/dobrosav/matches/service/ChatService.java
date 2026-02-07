package com.dobrosav.matches.service;

import com.dobrosav.matches.db.entities.ChatMessage;
import com.dobrosav.matches.db.entities.User;
import com.dobrosav.matches.db.entities.UserMatch;
import com.dobrosav.matches.db.repos.ChatMessageRepo;
import com.dobrosav.matches.db.repos.UserMatchRepo;
import com.dobrosav.matches.db.repos.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class ChatService {

    private final ChatMessageRepo chatMessageRepo;
    private final UserMatchRepo userMatchRepo;
    private final UserRepo userRepo;

    @Autowired
    public ChatService(ChatMessageRepo chatMessageRepo, UserMatchRepo userMatchRepo, UserRepo userRepo) {
        this.chatMessageRepo = chatMessageRepo;
        this.userMatchRepo = userMatchRepo;
        this.userRepo = userRepo;
    }

    @Transactional
    public ChatMessage saveMessage(Integer matchId, Integer senderId, String content) {
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

        return chatMessageRepo.save(chatMessage);
    }

    @Transactional(readOnly = true)
    public List<ChatMessage> getChatHistory(Integer matchId) {
        UserMatch match = userMatchRepo.findById(matchId).orElseThrow(() -> new RuntimeException("Match not found"));
        return chatMessageRepo.findByMatchOrderByTimestampAsc(match);
    }
}
