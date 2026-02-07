package com.dobrosav.matches.db.repos;

import com.dobrosav.matches.db.entities.ChatMessage;
import com.dobrosav.matches.db.entities.UserMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepo extends JpaRepository<ChatMessage, Integer> {
    List<ChatMessage> findByMatchOrderByTimestampAsc(UserMatch match);
}
