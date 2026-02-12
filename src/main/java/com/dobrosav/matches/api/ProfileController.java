package com.dobrosav.matches.api;

import com.dobrosav.matches.api.model.request.ProfileUpdateRequest;
import com.dobrosav.matches.api.model.response.UserResponse;
import com.dobrosav.matches.db.entities.User;
import com.dobrosav.matches.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/profile")
public class ProfileController {

    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyProfile(@AuthenticationPrincipal User currentUser) {
        User user = userService.findById(currentUser.getId());
        return ResponseEntity.ok(new UserResponse(user));
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateMyProfile(@AuthenticationPrincipal User currentUser, @RequestBody ProfileUpdateRequest request) {
        User updatedUser = userService.updateUserProfile(currentUser.getId(), request);
        return ResponseEntity.ok(new UserResponse(updatedUser));
    }
}
