package com.example.demo.service;

import com.example.demo.data.UserProfile;
import com.example.demo.repository.mongoDB.UserProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserProfileService {

    @Autowired
    private UserProfileRepository userProfileRepository;

    public Optional<UserProfile> getUserProfileByUsername(String username) {
        return userProfileRepository.findByUsername(username);
    }

    public Optional<UserProfile> getUserProfileByUserId(Long userId) {
        return userProfileRepository.findByUserId(userId);
    }
    public Optional<UserProfile> updateProfileImage(Long userId, String newProfileImage) {
        Optional<UserProfile> userProfileOpt = userProfileRepository.findByUserId(userId);
        userProfileOpt.ifPresent(userProfile -> {
            userProfile.setProfileImage(newProfileImage);
            userProfileRepository.save(userProfile);
        });
        return userProfileOpt;
    }

    public Optional<UserProfile> updateBannerImage(Long userId, String newBannerImage) {
        Optional<UserProfile> userProfileOpt = userProfileRepository.findByUserId(userId);
        userProfileOpt.ifPresent(userProfile -> {
            userProfile.setBannerImage(newBannerImage);
            userProfileRepository.save(userProfile);
        });
        return userProfileOpt;
    }

    public Optional<UserProfile> addTournamentImage(Long userId, String tournamentImage) {
        Optional<UserProfile> userProfileOpt = userProfileRepository.findByUserId(userId);
        userProfileOpt.ifPresent(userProfile -> {
            userProfile.getTournamentImages().add(tournamentImage);
            userProfileRepository.save(userProfile);
        });
        return userProfileOpt;
    }

    public Optional<UserProfile> addFriend(Long userId, String friendId) {
        Optional<UserProfile> userProfileOpt = userProfileRepository.findByUserId(userId);
        userProfileOpt.ifPresent(userProfile -> {
            if (!userProfile.getFriendIds().contains(friendId)) {
                userProfile.getFriendIds().add(friendId);
                userProfileRepository.save(userProfile);
            }
        });
        return userProfileOpt;
    }

    public Optional<UserProfile> addPost(Long userId, String postId) {
        Optional<UserProfile> userProfileOpt = userProfileRepository.findByUserId(userId);
        userProfileOpt.ifPresent(userProfile -> {
            userProfile.getPostIds().add(postId);
            userProfileRepository.save(userProfile);
        });
        return userProfileOpt;
    }

}
