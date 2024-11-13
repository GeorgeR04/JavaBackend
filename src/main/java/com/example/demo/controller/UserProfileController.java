package com.example.demo.controller;

import com.example.demo.data.UserProfile;
import com.example.demo.service.UserProfileService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.demo.security.request.JwtUtil;
import java.util.Optional;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/profile")
public class UserProfileController {

    @Autowired
    private UserProfileService userProfileService;

    private JwtUtil jwtUtil;  // Autowire JwtUtil here

    @Autowired
    public UserProfileController(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    /**
     * Retrieves a user's profile by userId.
     */
//    @GetMapping("/{userId}")
//    public ResponseEntity<UserProfile> getUserProfileById(@PathVariable Long userId) {
//        Optional<UserProfile> userProfileOpt = userProfileService.getUserProfileByUserId(userId);
//        return userProfileOpt
//                .map(ResponseEntity::ok)
//                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));
//    }

    @GetMapping
    public ResponseEntity<UserProfile> getCurrentUserProfile(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        String token = authHeader.substring(7); // Remove "Bearer " prefix
        String username = jwtUtil.extractUsername(token);

        Optional<UserProfile> userProfileOpt = userProfileService.getUserProfileByUsername(username);
        return userProfileOpt
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));
    }

    /**
     * Updates a user's profile image.
     */
    @PatchMapping("/{userId}/profileImage")
    public ResponseEntity<UserProfile> updateProfileImage(@PathVariable Long userId, @RequestBody String newProfileImage) {
        Optional<UserProfile> updatedProfile = userProfileService.updateProfileImage(userId, newProfileImage);
        return updatedProfile.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Updates a user's banner image.
     */
    @PatchMapping("/{userId}/bannerImage")
    public ResponseEntity<UserProfile> updateBannerImage(@PathVariable Long userId, @RequestBody String newBannerImage) {
        Optional<UserProfile> updatedProfile = userProfileService.updateBannerImage(userId, newBannerImage);
        return updatedProfile.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Adds a new tournament image to the user's profile.
     */
    @PatchMapping("/{userId}/tournamentImage")
    public ResponseEntity<UserProfile> addTournamentImage(@PathVariable Long userId, @RequestBody String tournamentImage) {
        Optional<UserProfile> updatedProfile = userProfileService.addTournamentImage(userId, tournamentImage);
        return updatedProfile.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Adds a new friend to the user's profile.
     */
    @PatchMapping("/{userId}/friend")
    public ResponseEntity<UserProfile> addFriend(@PathVariable Long userId, @RequestBody String friendId) {
        Optional<UserProfile> updatedProfile = userProfileService.addFriend(userId, friendId);
        return updatedProfile.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Adds a new post to the user's profile.
     */
    @PatchMapping("/{userId}/post")
    public ResponseEntity<UserProfile> addPost(@PathVariable Long userId, @RequestBody String postId) {
        Optional<UserProfile> updatedProfile = userProfileService.addPost(userId, postId);
        return updatedProfile.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
