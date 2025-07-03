package org.example.expert.domain.user.controller;

import lombok.RequiredArgsConstructor;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.user.dto.request.UserChangePasswordRequest;
import org.example.expert.domain.user.dto.response.UserProfileChangeResponse;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/users/{userId}")
    public ResponseEntity<UserResponse> getUser(@PathVariable long userId) {
        return ResponseEntity.ok(userService.getUser(userId));
    }

    @PutMapping("/users")
    public void changePassword(@AuthenticationPrincipal AuthUser authUser, @RequestBody UserChangePasswordRequest userChangePasswordRequest) {
        userService.changePassword(authUser.getId(), userChangePasswordRequest);
    }

    @PostMapping("/users/profile")
    public ResponseEntity<UserProfileChangeResponse> updateProfileImage(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam(value = "file", required = false)MultipartFile file){
        return ResponseEntity.ok(userService.updateProfileImage(authUser, file));
    }

    @DeleteMapping("/users/profile")
    public ResponseEntity<UserProfileChangeResponse> deleteProfileImage(
            @AuthenticationPrincipal AuthUser authUser) {
        return ResponseEntity.ok(userService.deleteProfileImage(authUser));
    }
}
