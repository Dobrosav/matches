package com.dobrosav.matches.db.repos;

import com.dobrosav.matches.db.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepo extends JpaRepository<User, Integer> {
    @Query("SELECT u FROM User u WHERE u.username=?1")
    List<User> findByUsername(String username);

    @Query("SELECT u FROM User u WHERE u.mail=?1")
    List<User> findByMail(String mail);

    @Query("SELECT u FROM User u WHERE u.mail=?1 AND u.password=?2")
    User findByMailAndPassword(String mail, String password);
}
