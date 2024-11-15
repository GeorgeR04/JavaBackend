package com.example.demo.service;

import com.example.demo.data.User;
import com.example.demo.data.UserProfile;
import com.example.demo.repository.mongoDB.UserProfileRepository;
import com.example.demo.repository.mySql.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.HashMap;

import java.util.Optional;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User registerUser(User user, UserProfile userProfile) {
        // Set default authKey and encode password
        user.setAuthKey(2001); // Default to "member"
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Save user in MySQL
        User savedUser = userRepository.save(user);
        logger.info("User saved in MySQL with ID: {}", savedUser.getId());

        // Populate UserProfile details
        userProfile.setUserId(savedUser.getId());
        userProfile.setUsername(savedUser.getUsername());
        userProfile.setFirstname(savedUser.getFirstname());
        userProfile.setLastname(savedUser.getLastname());

        // Attempt to save UserProfile in MongoDB
        try {
            userProfileRepository.save(userProfile);
            logger.info("UserProfile saved in MongoDB for user ID: {}", savedUser.getId());
        } catch (Exception e) {
            logger.error("Failed to save UserProfile in MongoDB", e);
        }

        return savedUser;
    }

    public boolean isEmailOrUsernameInUse(User user) {
        return userRepository.findByEmail(user.getEmail()).isPresent() ||
                userRepository.findByUsername(user.getUsername()).isPresent();
    }

    public void synchronizeRoleToMySQL(String username) {
        logger.info("Starting role synchronization for username: {}", username);

        Optional<UserProfile> userProfileOpt = userProfileRepository.findByUsername(username);

        if (userProfileOpt.isPresent()) {
            UserProfile userProfile = userProfileOpt.get();
            String role = userProfile.getRole();
            logger.info("Fetched role '{}' from MongoDB for username: {}", role, username);

            int authKey = convertRoleToAuthKey(role);
            if (authKey == -1) {
                logger.error("Invalid role '{}' found for user: {}", role, username);
                return;
            }

            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                user.setAuthKey(authKey);
                userRepository.save(user);
                logger.info("Updated MySQL user authKey to: {}", authKey);
            } else {
                logger.error("MySQL user not found for MongoDB username: {}", username);
            }
        } else {
            logger.error("User profile not found in MongoDB for username: {}", username);
        }
    }

    private static final Map<String, Integer> ROLE_AUTH_KEY_MAP = new HashMap<>() {{
        put("member", 2001);
        put("player", 2002);
        put("organizer", 2003);
    }};

    private int convertRoleToAuthKey(String role) {
        return ROLE_AUTH_KEY_MAP.getOrDefault(role, -1); // -1 for undefined roles
    }


}
