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
        // Set authKey and encode password
        user.setAuthKey(2001);
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
}
