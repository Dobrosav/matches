package com.dobrosav.matches.api;

import com.dobrosav.matches.api.model.request.LoginRequest;
import com.dobrosav.matches.api.model.request.UserRequest;
import com.dobrosav.matches.api.model.request.ChatMessageRequest;
import com.dobrosav.matches.api.model.request.UserPreferencesRequest;
import com.dobrosav.matches.api.model.request.UserRequest;
import com.dobrosav.matches.api.model.response.LoginWrapper;
import com.dobrosav.matches.api.model.response.MatchResponse;
import com.dobrosav.matches.api.model.response.SuccessResult;

import com.dobrosav.matches.api.model.response.UserImageResponse;
import com.dobrosav.matches.api.model.response.UserResponse;
import com.dobrosav.matches.db.entities.ChatMessage;
import com.dobrosav.matches.db.entities.User;
import com.dobrosav.matches.db.entities.UserImage;
import com.dobrosav.matches.service.ChatService;
import com.dobrosav.matches.service.UserService;
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

@RestController
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    private final ChatService chatService;

    @Autowired
    public UserController(UserService userService, ChatService chatService) {
        this.userService = userService;
        this.chatService = chatService;
    }

    @RequestMapping(value = "matches/users", method = RequestMethod.POST)
    public UserResponse createDefaultUser(@RequestBody UserRequest request) throws Exception {
        long startTime = System.currentTimeMillis();

        log.info("request={}  createDefaultUser executed in {}ms", request, System.currentTimeMillis() - startTime);
        return userService.createDefaultUser(request);
    }

    @RequestMapping(value = "matches/users/{mail}", method = RequestMethod.GET)
    public UserResponse findByMail(@PathVariable("mail") String mail) {
        return userService.getUserByMail(mail);
    }

    @RequestMapping(value = "matches/users/{mail}", method = RequestMethod.DELETE)
    public SuccessResult delete(@PathVariable("mail") String mail) {
        SuccessResult successResult = new SuccessResult();
        userService.deleteUser(mail);
        successResult.setResult(true);
        return successResult;
    }

    @RequestMapping(value = "matches/login/users", method = RequestMethod.POST)
    public ResponseEntity<LoginWrapper> login(@RequestBody LoginRequest request) {
        long startTime = System.currentTimeMillis();
        LoginWrapper wrapper = userService.login(request);
        log.info("request={} result={} login executed in {}ms", request, wrapper, System.currentTimeMillis() - startTime);
        return new ResponseEntity(wrapper, HttpStatus.OK);
    }

    @RequestMapping(value = "matches/users", method = RequestMethod.GET)
    public ResponseEntity<List<UserResponse>> getUserByAge(@RequestParam(name = "beginYear") Integer begin, @RequestParam(name = "endYear", required = false) Integer end) throws Exception {
        long startTime = System.currentTimeMillis();
        List<UserResponse> users = userService.findByAge(begin, end);
        return new ResponseEntity(users, HttpStatus.OK);
    }

    @RequestMapping(value = "matches/users/{mail}/images", method = RequestMethod.POST)
    public ResponseEntity<UserImageResponse> uploadImage(@PathVariable("mail") String mail, @RequestParam("file") MultipartFile file) throws Exception {
        return new ResponseEntity<>(userService.uploadImage(mail, file), HttpStatus.CREATED);
    }

    @RequestMapping(value = "matches/users/{mail}/images", method = RequestMethod.GET)
    public ResponseEntity<List<UserImageResponse>> getUserImages(@PathVariable("mail") String mail) {
        return new ResponseEntity<>(userService.getUserImages(mail), HttpStatus.OK);
    }

    @RequestMapping(value = "matches/users/{mail}/images/{imageId}", method = RequestMethod.GET)
    public ResponseEntity<byte[]> getImage(@PathVariable("mail") String mail, @PathVariable("imageId") Integer imageId) {
        UserImage image = userService.getImage(mail, imageId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + image.getFileName() + "\"")
                .contentType(MediaType.parseMediaType(image.getContentType()))
                .body(image.getContent());
    }

    @RequestMapping(value = "matches/users/{mail}/images/{imageId}", method = RequestMethod.DELETE)
    public ResponseEntity<SuccessResult> deleteImage(@PathVariable("mail") String mail, @PathVariable("imageId") Integer imageId) {
        userService.deleteImage(mail, imageId);
        SuccessResult successResult = new SuccessResult();
        successResult.setResult(true);
        return new ResponseEntity<>(successResult, HttpStatus.OK);
    }

    @RequestMapping(value = "matches/users/{mail}/images/{imageId}/profile", method = RequestMethod.PUT)
    public ResponseEntity<SuccessResult> setProfileImage(@PathVariable("mail") String mail, @PathVariable("imageId") Integer imageId) {
        userService.setProfileImage(mail, imageId);
        SuccessResult successResult = new SuccessResult();
        successResult.setResult(true);
        return new ResponseEntity<>(successResult, HttpStatus.OK);
    }
    @RequestMapping(value = "matches/users/{mail}/like/{likedUserId}", method = RequestMethod.POST)
    public ResponseEntity<SuccessResult> likeUser(@PathVariable("mail") String mail, @PathVariable("likedUserId") Integer likedUserId) {
        boolean match = userService.likeUser(mail, likedUserId);
        SuccessResult successResult = new SuccessResult();
        successResult.setResult(match); // Returning true if it's a match, false otherwise. Or true just to indicate success?
        // Let's interpret 'result' as "operation successful" (which is always true if no exception),
        // or we could add a specific message/field for "isMatch".
        // Given SuccessResult has only "Boolean result", I'll use it to indicate if the operation succeeded (which is implicit if 200 OK)
        // or maybe I should repurpose it?
        // Let's stick to standard practice: 200 OK means success.
        // If the user wants to know if it was a match, maybe I should return a different object.
        // But for now, let's just return true.
        successResult.setResult(match);
        return new ResponseEntity<>(successResult, HttpStatus.OK);
    }

    @RequestMapping(value = "matches/users/{mail}/matches", method = RequestMethod.GET)
    public ResponseEntity<List<MatchResponse>> getMatches(@PathVariable("mail") String mail) {
        return new ResponseEntity<>(userService.getMatches(mail), HttpStatus.OK);
    }

    @RequestMapping(value = "matches/users/{mail}/preferences", method = RequestMethod.PUT)
    public ResponseEntity<UserResponse> updatePreferences(@PathVariable("mail") String mail, @RequestBody UserPreferencesRequest request) {
        return new ResponseEntity<>(userService.updatePreferences(mail, request), HttpStatus.OK);
    }

    @RequestMapping(value = "matches/users/{mail}/dislike/{dislikedUserId}", method = RequestMethod.POST)
    public ResponseEntity<SuccessResult> dislikeUser(@PathVariable("mail") String mail, @PathVariable("dislikedUserId") Integer dislikedUserId) {
        userService.dislikeUser(mail, dislikedUserId);
        SuccessResult successResult = new SuccessResult();
        successResult.setResult(true);
        return new ResponseEntity<>(successResult, HttpStatus.OK);
    }

    @RequestMapping(value = "matches/users/{mail}/feed", method = RequestMethod.GET)
    public ResponseEntity<List<UserResponse>> getFeed(@PathVariable("mail") String mail) {
        return new ResponseEntity<>(userService.getFeed(mail), HttpStatus.OK);
    }

    @RequestMapping(value = "matches/matches/{matchId}/messages", method = RequestMethod.POST)
    public ResponseEntity<ChatMessage> sendChatMessage(@PathVariable Integer matchId, @RequestBody ChatMessageRequest request) {
        return new ResponseEntity<>(chatService.saveMessage(matchId, request.getSenderId(), request.getContent()), HttpStatus.OK);
    }

    @RequestMapping(value = "matches/matches/{matchId}/messages", method = RequestMethod.GET)
    public ResponseEntity<List<ChatMessage>> getChatHistory(@PathVariable Integer matchId) {
        return new ResponseEntity<>(chatService.getChatHistory(matchId), HttpStatus.OK);
    }

    @RequestMapping(value = "matches/users/{mail}/search", method = RequestMethod.GET)
    public ResponseEntity<List<UserResponse>> searchUsers(@PathVariable("mail") String mail) {
        return new ResponseEntity<>(userService.searchUsers(mail), HttpStatus.OK);
    }

    @RequestMapping(value = "matches/users/{mail}/likers", method = RequestMethod.GET)
    public ResponseEntity<List<UserResponse>> getLikers(@PathVariable("mail") String mail) {
        return new ResponseEntity<>(userService.getLikers(mail), HttpStatus.OK);
    }

    @RequestMapping(value = "matches/users/{mail}/premium", method = RequestMethod.PUT)
    public ResponseEntity<SuccessResult> setPremium(@PathVariable("mail") String mail, @RequestParam boolean isPremium) {
        userService.setPremium(mail, isPremium);
        SuccessResult successResult = new SuccessResult();
        successResult.setResult(true);
        return new ResponseEntity<>(successResult, HttpStatus.OK);
    }
}
