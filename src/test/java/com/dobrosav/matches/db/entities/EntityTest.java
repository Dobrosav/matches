package com.dobrosav.matches.db.entities;

import org.junit.jupiter.api.Test;
import java.util.Date;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class EntityTest {

    @Test
    void testUserEntity() {
        User user = new User();
        user.setId(1);
        user.setEmail("test@test.com");
        user.setPassword("pass");
        user.setName("John");
        user.setSurname("Doe");
        user.setDateOfBirth(new Date(1000000000000L));
        user.setSex(Sex.MALE);
        user.setUsername("johndoe");
        user.setBio("My bio");
        user.setLocation("New York");
        user.setInterests("coding");
        user.setPremium(true);
        user.setAdmin(true);

        assertEquals(1, user.getId());
        assertEquals("test@test.com", user.getEmail());
        assertEquals("pass", user.getPassword());
        assertEquals("John", user.getName());
        assertEquals("Doe", user.getSurname());
        assertEquals(Sex.MALE, user.getSex());
        assertEquals("johndoe", user.getUsername());
        assertEquals("My bio", user.getBio());
        assertEquals("New York", user.getLocation());
        assertEquals("coding", user.getInterests());
        assertEquals(true, user.getPremium());
        assertEquals(true, user.getAdmin());
        assertEquals(new Date(1000000000000L), user.getDateOfBirth());
    }

    @Test
    void testUserLikeEntity() {
        User liker = new User();
        User liked = new User();
        
        UserLike like = new UserLike();
        like.setId(1);
        like.setLiker(liker);
        like.setLiked(liked);
        like.setCreatedAt(new Date(1000000000000L));

        assertEquals(1, like.getId());
        assertEquals(liker, like.getLiker());
        assertEquals(liked, like.getLiked());
        assertEquals(new Date(1000000000000L), like.getCreatedAt());
    }

    @Test
    void testUserDislikeEntity() {
        User disliker = new User();
        User disliked = new User();
        
        UserDislike dislike = new UserDislike();
        dislike.setId(1);
        dislike.setDisliker(disliker);
        dislike.setDisliked(disliked);
        dislike.setCreatedAt(new Date(1000000000000L));

        assertEquals(1, dislike.getId());
        assertEquals(disliker, dislike.getDisliker());
        assertEquals(disliked, dislike.getDisliked());
        assertEquals(new Date(1000000000000L), dislike.getCreatedAt());
    }

    @Test
    void testUserPreferencesEntity() {
        User user = new User();
        
        UserPreferences prefs = new UserPreferences();
        prefs.setId(1);
        prefs.setUser(user);
        prefs.setTargetGender("F");
        prefs.setMinAge(20);
        prefs.setMaxAge(30);

        assertEquals(1, prefs.getId());
        assertEquals(user, prefs.getUser());
        assertEquals("F", prefs.getTargetGender());
        assertEquals(20, prefs.getMinAge());
        assertEquals(30, prefs.getMaxAge());
    }

    @Test
    void testUserImageEntity() {
        User user = new User();
        
        UserImage image = new UserImage();
        image.setId(1);
        image.setUser(user);
        image.setContent(new byte[]{1, 2, 3});
        image.setContentType("image/jpeg");
        image.setFileName("test.jpg");
        image.setProfileImage(true);

        assertEquals(1, image.getId());
        assertEquals(user, image.getUser());
        assertEquals(3, image.getContent().length);
        assertEquals("image/jpeg", image.getContentType());
        assertEquals("test.jpg", image.getFileName());
        assertEquals(true, image.getProfileImage());
    }
}
