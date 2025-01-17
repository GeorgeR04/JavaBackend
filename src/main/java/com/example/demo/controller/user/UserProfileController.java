package com.example.demo.controller.user;

import com.example.demo.data.user.UserProfile;
import com.example.demo.service.user.UserProfileService;
import com.example.demo.security.request.JwtUtil;
import com.example.demo.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    // Centralized helper for authorization validation
    private Optional<String> validateAndExtractUsername(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Optional.empty();
        }
        return Optional.of(jwtUtil.extractUsername(authHeader.substring(7)));
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<Map<String, Object>> getUserProfileByUsername(@PathVariable String username, HttpServletRequest request) {
        Optional<String> tokenUsername = validateAndExtractUsername(request);
        if (tokenUsername.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Authorization header is missing or invalid."));
        }

        if (!username.equals(tokenUsername.get())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "You do not have permission to access this profile."));
        }

        return userProfileService.getUserProfileByUsername(username)
                .map(userProfile -> ResponseEntity.ok(prepareUserProfileResponse(userProfile)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User profile not found.")));
    }


    @PostMapping("/upload-image")
    public ResponseEntity<UserProfile> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") String type,
            HttpServletRequest request) {

        Optional<String> usernameOpt = validateAndExtractUsername(request);
        if (usernameOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            Optional<UserProfile> userProfileOpt = userProfileService.getUserProfileByUsername(usernameOpt.get());
            if (userProfileOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            if ("profile".equalsIgnoreCase(type)) {
                userProfileService.updateProfileImage(usernameOpt.get(), file);
            } else if ("banner".equalsIgnoreCase(type)) {
                userProfileService.updateBannerImage(usernameOpt.get(), file);
            } else {
                return ResponseEntity.badRequest().build();
            }
            return ResponseEntity.ok(userProfileOpt.get());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @PostMapping("/assign-role")
    public ResponseEntity<UserProfile> assignRole(@RequestParam int authKey, HttpServletRequest request) {
        Optional<String> usernameOpt = validateAndExtractUsername(request);
        if (usernameOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return userProfileService.getUserProfileByUsername(usernameOpt.get())
                .map(userProfile -> ResponseEntity.ok(userProfileService.initializeUserProfileWithRole(userProfile, authKey)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping("/set-player-details")
    public ResponseEntity<?> setPlayerDetails(@RequestBody Map<String, Object> payload, HttpServletRequest request) {
        Optional<String> usernameOpt = validateAndExtractUsername(request);
        if (usernameOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String role = (String) payload.get("role");
        String specialization = (String) payload.get("specialization");
        String game = (String) payload.get("game");

        try {
            Optional<UserProfile> updatedProfile = userProfileService.updatePlayerDetails(usernameOpt.get(), role, specialization, game);
            if (updatedProfile.isPresent()) {
                userService.synchronizeRoleToMySQL(usernameOpt.get());
                return ResponseEntity.ok(updatedProfile.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid data: " + e.getMessage());
        }
    }

    @PostMapping("/sync-role")
    public ResponseEntity<?> synchronizeRoleToMySQL(HttpServletRequest request) {
        Optional<String> usernameOpt = validateAndExtractUsername(request);
        if (usernameOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorization header is missing or invalid.");
        }

        try {
            userService.synchronizeRoleToMySQL(usernameOpt.get());
            return ResponseEntity.ok("Role synchronized successfully.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to synchronize role: " + e.getMessage());
        }
    }

    private Map<String, Object> prepareUserProfileResponse(UserProfile userProfile) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", userProfile.getId());
        response.put("username", userProfile.getUsername());
        response.put("firstname", userProfile.getFirstname());
        response.put("lastname", userProfile.getLastname());
        response.put("profileImage", encodeImage(userProfile.getProfileImage()));
        response.put("bannerImage", encodeImage(userProfile.getBannerImage()));
        response.put("tournamentImages", userProfile.getTournamentImages());
        response.put("role", userProfile.getRole());
        response.put("specialization", userProfile.getSpecialization());
        response.put("game", userProfile.getGame());
        return response;
    }

    private String encodeImage(byte[] image) {
        return image != null ? Base64.getEncoder().encodeToString(image) : null;
    }
}
