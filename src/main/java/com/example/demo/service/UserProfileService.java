package com.example.demo.service;

import com.example.demo.data.UserProfile;
import com.example.demo.repository.mongoDB.UserProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@Service
public class UserProfileService {

    @Autowired
    private UserProfileRepository userProfileRepository;

    public Optional<UserProfile> getUserProfileByUsername(String username) {
        return userProfileRepository.findByUsername(username);
    }

    public Optional<UserProfile> updateProfileImage(String username, MultipartFile profileImage) {
        return userProfileRepository.findByUsername(username).map(userProfile -> {
            try {
                userProfile.setProfileImage(profileImage.getBytes());
                userProfileRepository.save(userProfile);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
            return userProfile;
        });
    }

    public Optional<UserProfile> updateBannerImage(String username, MultipartFile bannerImage) {
        return userProfileRepository.findByUsername(username).map(userProfile -> {
            try {
                userProfile.setBannerImage(bannerImage.getBytes());
                userProfileRepository.save(userProfile);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
            return userProfile;
        });
    }

    public Optional<UserProfile> addTournamentImage(String username, String tournamentImage) {
        Optional<UserProfile> userProfileOpt = userProfileRepository.findByUsername(username);
        userProfileOpt.ifPresent(userProfile -> {
            userProfile.getTournamentImages().add(tournamentImage);
            userProfileRepository.save(userProfile);
        });
        return userProfileOpt;
    }

    public Optional<UserProfile> addFriend(String username, String friendUsername) {
        Optional<UserProfile> userProfileOpt = userProfileRepository.findByUsername(username);
        userProfileOpt.ifPresent(userProfile -> {
            if (!userProfile.getFriendIds().contains(friendUsername)) {
                userProfile.getFriendIds().add(friendUsername);
                userProfileRepository.save(userProfile);
            }
        });
        return userProfileOpt;
    }

    public Optional<UserProfile> addPost(String username, String postId) {
        Optional<UserProfile> userProfileOpt = userProfileRepository.findByUsername(username);
        userProfileOpt.ifPresent(userProfile -> {
            userProfile.getPostIds().add(postId);
            userProfileRepository.save(userProfile);
        });
        return userProfileOpt;
    }
}
