package com.dobrosav.matches.db.repos;

import com.dobrosav.matches.db.entities.UserPreferences;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserPreferencesRepo extends JpaRepository<UserPreferences, Integer> {
}
