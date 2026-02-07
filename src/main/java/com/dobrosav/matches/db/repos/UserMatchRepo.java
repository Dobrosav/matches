package com.dobrosav.matches.db.repos;

import com.dobrosav.matches.db.entities.User;
import com.dobrosav.matches.db.entities.UserMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserMatchRepo extends JpaRepository<UserMatch, Integer> {
    
    @Query("SELECT m FROM UserMatch m WHERE m.user1 = ?1 OR m.user2 = ?1")
    List<UserMatch> findAllMatchesForUser(User user);
}
