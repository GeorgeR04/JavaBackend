package com.example.demo.controller;

import com.example.demo.data.User;
import com.example.demo.data.UserProfile;
import com.example.demo.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class RegisterController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody User user) {
        try {
            // Validation check
            String validationError = validateUserInput(user);
            if (validationError != null) {
                return new ResponseEntity<>(validationError, HttpStatus.BAD_REQUEST);
            }

            // Check if email or username is already in use
            if (userService.isEmailOrUsernameInUse(user)) {
                return new ResponseEntity<>("Error: Email or Username is already in use.", HttpStatus.BAD_REQUEST);
            }

            // Initialize the UserProfile
            UserProfile userProfile = new UserProfile();
            userProfile.setProfileImage(""); // Optional fields, can set a default if needed
            userProfile.setBannerImage("");

            // Register User (saves in both MySQL and MongoDB)
            userService.registerUser(user, userProfile);

            log.info("User registered successfully: {}", user.getUsername());
            return new ResponseEntity<>("User registered successfully!", HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error registering user: ", e);
            return new ResponseEntity<>("Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private String validateUserInput(User user) {
        if (user.getUsername() == null || user.getUsername().length() < 4 || user.getUsername().length() > 20) {
            return "Error: Username must be between 4 and 20 characters.";
        }
        if (user.getFirstname() == null || user.getFirstname().isEmpty()) {
            return "Error: First name is required.";
        }
        if (user.getLastname() == null || user.getLastname().isEmpty()) {
            return "Error: Last name is required.";
        }
        if (user.getEmail() == null || !user.getEmail().matches("\\S+@\\S+\\.\\S+")) {
            return "Error: Invalid email address.";
        }
        if (user.getPassword() == null || user.getPassword().length() < 8) {
            return "Error: Password must be at least 8 characters long.";
        }
        return null;
    }
}
