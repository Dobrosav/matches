package com.dobrosav.matches.service;

import com.dobrosav.matches.api.model.request.ProfileUpdateRequest;
import com.dobrosav.matches.api.model.request.UserPreferencesRequest;
import com.dobrosav.matches.api.model.response.MatchResponse;
import com.dobrosav.matches.api.model.response.UserImageResponse;
import com.dobrosav.matches.api.model.response.UserResponse;
import com.dobrosav.matches.db.entities.*;
import com.dobrosav.matches.db.repos.*;
import com.dobrosav.matches.exception.ServiceException;
import com.dobrosav.matches.mapper.UserMapper;
import org.springframework.data.jpa.domain.Specification;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceUnitTest {

    @Mock
    private UserRepo userRepo;
    @Mock
    private UserImageRepo userImageRepo;
    @Mock
    private UserLikeRepo userLikeRepo;
    @Mock
    private UserMatchRepo userMatchRepo;
    @Mock
    private UserPreferencesRepo userPreferencesRepo;
    @Mock
    private UserDislikeRepo userDislikeRepo;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private CacheManager cacheManager;
    @Mock
    private Cache cache;

    @InjectMocks
    private UserService userService;

    @Test
    void testFindByMail_Success() {
        User user = new User();
        user.setEmail("test@test.com");
        when(userRepo.findByEmail("test@test.com")).thenReturn(Optional.of(user));

        User result = userService.findByMail("test@test.com");
        assertNotNull(result);
        assertEquals("test@test.com", result.getEmail());
    }

    @Test
    void testFindByMail_NotFound() {
        when(userRepo.findByEmail("test@test.com")).thenReturn(Optional.empty());

        ServiceException exception = assertThrows(ServiceException.class, () -> userService.findByMail("test@test.com"));
        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpCode());
    }

    @Test
    void testGetUserByMail_Success() {
        User user = new User();
        user.setEmail("test@test.com");
        when(userRepo.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(new UserResponse(1, "Name", "Surname", "test@test.com", false, false, Sex.MALE, "username", new Date(), "none", "loc", "bio", "interests"));

        UserResponse result = userService.getUserByMail("test@test.com");
        assertNotNull(result);
        assertEquals("test@test.com", result.getEmail());
    }

    @Test
    void testDeleteUser() {
        User user = new User();
        user.setEmail("test@test.com");
        when(userRepo.findByEmail("test@test.com")).thenReturn(Optional.of(user));

        userService.deleteUser("test@test.com");

        verify(refreshTokenRepository).deleteByUser(user);
        verify(userImageRepo).deleteByUser(user);
        verify(userLikeRepo).deleteByLiker(user);
        verify(userLikeRepo).deleteByLiked(user);
        verify(userDislikeRepo).deleteByDisliker(user);
        verify(userDislikeRepo).deleteByDisliked(user);
        verify(userMatchRepo).deleteByUser1(user);
        verify(userMatchRepo).deleteByUser2(user);
        verify(userRepo).delete(user);
    }

    @Test
    void testUploadImage_Success_Profile() throws IOException {
        User user = new User();
        user.setEmail("test@test.com");
        when(userRepo.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(userImageRepo.countByUser(user)).thenReturn(0L);
        when(userImageRepo.save(any(UserImage.class))).thenAnswer(invocation -> {
            UserImage saved = invocation.getArgument(0);
            saved.setId(1);
            return saved;
        });

        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "content".getBytes());
        UserImageResponse response = userService.uploadImage("test@test.com", file);

        assertNotNull(response);
        assertTrue(response.getProfileImage());
    }

    @Test
    void testUploadImage_MaxLimit() {
        User user = new User();
        when(userRepo.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(userImageRepo.countByUser(user)).thenReturn(3L);

        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "content".getBytes());
        ServiceException ex = assertThrows(ServiceException.class, () -> userService.uploadImage("test@test.com", file));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getHttpCode());
    }

    @Test
    void testUploadImage_InvalidContentType() {
        User user = new User();
        when(userRepo.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(userImageRepo.countByUser(user)).thenReturn(0L);

        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "content".getBytes());
        ServiceException ex = assertThrows(ServiceException.class, () -> userService.uploadImage("test@test.com", file));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getHttpCode());
    }

    @Test
    void testGetUserImages() {
        User user = new User();
        UserImage img = new UserImage();
        img.setId(1);
        img.setFileName("test.jpg");
        when(userRepo.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(userImageRepo.findByUser(user)).thenReturn(Collections.singletonList(img));

        List<UserImageResponse> images = userService.getUserImages("test@test.com");
        assertEquals(1, images.size());
        assertEquals("test.jpg", images.get(0).getFileName());
    }

    @Test
    void testDeleteImage_Success() {
        User user = new User();
        UserImage img = new UserImage();
        img.setId(1);
        img.setProfileImage(false);
        
        when(userRepo.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(userImageRepo.findByIdAndUser(1, user)).thenReturn(Optional.of(img));

        userService.deleteImage("test@test.com", 1);
        verify(userImageRepo).delete(img);
    }
    
    @Test
    void testDeleteImage_ProfileImagePromotion() {
        User user = new User();
        UserImage profileImg = new UserImage();
        profileImg.setId(1);
        profileImg.setProfileImage(true);
        
        UserImage nextImg = new UserImage();
        nextImg.setId(2);
        nextImg.setProfileImage(false);

        when(userRepo.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(userImageRepo.findByIdAndUser(1, user)).thenReturn(Optional.of(profileImg));
        when(userImageRepo.findByUser(user)).thenReturn(Collections.singletonList(nextImg));

        userService.deleteImage("test@test.com", 1);
        
        verify(userImageRepo).delete(profileImg);
        verify(userImageRepo).save(nextImg);
        assertTrue(nextImg.getProfileImage());
    }

    @Test
    void testSetProfileImage() {
        User user = new User();
        UserImage newProfile = new UserImage();
        newProfile.setId(2);
        newProfile.setProfileImage(false);
        
        UserImage currentProfile = new UserImage();
        currentProfile.setId(1);
        currentProfile.setProfileImage(true);

        when(userRepo.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(userImageRepo.findByIdAndUser(2, user)).thenReturn(Optional.of(newProfile));
        when(userImageRepo.findByUserAndProfileImageTrue(user)).thenReturn(Optional.of(currentProfile));
        
        when(cacheManager.getCache("images")).thenReturn(cache);

        userService.setProfileImage("test@test.com", 2);

        verify(cacheManager, atLeastOnce()).getCache("images");
        assertFalse(currentProfile.getProfileImage());
        assertTrue(newProfile.getProfileImage());
    }

    @Test
    void testLikeUser_Success_Match() {
        User liker = new User();
        liker.setId(1);
        liker.setPremium(true);
        liker.setEmail("liker@test.com");
        
        User liked = new User();
        liked.setId(2);
        liked.setEmail("liked@test.com");

        when(userRepo.findByEmail("liker@test.com")).thenReturn(Optional.of(liker));
        when(userRepo.findById(2)).thenReturn(Optional.of(liked));
        when(userLikeRepo.findByLikerAndLiked(liker, liked)).thenReturn(Optional.empty()); // Not already liked by liker
        when(userLikeRepo.findByLikerAndLiked(liked, liker)).thenReturn(Optional.of(new UserLike())); // Liked by the other user (match)
        when(cacheManager.getCache("matches")).thenReturn(cache);

        boolean result = userService.likeUser("liker@test.com", 2);

        assertTrue(result);
        verify(userMatchRepo).save(any(UserMatch.class));
    }

    @Test
    void testLikeUser_DailyLimit() {
        User liker = new User();
        liker.setId(1);
        liker.setPremium(false);
        
        when(userRepo.findByEmail("liker@test.com")).thenReturn(Optional.of(liker));
        when(userLikeRepo.countByLikerAndCreatedAtAfter(eq(liker), any())).thenReturn(10L);

        ServiceException ex = assertThrows(ServiceException.class, () -> userService.likeUser("liker@test.com", 2));
        assertEquals(HttpStatus.FORBIDDEN, ex.getHttpCode());
    }

    @Test
    void testUpdatePreferences() {
        User user = new User();
        user.setEmail("test@test.com");
        when(userRepo.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(new UserResponse(1, "Name", "Surname", "test@test.com", false, false, Sex.MALE, "username", new Date(), "none", "loc", "bio", "interests"));

        UserPreferencesRequest req = new UserPreferencesRequest();
        req.setTargetGender("F");
        req.setMinAge(20);
        req.setMaxAge(30);

        UserResponse res = userService.updatePreferences("test@test.com", req);
        
        assertNotNull(user.getPreferences());
        assertEquals("F", user.getPreferences().getTargetGender());
    }

    @Test
    void testDislikeUser() {
        User disliker = new User();
        disliker.setEmail("disliker@test.com");
        User disliked = new User();
        disliked.setId(2);

        when(userRepo.findByEmail("disliker@test.com")).thenReturn(Optional.of(disliker));
        when(userRepo.findById(2)).thenReturn(Optional.of(disliked));

        userService.dislikeUser("disliker@test.com", 2);

        verify(userDislikeRepo).save(any(UserDislike.class));
    }

    @Test
    void testGetFeed() {
        User user = new User();
        user.setId(1);
        user.setEmail("test@test.com");
        
        when(userRepo.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(userRepo.findByIdNotIn(anyList())).thenReturn(Collections.emptyList());

        List<UserResponse> feed = userService.getFeed("test@test.com");
        assertNotNull(feed);
        assertTrue(feed.isEmpty());
    }

    @Test
    void testGetFilteredFeed() {
        User user = new User();
        user.setId(1);
        user.setEmail("test@test.com");
        
        when(userRepo.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        Page<User> page = new PageImpl<>(Collections.emptyList());
        
        when(userRepo.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        List<UserResponse> feed = userService.getFilteredFeed("test@test.com", "F", 20, 30, null);
        assertNotNull(feed);
    }

    @Test
    void testGetLikers_Premium() {
        User user = new User();
        user.setPremium(true);
        when(userRepo.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(userLikeRepo.findByLiked(user)).thenReturn(Collections.emptyList());

        List<UserResponse> likers = userService.getLikers("test@test.com");
        assertNotNull(likers);
    }

    @Test
    void testGetLikers_NonPremium() {
        User user = new User();
        user.setPremium(false);
        when(userRepo.findByEmail("test@test.com")).thenReturn(Optional.of(user));

        ServiceException ex = assertThrows(ServiceException.class, () -> userService.getLikers("test@test.com"));
        assertEquals(HttpStatus.FORBIDDEN, ex.getHttpCode());
    }

    @Test
    void testSetPremium() {
        User user = new User();
        when(userRepo.findByEmail("test@test.com")).thenReturn(Optional.of(user));

        userService.setPremium("test@test.com", true);

        assertTrue(user.getPremium());
        verify(userRepo).save(user);
    }
    
    @Test
    void testUpdateUserProfile() {
        User user = new User();
        user.setId(1);
        
        when(userRepo.findById(1)).thenReturn(Optional.of(user));
        when(userRepo.save(user)).thenReturn(user);
        when(cacheManager.getCache("users")).thenReturn(cache);

        ProfileUpdateRequest req = new ProfileUpdateRequest();
        req.setBio("New Bio");
        
        userService.updateUserProfile(1, req);
        
        assertEquals("New Bio", user.getBio());
        verify(cacheManager).getCache("users");
    }
}
