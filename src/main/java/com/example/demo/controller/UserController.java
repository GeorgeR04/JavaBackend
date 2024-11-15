package com.example.demo.controller;

import com.example.demo.data.User;
import com.example.demo.data.UserProfile;
import com.example.demo.repository.mongoDB.UserProfileRepository;
import com.example.demo.repository.mySql.UserRepository;
import com.example.demo.security.request.JwtUtil;
import com.example.demo.service.UserProfileService;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    @Autowired
    private UserProfileService userProfileService;

    // New registration method to save data in both MySQL and MongoDB
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user, @RequestBody UserProfile userProfile) {
        try {
            User createdUser = userService.registerUser(user, userProfile);
            return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>("User registration failed.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userRepository.findAll();
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            return new ResponseEntity<>(user.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Updated getUserByUsername with authorization check
    @GetMapping("/user/{username}")
    public ResponseEntity<?> getUserByUsername(@PathVariable String username, HttpServletRequest request) {
        // Extract the username from the JWT in the Authorization header
        String token = request.getHeader("Authorization").substring(7);
        String authenticatedUsername = jwtUtil.extractUsername(token);

        // Check if the username in the token matches the requested username
        if (!authenticatedUsername.equals(username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        }

        // Fetch and return the user data if the authenticated user matches the requested username
        Optional<User> optionalUser = userRepository.findByUsername(username);
        return optionalUser
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found"));
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        try {
            User newUser = userRepository.save(user);
            return new ResponseEntity<>(newUser, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User updatedUser) {
        Optional<User> userData = userRepository.findById(id);
        if (userData.isPresent()) {
            User existingUser = userData.get();
            existingUser.setUsername(updatedUser.getUsername());
            existingUser.setFirstname(updatedUser.getFirstname());
            existingUser.setLastname(updatedUser.getLastname());
            existingUser.setPassword(updatedUser.getPassword());
            existingUser.setEmail(updatedUser.getEmail());
            existingUser.setAuthKey(updatedUser.getAuthKey());

            User savedUser = userRepository.save(existingUser);
            return new ResponseEntity<>(savedUser, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteUser(@PathVariable Long id) {
        try {
            userRepository.deleteById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping
    public ResponseEntity<HttpStatus> deleteAllUsers() {
        try {
            userRepository.deleteAll();
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/update-role/{username}")
    public ResponseEntity<?> updateUserRole(@PathVariable String username, @RequestBody String newRole) {
        // Check if the new role is valid
        if (newRole == null || newRole.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Role cannot be null or empty");
        }

        // Use UserProfileService to update the role
        Optional<UserProfile> updatedProfile = userProfileService.updateUserProfileRole(username, newRole);

        if (updatedProfile.isPresent()) {
            return ResponseEntity.ok(updatedProfile.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User profile not found");
        }
    }
}
