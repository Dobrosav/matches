package com.dobrosav.matches.db.repos;

import com.dobrosav.matches.db.entities.User;
import com.dobrosav.matches.db.entities.UserDislike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserDislikeRepo extends JpaRepository<UserDislike, Integer> {
    List<UserDislike> findByDisliker(User disliker);

    void deleteByDisliker(User disliker);
    void deleteByDisliked(User disliked);
}
