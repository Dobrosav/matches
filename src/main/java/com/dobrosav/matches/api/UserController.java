package com.dobrosav.matches.api;

import com.dobrosav.matches.api.model.request.LoginRequest;
import com.dobrosav.matches.api.model.request.UserRequest;
import com.dobrosav.matches.api.model.request.ChatMessageRequest;
import com.dobrosav.matches.api.model.request.UserPreferencesRequest;
import com.dobrosav.matches.api.model.response.MatchResponse;
import com.dobrosav.matches.api.model.response.SuccessResult;

import com.dobrosav.matches.api.model.response.UserImageResponse;
import com.dobrosav.matches.api.model.response.UserResponse;
import com.dobrosav.matches.api.model.response.ChatMessageResponse;
import com.dobrosav.matches.db.entities.UserImage;

import com.dobrosav.matches.security.AuthenticationResponse;
import com.dobrosav.matches.security.AuthenticationService;
import com.dobrosav.matches.service.ChatService;
import com.dobrosav.matches.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    private final ChatService chatService;
    private final AuthenticationService authenticationService;

    @Autowired
    public UserController(UserService userService, ChatService chatService, AuthenticationService authenticationService) {
        this.userService = userService;
        this.chatService = chatService;
        this.authenticationService = authenticationService;
    }
    
    @PostMapping("/auth/register")
    public ResponseEntity<AuthenticationResponse> register(@Valid @RequestBody UserRequest request) {
        return ResponseEntity.ok(authenticationService.register(request));
    }

    @PostMapping("/auth/login")
    public ResponseEntity<AuthenticationResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }
    
    @PostMapping("/auth/refresh-token")
    public ResponseEntity<AuthenticationResponse> refreshToken(HttpServletRequest request) {
        return ResponseEntity.ok(authenticationService.refreshToken(request));
    }
    
    @GetMapping("/users/{email}")
    public UserResponse findByMail(@PathVariable("email") String email) {
        return userService.getUserByMail(email);
    }

    @DeleteMapping("/users/{email}")
    public SuccessResult delete(@PathVariable("email") String email) {
        SuccessResult successResult = new SuccessResult();
        userService.deleteUser(email);
        successResult.setResult(true);
        return successResult;
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getUserByAge(@RequestParam(name = "beginYear") Integer begin, @RequestParam(name = "endYear", required = false) Integer end) throws Exception {
        List<UserResponse> users = userService.findByAge(begin, end);
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @PostMapping("/users/{email}/images")
    public ResponseEntity<UserImageResponse> uploadImage(@PathVariable("email") String email, @RequestParam("file") MultipartFile file) throws Exception {
        return new ResponseEntity<>(userService.uploadImage(email, file), HttpStatus.CREATED);
    }

    @GetMapping("/users/{email}/images")
    public ResponseEntity<List<UserImageResponse>> getUserImages(@PathVariable("email") String email) {
        return new ResponseEntity<>(userService.getUserImages(email), HttpStatus.OK);
    }

    @GetMapping("/users/{email}/images/{imageId}")
    public ResponseEntity<byte[]> getImage(@PathVariable("email") String email, @PathVariable("imageId") Integer imageId) {
        UserImage image = userService.getImage(email, imageId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + image.getFileName() + "\"")
                .contentType(MediaType.parseMediaType(image.getContentType()))
                .body(image.getContent());
    }

    @DeleteMapping("/users/{email}/images/{imageId}")
    public ResponseEntity<SuccessResult> deleteImage(@PathVariable("email") String email, @PathVariable("imageId") Integer imageId) {
        userService.deleteImage(email, imageId);
        SuccessResult successResult = new SuccessResult();
        successResult.setResult(true);
        return new ResponseEntity<>(successResult, HttpStatus.OK);
    }

    @PutMapping("/users/{email}/images/{imageId}/profile")
    public ResponseEntity<SuccessResult> setProfileImage(@PathVariable("email") String email, @PathVariable("imageId") Integer imageId) {
        userService.setProfileImage(email, imageId);
        SuccessResult successResult = new SuccessResult();
        successResult.setResult(true);
        return new ResponseEntity<>(successResult, HttpStatus.OK);
    }
    
    @PostMapping("/users/{email}/like/{likedUserId}")
    public ResponseEntity<SuccessResult> likeUser(@PathVariable("email") String email, @PathVariable("likedUserId") Integer likedUserId) {
        boolean match = userService.likeUser(email, likedUserId);
        SuccessResult successResult = new SuccessResult();
        successResult.setResult(match);
        return new ResponseEntity<>(successResult, HttpStatus.OK);
    }

    @GetMapping("/users/{email}/matches")
    public ResponseEntity<List<MatchResponse>> getMatches(@PathVariable("email") String email) {
        return new ResponseEntity<>(userService.getMatches(email), HttpStatus.OK);
    }

    @PutMapping("/users/{email}/preferences")
    public ResponseEntity<UserResponse> updatePreferences(@PathVariable("email") String email, @RequestBody UserPreferencesRequest request) {
        return new ResponseEntity<>(userService.updatePreferences(email, request), HttpStatus.OK);
    }

    @PostMapping("/users/{email}/dislike/{dislikedUserId}")
    public ResponseEntity<SuccessResult> dislikeUser(@PathVariable("email") String email, @PathVariable("dislikedUserId") Integer dislikedUserId) {
        userService.dislikeUser(email, dislikedUserId);
        SuccessResult successResult = new SuccessResult();
        successResult.setResult(true);
        return new ResponseEntity<>(successResult, HttpStatus.OK);
    }

    @GetMapping("/users/{email}/feed")
    public ResponseEntity<List<UserResponse>> getFeed(@PathVariable("email") String email) {
        return new ResponseEntity<>(userService.getFeed(email), HttpStatus.OK);
    }

    @GetMapping("/users/{email}/filtered-feed")
    public ResponseEntity<List<UserResponse>> getFilteredFeed(
            @PathVariable("email") String email,
            @RequestParam(name = "gender", required = false) String gender,
            @RequestParam(name = "minAge", required = false) Integer minAge,
            @RequestParam(name = "maxAge", required = false) Integer maxAge
    ) {
        List<UserResponse> users = userService.getFilteredFeed(email, gender, minAge, maxAge);
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @PostMapping("/matches/{matchId}/messages")
    public ResponseEntity<ChatMessageResponse> sendChatMessage(@PathVariable Integer matchId, @RequestBody ChatMessageRequest request) {
        return new ResponseEntity<>(chatService.saveMessage(matchId, request.getSenderId(), request.getContent()), HttpStatus.OK);
    }

    @GetMapping("/matches/{matchId}/messages")
    public ResponseEntity<List<ChatMessageResponse>> getChatHistory(@PathVariable Integer matchId) {
        return new ResponseEntity<>(chatService.getChatHistory(matchId), HttpStatus.OK);
    }

    @GetMapping("/users/{email}/search")
    public ResponseEntity<List<UserResponse>> searchUsers(@PathVariable("email") String email) {
        return new ResponseEntity<>(userService.searchUsers(email), HttpStatus.OK);
    }

    @GetMapping("/users/{email}/likers")
    public ResponseEntity<List<UserResponse>> getLikers(@PathVariable("email") String email) {
        return new ResponseEntity<>(userService.getLikers(email), HttpStatus.OK);
    }

    @PutMapping("/users/{email}/premium")
    public ResponseEntity<SuccessResult> setPremium(@PathVariable("email") String email, @RequestParam boolean isPremium) {
        userService.setPremium(email, isPremium);
        SuccessResult successResult = new SuccessResult();
        successResult.setResult(true);
        return new ResponseEntity<>(successResult, HttpStatus.OK);
    }
}
