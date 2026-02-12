package com.dobrosav.matches.db.repos;

import com.dobrosav.matches.db.entities.User;
import com.dobrosav.matches.db.entities.UserImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserImageRepo extends JpaRepository<UserImage, Integer> {
    long countByUser(User user);
    List<UserImage> findByUser(User user);
    Optional<UserImage> findByIdAndUser(Integer id, User user);
    Optional<UserImage> findByUserAndProfileImageTrue(User user);
    void deleteByUser(User user);
}
