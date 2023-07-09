package com.dobrosav.matches.db.repos;

import com.dobrosav.matches.db.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepo extends JpaRepository<User, Integer> {
}
