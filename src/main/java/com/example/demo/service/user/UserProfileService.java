package com.example.demo.service.user;

import com.example.demo.data.user.UserProfile;
import com.example.demo.repository.mongoDB.user.UserProfilesRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@Service
public class UserProfileService {

    private final UserService userService;
    private final UserProfilesRepository userProfilesRepository;

    public UserProfileService(UserService userService, UserProfilesRepository userProfilesRepository) {
        this.userService = userService;
        this.userProfilesRepository = userProfilesRepository;
    }

    public Optional<UserProfile> getUserProfileByUsername(String username) {
        return userProfilesRepository.findByUsername(username);
    }

    public UserProfile initializeUserProfileWithRole(UserProfile userProfile, int authKey) {
        String role = determineRoleByAuthKey(authKey);
        userProfile.setRole(role);

        if ("player".equals(role)) {
            userProfile.setGame(null);
        }

        return userProfilesRepository.save(userProfile);
    }

    public Optional<UserProfile> updatePlayerDetails(String username, String role, String specialization, String game) {
        validateRole(role);

        return userProfilesRepository.findByUsername(username).map(userProfile -> {
            userProfile.setRole(role);

            if ("player".equals(role)) {
                userProfile.setSpecialization(specialization);
                userProfile.setGame(game);
            } else {
                clearPlayerFields(userProfile);
            }

            return userProfilesRepository.save(userProfile);
        });
    }

    public Optional<UserProfile> updateProfileImage(String username, MultipartFile profileImage) {
        return updateImage(username, profileImage, true);
    }

    public Optional<UserProfile> updateBannerImage(String username, MultipartFile bannerImage) {
        return updateImage(username, bannerImage, false);
    }

    public Optional<UserProfile> updateUserProfileRole(String username, String newRole) {
        return userProfilesRepository.findByUsername(username).map(userProfile -> {
            userProfile.setRole(newRole);
            userProfilesRepository.save(userProfile);

            userService.synchronizeRoleToMySQL(username);

            return userProfile;
        });
    }

    private String determineRoleByAuthKey(int authKey) {
        switch (authKey) {
            case 2001:
                return "member";
            case 2002:
                return "player";
            case 2003:
                return "organizer";
            default:
                throw new IllegalArgumentException("Invalid authKey: " + authKey);
        }
    }

    private void validateRole(String role) {
        if (!"player".equals(role) && !"organizer".equals(role)) {
            throw new IllegalArgumentException("Invalid role specified: " + role);
        }
    }

    private void clearPlayerFields(UserProfile userProfile) {
        userProfile.setSpecialization(null);
        userProfile.setGame(null);
    }

    private Optional<UserProfile> updateImage(String username, MultipartFile image, boolean isProfileImage) {
        return userProfilesRepository.findByUsername(username).map(userProfile -> {
            try {
                if (isProfileImage) {
                    userProfile.setProfileImage(image.getBytes());
                } else {
                    userProfile.setBannerImage(image.getBytes());
                }
                userProfilesRepository.save(userProfile);
            } catch (IOException e) {
                throw new RuntimeException("Failed to update image for user: " + username, e);
            }
            return userProfile;
        });
    }
}
