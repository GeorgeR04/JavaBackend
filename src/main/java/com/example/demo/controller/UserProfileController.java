package com.example.demo.controller;

import com.example.demo.data.UserProfile;
import com.example.demo.service.UserProfileService;
import com.example.demo.security.request.JwtUtil;
import com.example.demo.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@RestController
@RequestMapping("/api/profile")
public class UserProfileController {

    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    private UserService userService;


    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/username/{username}")
    public ResponseEntity<?> getUserProfileByUsername(@PathVariable String username, HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorization header is missing or invalid.");
        }

        String token = authHeader.substring(7);
        String tokenUsername = jwtUtil.extractUsername(token);

        if (!username.equals(tokenUsername)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not have permission to access this profile.");
        }

        Optional<UserProfile> userProfileOpt = userProfileService.getUserProfileByUsername(username);
        if (userProfileOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User profile not found.");
        }

        UserProfile userProfile = userProfileOpt.get();
        Map<String, Object> response = new HashMap<>();
        response.put("id", userProfile.getId());
        response.put("username", userProfile.getUsername());
        response.put("firstname", userProfile.getFirstname());
        response.put("lastname", userProfile.getLastname());
        response.put("profileImage", userProfile.getProfileImage() != null ?
                Base64.getEncoder().encodeToString(userProfile.getProfileImage()) : null);
        response.put("bannerImage", userProfile.getBannerImage() != null ?
                Base64.getEncoder().encodeToString(userProfile.getBannerImage()) : null);
        response.put("tournamentImages", userProfile.getTournamentImages());
        response.put("role", userProfile.getRole());
        response.put("specialization", userProfile.getSpecialization());
        response.put("game", userProfile.getGame());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/upload-image")
    public ResponseEntity<UserProfile> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") String type,
            HttpServletRequest request) {

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token = authHeader.substring(7);
        String username = jwtUtil.extractUsername(token);

        Optional<UserProfile> userProfileOpt = userProfileService.getUserProfileByUsername(username);
        if (userProfileOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        UserProfile userProfile = userProfileOpt.get();
        try {
            if ("profile".equals(type)) {
                userProfileService.updateProfileImage(userProfile.getUsername(), file);
            } else if ("banner".equals(type)) {
                userProfileService.updateBannerImage(userProfile.getUsername(), file);
            }
            return ResponseEntity.ok(userProfile);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/assign-role")
    public ResponseEntity<UserProfile> assignRole(
            @RequestParam int authKey,
            HttpServletRequest request) {

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token = authHeader.substring(7);
        String username = jwtUtil.extractUsername(token);

        Optional<UserProfile> userProfileOpt = userProfileService.getUserProfileByUsername(username);
        if (userProfileOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        UserProfile updatedProfile = userProfileService.initializeUserProfileWithRole(userProfileOpt.get(), authKey);
        return ResponseEntity.ok(updatedProfile);
    }

    @PostMapping("/set-player-details")
    public ResponseEntity<UserProfile> setPlayerDetails(
            @RequestBody Map<String, Object> payload,
            HttpServletRequest request) {

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token = authHeader.substring(7);
        String username = jwtUtil.extractUsername(token);

        // Extracting new role, specialization, and game from payload
        String role = (String) payload.get("role");
        String specialization = (String) payload.get("specialization");
        String game = (String) payload.get("game");

        try {
            // Update player details in MongoDB
            Optional<UserProfile> updatedProfile = userProfileService.updatePlayerDetails(username, role, specialization, game);

            if (updatedProfile.isPresent()) {
                // Synchronize role to MySQL after MongoDB update
                userService.synchronizeRoleToMySQL(username);
                return ResponseEntity.ok(updatedProfile.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping("/sync-role")
    public ResponseEntity<?> synchronizeRoleToMySQL(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorization header is missing or invalid.");
        }

        String token = authHeader.substring(7);
        String username = jwtUtil.extractUsername(token);

        try {
            userService.synchronizeRoleToMySQL(username);
            return ResponseEntity.ok("Role synchronized successfully.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to synchronize role: " + e.getMessage());
        }
    }

}