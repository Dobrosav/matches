package com.dobrosav.matches.service;

import com.dobrosav.matches.api.model.request.UserRequest;
import com.dobrosav.matches.api.model.response.MatchResponse;
import com.dobrosav.matches.api.model.response.UserImageResponse;
import com.dobrosav.matches.api.model.response.UserResponse;
import com.dobrosav.matches.api.model.request.UserPreferencesRequest;
import com.dobrosav.matches.db.entities.User;
import com.dobrosav.matches.db.repos.*;
import com.dobrosav.matches.exception.ServiceException;
import com.dobrosav.matches.security.AuthenticationService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
public class UserServiceTest {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0.33");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7.2")
            .withExposedPorts(6379);

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private UserImageRepo userImageRepo;

    @Autowired
    private UserLikeRepo userLikeRepo;

    @Autowired
    private UserDislikeRepo userDislikeRepo;

    @Autowired
    private UserMatchRepo userMatchRepo;

    @Autowired
    private ChatMessageRepo chatMessageRepo;

    @Autowired
    private UserPreferencesRepo userPreferencesRepo;

    @Autowired
    private RefreshTokenRepository refreshTokenRepo;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");

        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> String.valueOf(redis.getMappedPort(6379)));
    }

    @BeforeEach
    void setUp() {
        chatMessageRepo.deleteAll();
        userDislikeRepo.deleteAll();
        userLikeRepo.deleteAll();
        userMatchRepo.deleteAll();
        userImageRepo.deleteAll();
        refreshTokenRepo.deleteAll();
        userPreferencesRepo.deleteAll();
        userRepo.deleteAll();
    }

    @AfterEach
    void tearDown() {
        chatMessageRepo.deleteAll();
        userDislikeRepo.deleteAll();
        userLikeRepo.deleteAll();
        userMatchRepo.deleteAll();
        userImageRepo.deleteAll();
        refreshTokenRepo.deleteAll();
        userPreferencesRepo.deleteAll();
        userRepo.deleteAll();
    }

    @Test
    void testUploadImage() throws Exception {
        UserRequest userRequest = new UserRequest();
        userRequest.setName("Test");
        userRequest.setSurname("User");
        userRequest.setEmail("test@example.com");
        userRequest.setUsername("testuser");
        userRequest.setPassword("password");
        userRequest.setSex("M");
        userRequest.setDateOfBirth(new Date());

        authenticationService.register(userRequest);

        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test image content".getBytes());
        UserImageResponse response = userService.uploadImage("test@example.com", file);

        assertNotNull(response);
        assertEquals("test.jpg", response.getFileName());
        assertTrue(response.getProfileImage());

        List<UserImageResponse> images = userService.getUserImages("test@example.com");
        assertEquals(1, images.size());
    }

    @Test
    void testUploadMaxImages() throws Exception {
        UserRequest userRequest = new UserRequest();
        userRequest.setName("Test");
        userRequest.setSurname("User");
        userRequest.setEmail("test@example.com");
        userRequest.setUsername("testuser");
        userRequest.setPassword("password");
        userRequest.setSex("M");
        userRequest.setDateOfBirth(new Date());

        authenticationService.register(userRequest);

        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test image content".getBytes());
        userService.uploadImage("test@example.com", file);
        userService.uploadImage("test@example.com", file);
        userService.uploadImage("test@example.com", file);

        assertThrows(ServiceException.class, () -> userService.uploadImage("test@example.com", file));
    }

    @Test
    void testLikeAndMatch() throws Exception {
        UserRequest user1Request = new UserRequest();
        user1Request.setName("User1");
        user1Request.setSurname("One");
        user1Request.setEmail("user1@example.com");
        user1Request.setUsername("user1");
        user1Request.setPassword("password");
        user1Request.setSex("M");
        user1Request.setDateOfBirth(new Date());

        UserRequest user2Request = new UserRequest();
        user2Request.setName("User2");
        user2Request.setSurname("Two");
        user2Request.setEmail("user2@example.com");
        user2Request.setUsername("user2");
        user2Request.setPassword("password");
        user2Request.setSex("F");
        user2Request.setDateOfBirth(new Date());

        authenticationService.register(user1Request);
        authenticationService.register(user2Request);
        User user1 = userRepo.findByEmail("user1@example.com").get();
        User user2 = userRepo.findByEmail("user2@example.com").get();


        // User 1 likes User 2
        boolean match1 = userService.likeUser("user1@example.com", user2.getId());
        assertFalse(match1); // Not a match yet

        // User 2 likes User 1
        boolean match2 = userService.likeUser("user2@example.com", user1.getId());
        assertTrue(match2); // Should be a match now

        List<MatchResponse> matches1 = userService.getMatches("user1@example.com");
        assertEquals(1, matches1.size());
        assertEquals(user2.getId(), matches1.get(0).getOtherUser().getId());

        List<MatchResponse> matches2 = userService.getMatches("user2@example.com");
        assertEquals(1, matches2.size());
        assertEquals(user1.getId(), matches2.get(0).getOtherUser().getId());
    }

    @Test
    void testPasswordHashing() throws Exception {
        UserRequest userRequest = new UserRequest();
        userRequest.setName("Hash");
        userRequest.setSurname("Test");
        userRequest.setEmail("hash@example.com");
        userRequest.setUsername("hashtest");
        userRequest.setPassword("plainpassword");
        userRequest.setSex("M");
        userRequest.setDateOfBirth(new Date());

        authenticationService.register(userRequest);

        User user = userRepo.findByEmail("hash@example.com").get();
        assertNotNull(user);
        assertNotEquals("plainpassword", user.getPassword());
    }

    @Test
    void testFeedExclusions() throws Exception {
        UserRequest mainUserReq = new UserRequest("Main", "User", "main@example.com", "mainuser", "p", "M", new Date(), "");
        UserRequest likedUserReq = new UserRequest("Liked", "User", "liked@example.com", "likeduser", "p", "F", new Date(), "");
        UserRequest dislikedUserReq = new UserRequest("Disliked", "User", "disliked@example.com", "dislikeduser", "p", "F", new Date(), "");
        UserRequest otherUserReq = new UserRequest("Other", "User", "other@example.com", "otheruser", "p", "F", new Date(), "");

        authenticationService.register(mainUserReq);
        authenticationService.register(likedUserReq);
        authenticationService.register(dislikedUserReq);
        authenticationService.register(otherUserReq);

        User mainUser = userRepo.findByEmail("main@example.com").get();
        User likedUser = userRepo.findByEmail("liked@example.com").get();
        User dislikedUser = userRepo.findByEmail("disliked@example.com").get();


        userService.likeUser(mainUser.getEmail(), likedUser.getId());
        userService.dislikeUser(mainUser.getEmail(), dislikedUser.getId());

        List<UserResponse> feed = userService.getFeed(mainUser.getEmail());

        assertEquals(1, feed.size());
        assertEquals("other@example.com", feed.get(0).getEmail());
    }

    @Test
    void testPremiumFeatures() throws Exception {
        UserRequest premiumUserReq = new UserRequest("Premium", "User", "premium@example.com", "premuser", "p", "M", new Date(), "");
        UserRequest normalUserReq = new UserRequest("Normal", "User", "normal@example.com", "normuser", "p", "F", new Date(), "");

        authenticationService.register(premiumUserReq);
        authenticationService.register(normalUserReq);
        User premiumUser = userRepo.findByEmail("premium@example.com").get();
        User normalUser = userRepo.findByEmail("normal@example.com").get();


        userService.setPremium(premiumUser.getEmail(), true);

        // Test "who liked me"
        userService.likeUser(normalUser.getEmail(), premiumUser.getId());

        // Premium can see likers
        List<UserResponse> likers = userService.getLikers(premiumUser.getEmail());
        assertEquals(1, likers.size());
        assertEquals(normalUser.getId(), likers.get(0).getId());

        // Normal user cannot
        assertThrows(ServiceException.class, () -> userService.getLikers(normalUser.getEmail()));

        // Test like limit
        for (int i = 0; i < 9; i++) {
            UserRequest tempUserReq = new UserRequest("Temp" + i, "User", "temp" + i + "@example.com", "temp" + i, "p", "F", new Date(), "");
            authenticationService.register(tempUserReq);
            User tempUser = userRepo.findByEmail("temp" + i + "@example.com").get();
            userService.likeUser(normalUser.getEmail(), tempUser.getId());
        }

        UserRequest extraUserReq = new UserRequest("Extra", "User", "extra@example.com", "extra", "p", "F", new Date(), "");
        authenticationService.register(extraUserReq);
        User extraUser = userRepo.findByEmail("extra@example.com").get();

        assertThrows(ServiceException.class, () -> userService.likeUser(normalUser.getEmail(), extraUser.getId()));
    }

    @Test
    void testUploadInvalidImage() throws Exception {
        UserRequest userRequest = new UserRequest("Invalid", "Image", "invalid@example.com", "invalidimg", "p", "M", new Date(), "");
        authenticationService.register(userRequest);

        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "not an image".getBytes());
        assertThrows(ServiceException.class, () -> userService.uploadImage("invalid@example.com", file));
    }


    @Test
    void testGetUserImagesEmpty() throws Exception {
        UserRequest userRequest = new UserRequest("No", "Images", "noimages@example.com", "noimages", "p", "F", new Date(), "");
        authenticationService.register(userRequest);

        List<UserImageResponse> images = userService.getUserImages("noimages@example.com");
        assertTrue(images.isEmpty());
    }

    @Test
    void testDeleteImage() throws Exception {
        UserRequest userRequest = new UserRequest("Delete", "Image", "deleteimg@example.com", "deleteimg", "p", "M", new Date(), "");
        authenticationService.register(userRequest);

        MockMultipartFile file1 = new MockMultipartFile("file1", "first.jpg", "image/jpeg", "content1".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("file2", "second.jpg", "image/jpeg", "content2".getBytes());
        UserImageResponse img1 = userService.uploadImage("deleteimg@example.com", file1);
        userService.uploadImage("deleteimg@example.com", file2);

        assertEquals(2, userService.getUserImages("deleteimg@example.com").size());

        userService.deleteImage("deleteimg@example.com", img1.getId());

        assertEquals(1, userService.getUserImages("deleteimg@example.com").size());
        // Verify that the second image is now the profile image
        UserImageResponse remainingImage = userService.getUserImages("deleteimg@example.com").get(0);
        assertTrue(remainingImage.getProfileImage());
    }

    @Test
    void testUpdatePreferences() throws Exception {
        UserRequest userRequest = new UserRequest("Prefs", "User", "prefs@example.com", "prefsuser", "p", "F", new Date(), "");
        authenticationService.register(userRequest);
        User user = userRepo.findByEmail("prefs@example.com").get();

        UserPreferencesRequest prefsRequest = new UserPreferencesRequest();
        prefsRequest.setTargetGender("Female");
        prefsRequest.setMinAge(25);
        prefsRequest.setMaxAge(35);

        userService.updatePreferences("prefs@example.com", prefsRequest);

        User updatedUser = userRepo.findByEmail("prefs@example.com").get();
        assertEquals("Female", updatedUser.getPreferences().getTargetGender());
        assertEquals(25, updatedUser.getPreferences().getMinAge());
        assertEquals(35, updatedUser.getPreferences().getMaxAge());
    }


    @Test
    void testLikeUserSelf() {
        UserRequest userRequest = new UserRequest("Self", "Liker", "selflike@example.com", "selflike", "p", "M", new Date(), "");
        authenticationService.register(userRequest);
        User user = userRepo.findByEmail("selflike@example.com").get();

        assertThrows(ServiceException.class, () -> userService.likeUser("selflike@example.com", user.getId()));
    }

    @Test
    void testLikeUserAlreadyLiked() {
        UserRequest user1Request = new UserRequest("User1", "Like", "user1like@example.com", "user1like", "p", "M", new Date(), "");
        UserRequest user2Request = new UserRequest("User2", "Like", "user2like@example.com", "user2like", "p", "F", new Date(), "");
        authenticationService.register(user1Request);
        authenticationService.register(user2Request);
        User user2 = userRepo.findByEmail("user2like@example.com").get();

        userService.likeUser("user1like@example.com", user2.getId()); // First like
        assertThrows(ServiceException.class, () -> userService.likeUser("user1like@example.com", user2.getId())); // Second like
    }

    @Test
    void testDeleteNonProfileImage() throws Exception {
        UserRequest userRequest = new UserRequest("Non", "Profile", "nonprofile@example.com", "nonprofile", "p", "F", new Date(), "");
        authenticationService.register(userRequest);

        MockMultipartFile file1 = new MockMultipartFile("file1", "first.jpg", "image/jpeg", "content1".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("file2", "second.jpg", "image/jpeg", "content2".getBytes());
        UserImageResponse img1 = userService.uploadImage("nonprofile@example.com", file1);
        UserImageResponse img2 = userService.uploadImage("nonprofile@example.com", file2);

        userService.deleteImage("nonprofile@example.com", img2.getId()); // Delete non-profile image

        List<UserImageResponse> images = userService.getUserImages("nonprofile@example.com");
        assertEquals(1, images.size());
        assertTrue(images.get(0).getProfileImage()); // First image should still be profile
    }
}
