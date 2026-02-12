package com.dobrosav.matches.db.repos;

import com.dobrosav.matches.db.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<User,Integer> {
    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    List<User> findByIdNotIn(Collection<Integer> ids);

    @Query("SELECT u FROM User u WHERE u.id NOT IN :excludedIds AND u.sex = :gender AND u.dateOfBirth BETWEEN :startDate AND :endDate")
    Page<User> findFilteredFeed(
            @Param("excludedIds") Collection<Integer> excludedIds,
            @Param("gender") String gender,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate,
            Pageable pageable
    );
}

