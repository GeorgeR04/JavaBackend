package com.example.demo.service.user;

import com.example.demo.data.user.User;
import com.example.demo.data.user.UserProfile;
import com.example.demo.repository.mongoDB.user.UserProfilesRepository;
import com.example.demo.repository.mySql.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final UserProfilesRepository userProfilesRepository;
    private final PasswordEncoder passwordEncoder;

    private static final Map<String, Integer> ROLE_AUTH_KEY_MAP = Map.of(
            "member", 2001,
            "player", 2002,
            "organizer", 2003
    );

    public UserService(UserRepository userRepository,
                       UserProfilesRepository userProfilesRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userProfilesRepository = userProfilesRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerUser(User user, UserProfile userProfile) {
        user.setAuthKey(ROLE_AUTH_KEY_MAP.get("member"));
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        User savedUser = userRepository.save(user);
        logger.info("User saved in MySQL with ID: {}", savedUser.getId());

        populateUserProfile(userProfile, savedUser);
        saveUserProfile(userProfile, savedUser.getId());

        return savedUser;
    }

    private void populateUserProfile(UserProfile userProfile, User savedUser) {
        userProfile.setUserId(savedUser.getId());
        userProfile.setUsername(savedUser.getUsername());
        userProfile.setFirstname(savedUser.getFirstname());
        userProfile.setLastname(savedUser.getLastname());
    }

    private void saveUserProfile(UserProfile userProfile, Long userId) {
        try {
            userProfilesRepository.save(userProfile);
            logger.info("UserProfile saved in MongoDB for user ID: {}", userId);
        } catch (Exception e) {
            logger.error("Failed to save UserProfile in MongoDB for user ID: {}", userId, e);
        }
    }

    public boolean isEmailOrUsernameInUse(User user) {
        return userRepository.findByEmail(user.getEmail()).isPresent() ||
                userRepository.findByUsername(user.getUsername()).isPresent();
    }

    public void synchronizeRoleToMySQL(String username) {
        logger.info("Starting role synchronization for username: {}", username);

        userProfilesRepository.findByUsername(username).ifPresentOrElse(
                userProfile -> synchronizeUserRole(username, userProfile),
                () -> logger.error("User profile not found in MongoDB for username: {}", username)
        );
    }

    private void synchronizeUserRole(String username, UserProfile userProfile) {
        String role = userProfile.getRole();
        logger.info("Fetched role '{}' from MongoDB for username: {}", role, username);

        int authKey = convertRoleToAuthKey(role);
        if (authKey == -1) {
            logger.error("Invalid role '{}' found for user: {}", role, username);
            return;
        }

        userRepository.findByUsername(username).ifPresentOrElse(
                user -> {
                    user.setAuthKey(authKey);
                    userRepository.save(user);
                    logger.info("Updated MySQL user authKey to: {}", authKey);
                },
                () -> logger.error("MySQL user not found for MongoDB username: {}", username)
        );
    }

    private int convertRoleToAuthKey(String role) {
        return ROLE_AUTH_KEY_MAP.getOrDefault(role, -1);
    }
}
