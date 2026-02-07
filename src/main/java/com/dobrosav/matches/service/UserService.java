package com.dobrosav.matches.service;

import com.dobrosav.matches.api.model.request.LoginRequest;
import com.dobrosav.matches.db.entities.User;
import com.dobrosav.matches.db.repos.UserRepo;
import com.dobrosav.matches.api.model.response.LoginWrapper;
import com.dobrosav.matches.api.model.response.SuccessResult;
import com.dobrosav.matches.api.model.request.UserRequest;
import com.dobrosav.matches.exception.ErrorType;
import com.dobrosav.matches.exception.ServiceException;
import org.joda.time.DateTime;
import org.joda.time.Years;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

import com.dobrosav.matches.api.model.response.MatchResponse;
import com.dobrosav.matches.api.model.request.UserPreferencesRequest;
import com.dobrosav.matches.api.model.response.UserImageResponse;
import com.dobrosav.matches.api.model.response.UserResponse;
import com.dobrosav.matches.db.entities.*;
import com.dobrosav.matches.db.repos.*;
import com.dobrosav.matches.mapper.UserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepo userRepo;
    private final UserImageRepo userImageRepo;
    private final UserLikeRepo userLikeRepo;
    private final UserMatchRepo userMatchRepo;
    private final UserPreferencesRepo userPreferencesRepo;
    private final UserDislikeRepo userDislikeRepo;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepo userRepo, UserImageRepo userImageRepo, UserLikeRepo userLikeRepo, UserMatchRepo userMatchRepo, UserPreferencesRepo userPreferencesRepo, UserDislikeRepo userDislikeRepo, UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.userImageRepo = userImageRepo;
        this.userLikeRepo = userLikeRepo;
        this.userMatchRepo = userMatchRepo;
        this.userPreferencesRepo = userPreferencesRepo;
        this.userDislikeRepo = userDislikeRepo;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @CachePut(value = "users", key = "#request.mail")
    public UserResponse createDefaultUser(UserRequest request) throws Exception {
        if (userRepo.findByUsername(request.getSurname()).isEmpty() && userRepo.findByMail(request.getMail())==null) {
            User user = User.createDefaultUser(request.getName(), request.getSurname(), request.getMail(), request.getUsername(), passwordEncoder.encode(request.getPassword()), request.getSex(), request.getDateOfBirth(), request.getDisabilities());
            
            UserPreferences preferences = new UserPreferences(user);
            user.setPreferences(preferences);

            userRepo.save(user);
            return userMapper.toDto(user);

        } else {
            throw new ServiceException(ErrorType.USER_ALREADY_EXISTS, HttpStatus.CONFLICT);
        }
    }

    public User findByMail(String mail) {
        User user = userRepo.findByMail(mail);
        if (user == null) {
            throw new ServiceException(ErrorType.USER_NOT_FOUND, HttpStatus.NOT_FOUND);
        }
        return user;
    }

    @Cacheable(value = "users", key = "#mail")
    public UserResponse getUserByMail(String mail) {
        return userMapper.toDto(findByMail(mail));
    }

    @CacheEvict(value = "users", key = "#mail")
    public void deleteUser(String mail) {
        User deletedUser = findByMail(mail);
        userRepo.delete(deletedUser);
    }

    public LoginWrapper login(LoginRequest request) {
        LoginWrapper loginWrapper = new LoginWrapper();
        User user = userRepo.findByMail(request.getMail());
        SuccessResult successResult = new SuccessResult();
        if (user != null && passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            successResult.setResult(true);
            loginWrapper.setUser(userMapper.toDto(user));
        } else {
            successResult.setResult(false);
        }
        loginWrapper.setResult(successResult);
        return loginWrapper;
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
            }
        });

        // Set new profile image
        if (!Boolean.TRUE.equals(newProfileImage.getProfileImage())) {
            newProfileImage.setProfileImage(true);
            userImageRepo.save(newProfileImage);
        }
    }

    public UserImage getImage(String mail, Integer imageId) {
        User user = findByMail(mail);
        return userImageRepo.findByIdAndUser(imageId, user)
                .orElseThrow(() -> new ServiceException(ErrorType.IMAGE_NOT_FOUND, HttpStatus.NOT_FOUND));
    }

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
            return true;
        }

        return false;
    }

    @Transactional(readOnly = true)
    public List<MatchResponse> getMatches(String mail) {
        User user = findByMail(mail);
        List<UserMatch> matches = userMatchRepo.findAllMatchesForUser(user);

        return matches.stream().map(match -> {
            User otherUser = match.getUser1().getId().equals(user.getId()) ? match.getUser2() : match.getUser1();
            return new MatchResponse(match.getId(), userMapper.toDto(otherUser));
        }).collect(Collectors.toList());
    }

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

    public void dislikeUser(String dislikerMail, Integer dislikedUserId) {
        User disliker = findByMail(dislikerMail);
        User disliked = userRepo.findById(dislikedUserId)
                .orElseThrow(() -> new ServiceException(ErrorType.USER_NOT_FOUND, HttpStatus.NOT_FOUND));

        UserDislike dislike = new UserDislike(disliker, disliked);
        userDislikeRepo.save(dislike);
    }

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
                    return user.getSex().equalsIgnoreCase(prefs.getTargetGender());
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

    @Transactional
    public void setPremium(String mail, boolean isPremium) {
        User user = findByMail(mail);
        user.setPremium(isPremium);
        userRepo.save(user);
    }
}
