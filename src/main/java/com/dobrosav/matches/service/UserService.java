package com.dobrosav.matches.service;

import com.dobrosav.matches.api.model.request.ProfileUpdateRequest;
import com.dobrosav.matches.api.model.request.UserPreferencesRequest;
import com.dobrosav.matches.api.model.response.MatchResponse;
import com.dobrosav.matches.api.model.response.UserImageResponse;
import com.dobrosav.matches.api.model.response.UserResponse;
import com.dobrosav.matches.db.entities.*;
import com.dobrosav.matches.db.repos.*;
import com.dobrosav.matches.db.repos.RefreshTokenRepository;
import com.dobrosav.matches.exception.ErrorType;
import com.dobrosav.matches.exception.ServiceException;
import com.dobrosav.matches.mapper.UserMapper;
import com.dobrosav.matches.db.specifications.UserSpecification;
import com.dobrosav.matches.utils.CitiesData;
import org.springframework.data.jpa.domain.Specification;
import org.joda.time.DateTime;
import org.joda.time.Years;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepo userRepo;
    private final UserImageRepo userImageRepo;
    private final UserLikeRepo userLikeRepo;
    private final UserMatchRepo userMatchRepo;
    private final UserPreferencesRepo userPreferencesRepo;
    private final UserDislikeRepo userDislikeRepo;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final CacheManager cacheManager;

    @Autowired
    public UserService(UserRepo userRepo, UserImageRepo userImageRepo, UserLikeRepo userLikeRepo, UserMatchRepo userMatchRepo, UserPreferencesRepo userPreferencesRepo, UserDislikeRepo userDislikeRepo, RefreshTokenRepository refreshTokenRepository, UserMapper userMapper, PasswordEncoder passwordEncoder, CacheManager cacheManager) {
        this.userRepo = userRepo;
        this.userImageRepo = userImageRepo;
        this.userLikeRepo = userLikeRepo;
        this.userMatchRepo = userMatchRepo;
        this.userPreferencesRepo = userPreferencesRepo;
        this.userDislikeRepo = userDislikeRepo;
        this.refreshTokenRepository = refreshTokenRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.cacheManager = cacheManager;
    }

    public User findByMail(String mail) {
        return userRepo.findByEmail(mail)
                .orElseThrow(() -> new ServiceException(ErrorType.USER_NOT_FOUND, HttpStatus.NOT_FOUND));
    }

    @Cacheable(value = "users", key = "#mail")
    public UserResponse getUserByMail(String mail) {
        return userMapper.toDto(findByMail(mail));
    }

    @Transactional
    @CacheEvict(value = {"users", "feed", "matches"}, key = "#mail")
    public void deleteUser(String mail) {
        User user = findByMail(mail);
        refreshTokenRepository.deleteByUser(user);
        userImageRepo.deleteByUser(user);
        userLikeRepo.deleteByLiker(user);
        userLikeRepo.deleteByLiked(user);
        userDislikeRepo.deleteByDisliker(user);
        userDislikeRepo.deleteByDisliked(user);
        userMatchRepo.deleteByUser1(user);
        userMatchRepo.deleteByUser2(user);
        userRepo.delete(user);
    }

    public List<UserResponse> findByAge(Integer begin, Integer end) throws Exception {
        List<User> users;
        users = userRepo.findAll();
        List<User> filteredUsers;
        if (end == null) {
            filteredUsers = users.stream().filter(user -> Years.yearsBetween(new DateTime(user.getDateOfBirth().getTime()), DateTime.now()).getYears() >= begin).toList();
        } else
            filteredUsers = users.stream().filter(user ->
                            Years.yearsBetween(new DateTime(user.getDateOfBirth().getTime()), DateTime.now()).getYears() >= begin)
                    .filter(user -> Years.yearsBetween(new DateTime(user.getDateOfBirth().getTime()), DateTime.now()).getYears() <= end).
                    toList();

        return filteredUsers.stream().map(userMapper::toDto).collect(Collectors.toList());
    }


    public UserImageResponse uploadImage(String mail, MultipartFile file) throws IOException {
        User user = findByMail(mail);
        long imageCount = userImageRepo.countByUser(user);
        if (imageCount >= 3) {
            throw new ServiceException(ErrorType.MAX_IMAGES_REACHED, HttpStatus.BAD_REQUEST);
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new ServiceException(ErrorType.INVALID_IMAGE, HttpStatus.BAD_REQUEST);
        }

        boolean isProfileImage = (imageCount == 0);

        UserImage userImage = new UserImage(user, file.getBytes(), contentType, file.getOriginalFilename(), isProfileImage);
        userImageRepo.save(userImage);

        return new UserImageResponse(userImage.getId(), userImage.getFileName(), userImage.getContentType(), userImage.getProfileImage());
    }

    public List<UserImageResponse> getUserImages(String mail) {
        User user = findByMail(mail);
        List<UserImage> images = userImageRepo.findByUser(user);
        return images.stream()
                .map(img -> new UserImageResponse(img.getId(), img.getFileName(), img.getContentType(), img.getProfileImage()))
                .collect(Collectors.toList());
    }

    @CacheEvict(value = "images", key = "#imageId")
    public void deleteImage(String mail, Integer imageId) {
        User user = findByMail(mail);
        UserImage image = userImageRepo.findByIdAndUser(imageId, user)
                .orElseThrow(() -> new ServiceException(ErrorType.IMAGE_NOT_FOUND, HttpStatus.NOT_FOUND));

        boolean wasProfileImage = Boolean.TRUE.equals(image.getProfileImage());
        userImageRepo.delete(image);
        userImageRepo.flush(); // Ensure deletion is synchronized with DB

        if (wasProfileImage) {
            // If the deleted image was the profile image, promote the first available image to profile image
            List<UserImage> remainingImages = userImageRepo.findByUser(user);
            if (!remainingImages.isEmpty()) {
                UserImage newProfileImage = remainingImages.get(0);
                newProfileImage.setProfileImage(true);
                userImageRepo.save(newProfileImage);
            }
        }
    }

    @Transactional
    public void setProfileImage(String mail, Integer imageId) {
        User user = findByMail(mail);

        // Ensure the image exists and belongs to the user
        UserImage newProfileImage = userImageRepo.findByIdAndUser(imageId, user)
                .orElseThrow(() -> new ServiceException(ErrorType.IMAGE_NOT_FOUND, HttpStatus.NOT_FOUND));

        // Unset current profile image if it's different
        userImageRepo.findByUserAndProfileImageTrue(user).ifPresent(currentProfileImage -> {
            if (!currentProfileImage.getId().equals(newProfileImage.getId())) {
                currentProfileImage.setProfileImage(false);
                userImageRepo.save(currentProfileImage);
                // Evict cache for old profile image
                Objects.requireNonNull(cacheManager.getCache("images")).evict(currentProfileImage.getId());
            }
        });

        // Set new profile image
        if (!Boolean.TRUE.equals(newProfileImage.getProfileImage())) {
            newProfileImage.setProfileImage(true);
            userImageRepo.save(newProfileImage);
            // Evict cache for new profile image
            Objects.requireNonNull(cacheManager.getCache("images")).evict(newProfileImage.getId());
        }
    }

    @Cacheable(value = "images", key = "#imageId")
    public UserImage getImage(String mail, Integer imageId) {
        User user = findByMail(mail);
        return userImageRepo.findByIdAndUser(imageId, user)
                .orElseThrow(() -> new ServiceException(ErrorType.IMAGE_NOT_FOUND, HttpStatus.NOT_FOUND));
    }

    @CacheEvict(value = "feed", key = "#likerMail")
    public boolean likeUser(String likerMail, Integer likedUserId) {
        User liker = findByMail(likerMail);

        if (!liker.getPremium()) {
            Date twentyFourHoursAgo = new DateTime().minusDays(1).toDate();
            if (userLikeRepo.countByLikerAndCreatedAtAfter(liker, twentyFourHoursAgo) >= 10) {
                throw new ServiceException(ErrorType.DAILY_LIKE_LIMIT_REACHED, HttpStatus.FORBIDDEN);
            }
        }

        User liked = userRepo.findById(likedUserId)
                .orElseThrow(() -> new ServiceException(ErrorType.USER_NOT_FOUND, HttpStatus.NOT_FOUND));

        if (liker.getId().equals(liked.getId())) {
            throw new ServiceException(ErrorType.CANNOT_LIKE_SELF, HttpStatus.BAD_REQUEST);
        }

        if (userLikeRepo.findByLikerAndLiked(liker, liked).isPresent()) {
            throw new ServiceException(ErrorType.ALREADY_LIKED, HttpStatus.BAD_REQUEST);
        }

        UserLike like = new UserLike(liker, liked);
        userLikeRepo.save(like);

        // Check if it's a match (mutual like)
        if (userLikeRepo.findByLikerAndLiked(liked, liker).isPresent()) {
            UserMatch match = new UserMatch(liker, liked);
            userMatchRepo.save(match);
            
            // Invalidate matches cache for both users
            Objects.requireNonNull(cacheManager.getCache("matches")).evict(likerMail);
            Objects.requireNonNull(cacheManager.getCache("matches")).evict(liked.getEmail());
            // Also invalidate feed for the liked user (so they don't see liker anymore in feed, if they haven't liked yet - but here they have)
            // Actually, if it's a match, they have both liked each other, so they wouldn't be in each other's feed anyway.
            
            return true;
        }

        return false;
    }

    @Cacheable(value = "matches", key = "#mail")
    @Transactional(readOnly = true)
    public List<MatchResponse> getMatches(String mail) {
        User user = findByMail(mail);
        List<UserMatch> matches = userMatchRepo.findAllMatchesForUser(user);

        return matches.stream().map(match -> {
            User otherUser = match.getUser1().getId().equals(user.getId()) ? match.getUser2() : match.getUser1();
            return new MatchResponse(match.getId(), userMapper.toDto(otherUser));
        }).collect(Collectors.toList());
    }

    @CacheEvict(value = {"users", "feed"}, key = "#mail")
    @Transactional
    public UserResponse updatePreferences(String mail, UserPreferencesRequest request) {
        User user = findByMail(mail);
        UserPreferences preferences = user.getPreferences();
        if (preferences == null) {
            preferences = new UserPreferences(user);
            user.setPreferences(preferences);
        }
        preferences.setTargetGender(request.getTargetGender());
        preferences.setMinAge(request.getMinAge());
        preferences.setMaxAge(request.getMaxAge());

        // userPreferencesRepo.save(preferences); // This is not needed due to cascading
        return userMapper.toDto(user);
    }

    @CacheEvict(value = "feed", key = "#dislikerMail")
    public void dislikeUser(String dislikerMail, Integer dislikedUserId) {
        User disliker = findByMail(dislikerMail);
        User disliked = userRepo.findById(dislikedUserId)
                .orElseThrow(() -> new ServiceException(ErrorType.USER_NOT_FOUND, HttpStatus.NOT_FOUND));

        UserDislike dislike = new UserDislike(disliker, disliked);
        userDislikeRepo.save(dislike);
    }

    @Cacheable(value = "feed", key = "#mail")
    @Transactional(readOnly = true)
    public List<UserResponse> getFeed(String mail) {
        User currentUser = findByMail(mail);

        List<Integer> excludedUserIds = new ArrayList<>();
        excludedUserIds.add(currentUser.getId()); // Exclude self

        // Exclude liked users
        userLikeRepo.findByLiker(currentUser).forEach(like -> excludedUserIds.add(like.getLiked().getId()));

        // Exclude disliked users
        userDislikeRepo.findByDisliker(currentUser).forEach(dislike -> excludedUserIds.add(dislike.getDisliked().getId()));

        // Exclude matched users
        userMatchRepo.findAllMatchesForUser(currentUser).forEach(match -> {
            excludedUserIds.add(match.getUser1().getId());
            excludedUserIds.add(match.getUser2().getId());
        });

        List<User> feedUsers = userRepo.findByIdNotIn(excludedUserIds);

        return feedUsers.stream().map(userMapper::toDto).collect(Collectors.toList());
    }

    @Cacheable(value = "filtered-feed", key = "#mail + '-' + #gender + '-' + #minAge + '-' + #maxAge")
    @Transactional(readOnly = true)
    public List<UserResponse> getFilteredFeed(String mail, String gender, Integer minAge, Integer maxAge, String location) {
        User currentUser = findByMail(mail);

        List<Integer> excludedUserIds = new ArrayList<>();
        excludedUserIds.add(currentUser.getId()); // Exclude self

        userLikeRepo.findByLiker(currentUser).forEach(like -> excludedUserIds.add(like.getLiked().getId()));
        userDislikeRepo.findByDisliker(currentUser).forEach(dislike -> excludedUserIds.add(dislike.getDisliked().getId()));
        userMatchRepo.findAllMatchesForUser(currentUser).forEach(match -> {
            excludedUserIds.add(match.getUser2().getId());
        });

        List<String> allowedLocations = new ArrayList<>();
        
        if (location != null && !location.isEmpty() && !location.equalsIgnoreCase("Any")) {
            allowedLocations.add(location);
        }
        Specification<User> spec = UserSpecification.filter(excludedUserIds, gender, minAge, maxAge, allowedLocations);

        Pageable pageable = PageRequest.of(0, 25);
        Page<User> feedUsers = userRepo.findAll(spec, pageable);

        return feedUsers.stream().map(userMapper::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserResponse> searchUsers(String mail) {
        User currentUser = findByMail(mail);
        UserPreferences prefs = currentUser.getPreferences();

        List<User> allUsers = userRepo.findAll(); // In a real app, this would be a more targeted query

        return allUsers.stream()
                .filter(user -> !user.getId().equals(currentUser.getId())) // Not self
                .filter(user -> {
                    int age = Years.yearsBetween(new DateTime(user.getDateOfBirth().getTime()), DateTime.now()).getYears();
                    return age >= prefs.getMinAge() && age <= prefs.getMaxAge();
                })
                .filter(user -> {
                    if ("Any".equalsIgnoreCase(prefs.getTargetGender())) {
                        return true;
                    }
                    if (user.getSex() == null) {
                        return false;
                    }
                    return user.getSex().name().equalsIgnoreCase(prefs.getTargetGender());
                })
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getLikers(String mail) {
        User currentUser = findByMail(mail);
        if (!currentUser.getPremium()) {
            throw new ServiceException(ErrorType.PREMIUM_FEATURE_ONLY, HttpStatus.FORBIDDEN);
        }
        return userLikeRepo.findByLiked(currentUser).stream()
                .map(UserLike::getLiker)
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    @CacheEvict(value = {"users", "feed"}, key = "#mail")
    @Transactional
    public void setPremium(String mail, boolean isPremium) {
        User user = findByMail(mail);
        user.setPremium(isPremium);
        userRepo.save(user);
    }

    public User findById(Integer id) {
        return userRepo.findById(id)
                .orElseThrow(() -> new ServiceException(ErrorType.USER_NOT_FOUND, HttpStatus.NOT_FOUND));
    }

    @Transactional
    public User updateUserProfile(Integer id, ProfileUpdateRequest request) {
        User user = findById(id);
        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }
        if (request.getInterests() != null) {
            user.setInterests(request.getInterests());
        }
        if (request.getLocation() != null) {
            user.setLocation(request.getLocation());
        }
        User savedUser = userRepo.save(user);

        // Manual eviction
        Objects.requireNonNull(cacheManager.getCache("users")).evict(savedUser.getEmail());

        return savedUser;
    }
}
