package com.dobrosav.matches.db.repos;

import com.dobrosav.matches.db.entities.User;
import com.dobrosav.matches.db.entities.UserLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserLikeRepo extends JpaRepository<UserLike, Integer> {
    Optional<UserLike> findByLikerAndLiked(User liker, User liked);
    List<UserLike> findByLiker(User liker);
    long countByLikerAndCreatedAtAfter(User liker, java.util.Date date);
    List<UserLike> findByLiked(User liked);
}
