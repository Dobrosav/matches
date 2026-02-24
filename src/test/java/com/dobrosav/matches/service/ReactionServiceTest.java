package com.dobrosav.matches.service;

import com.dobrosav.matches.db.entities.User;
import com.dobrosav.matches.db.entities.UserLike;
import com.dobrosav.matches.db.repos.UserDislikeRepo;
import com.dobrosav.matches.db.repos.UserLikeRepo;
import com.dobrosav.matches.db.repos.UserMatchRepo;
import com.dobrosav.matches.db.repos.UserRepo;
import com.dobrosav.matches.exception.ServiceException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReactionServiceTest {

    @Mock
    private UserRepo userRepo;

    @Mock
    private UserLikeRepo userLikeRepo;

    @Mock
    private UserDislikeRepo userDislikeRepo;

    @Mock
    private UserMatchRepo userMatchRepo;

    @InjectMocks
    private ReactionService reactionService;

    @Test
    void testProcessReaction_Like() {
        User fromUser = new User();
        fromUser.setEmail("from@example.com");
        User toUser = new User();
        toUser.setEmail("to@example.com");

        when(userRepo.findByEmail("from@example.com")).thenReturn(Optional.of(fromUser));
        when(userRepo.findByEmail("to@example.com")).thenReturn(Optional.of(toUser));
        when(userLikeRepo.findByLikerAndLiked(fromUser, toUser)).thenReturn(Optional.empty());
        when(userLikeRepo.findByLikerAndLiked(toUser, fromUser)).thenReturn(Optional.empty());

        reactionService.processReaction("from@example.com", "to@example.com", "like");

        verify(userLikeRepo, times(1)).save(any(UserLike.class));
        verify(userMatchRepo, never()).save(any());
    }

    @Test
    void testProcessReaction_Like_Match() {
        User fromUser = new User();
        fromUser.setEmail("from@example.com");
        User toUser = new User();
        toUser.setEmail("to@example.com");

        when(userRepo.findByEmail("from@example.com")).thenReturn(Optional.of(fromUser));
        when(userRepo.findByEmail("to@example.com")).thenReturn(Optional.of(toUser));
        when(userLikeRepo.findByLikerAndLiked(fromUser, toUser)).thenReturn(Optional.empty());
        when(userLikeRepo.findByLikerAndLiked(toUser, fromUser)).thenReturn(Optional.of(new UserLike()));

        reactionService.processReaction("from@example.com", "to@example.com", "like");

        verify(userLikeRepo, times(1)).save(any(UserLike.class));
        verify(userMatchRepo, times(2)).save(any());
    }

    @Test
    void testProcessReaction_Dislike() {
        User fromUser = new User();
        fromUser.setEmail("from@example.com");
        User toUser = new User();
        toUser.setEmail("to@example.com");

        when(userRepo.findByEmail("from@example.com")).thenReturn(Optional.of(fromUser));
        when(userRepo.findByEmail("to@example.com")).thenReturn(Optional.of(toUser));

        reactionService.processReaction("from@example.com", "to@example.com", "dislike");

        verify(userDislikeRepo, times(1)).save(any());
        verify(userLikeRepo, never()).save(any());
        verify(userMatchRepo, never()).save(any());
    }

    @Test
    void testProcessReaction_InvalidReaction() {
        User fromUser = new User();
        fromUser.setEmail("from@example.com");
        User toUser = new User();
        toUser.setEmail("to@example.com");

        when(userRepo.findByEmail("from@example.com")).thenReturn(Optional.of(fromUser));
        when(userRepo.findByEmail("to@example.com")).thenReturn(Optional.of(toUser));

        assertThrows(ServiceException.class, () -> reactionService.processReaction("from@example.com", "to@example.com", "invalid"));
    }

    @Test
    void testProcessReaction_UserNotFound() {
        when(userRepo.findByEmail("from@example.com")).thenReturn(Optional.empty());
        assertThrows(ServiceException.class, () -> reactionService.processReaction("from@example.com", "to@example.com", "like"));
    }
}
