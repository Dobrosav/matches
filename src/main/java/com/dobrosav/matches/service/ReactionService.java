package com.dobrosav.matches.service;

import com.dobrosav.matches.db.entities.User;
import com.dobrosav.matches.db.entities.UserDislike;
import com.dobrosav.matches.db.entities.UserLike;
import com.dobrosav.matches.db.entities.UserMatch;
import com.dobrosav.matches.db.repos.UserDislikeRepo;
import com.dobrosav.matches.db.repos.UserLikeRepo;
import com.dobrosav.matches.db.repos.UserMatchRepo;
import com.dobrosav.matches.db.repos.UserRepo;
import com.dobrosav.matches.exception.ErrorType;
import com.dobrosav.matches.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ReactionService {

    private final UserRepo userRepo;
    private final UserLikeRepo userLikeRepo;
    private final UserDislikeRepo userDislikeRepo;
    private final UserMatchRepo userMatchRepo;

    @Autowired
    public ReactionService(UserRepo userRepo, UserLikeRepo userLikeRepo, UserDislikeRepo userDislikeRepo, UserMatchRepo userMatchRepo) {
        this.userRepo = userRepo;
        this.userLikeRepo = userLikeRepo;
        this.userDislikeRepo = userDislikeRepo;
        this.userMatchRepo = userMatchRepo;
    }

    public void processReaction(String fromUserEmail, String toUserEmail, String reaction) {
        User fromUser = userRepo.findByEmail(fromUserEmail).orElseThrow(() -> new ServiceException(ErrorType.USER_NOT_FOUND, HttpStatus.NOT_FOUND));
        User toUser = userRepo.findByEmail(toUserEmail).orElseThrow(() -> new ServiceException(ErrorType.USER_NOT_FOUND, HttpStatus.NOT_FOUND));

        if ("like".equalsIgnoreCase(reaction)) {
            handleLike(fromUser, toUser);
        } else if ("dislike".equalsIgnoreCase(reaction)) {
            handleDislike(fromUser, toUser);
        } else {
            throw new ServiceException(ErrorType.INVALID_REACTION, HttpStatus.BAD_REQUEST);
        }
    }

    private void handleLike(User fromUser, User toUser) {
        UserLike userLike = new UserLike(fromUser, toUser);
        userLikeRepo.save(userLike);

        Optional<UserLike> doesOtherUserLike = userLikeRepo.findByLikerAndLiked(toUser, fromUser);
        if (doesOtherUserLike.isPresent()) {
            UserMatch match1 = new UserMatch(fromUser, toUser);
            UserMatch match2 = new UserMatch(toUser, fromUser);
            userMatchRepo.save(match1);
            userMatchRepo.save(match2);
        }
    }

    private void handleDislike(User fromUser, User toUser) {
        UserDislike userDislike = new UserDislike(fromUser, toUser);
        userDislikeRepo.save(userDislike);
    }
}
