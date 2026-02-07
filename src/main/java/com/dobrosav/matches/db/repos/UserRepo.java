package com.dobrosav.matches.db.repos;

import com.dobrosav.matches.db.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface UserRepo extends JpaRepository<User,Integer> {
    List<User> findByUsername(String username);

    User findByMail(String mail);
    User findByMailAndPassword(String mail, String password);

    List<User> findByIdNotIn(Collection<Integer> ids);
}

