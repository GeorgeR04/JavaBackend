package com.example.demo.service.user;

import com.example.demo.data.user.UserProfile;
import com.example.demo.repository.mongoDB.user.UserProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@Service
public class UserProfileService {

    @Autowired
    private UserService userService;

    @Autowired
    private UserProfileRepository userProfileRepository;



    public Optional<UserProfile> getUserProfileByUsername(String username) {
        return userProfileRepository.findByUsername(username);
    }

    public UserProfile initializeUserProfileWithRole(UserProfile userProfile, int authKey) {
        switch (authKey) {
            case 2001:
                userProfile.setRole("member");
                break;
            case 2002:
                userProfile.setRole("player");
                userProfile.setGame(null); // Initialize game to null
                break;
            case 2003:
                userProfile.setRole("organizer");
                break;
            default:
                throw new IllegalArgumentException("Invalid authKey: " + authKey);
        }
        return userProfileRepository.save(userProfile);
    }

    public Optional<UserProfile> updatePlayerDetails(String username, String role, String specialization, String game) {
        return userProfileRepository.findByUsername(username).map(userProfile -> {
            if (!"player".equals(role) && !"organizer".equals(role)) {
                throw new IllegalArgumentException("Invalid role specified.");
            }

            userProfile.setRole(role);

            if ("player".equals(role)) {
                userProfile.setSpecialization(specialization);
                userProfile.setGame(game);
            } else {
                userProfile.setSpecialization(null);
                userProfile.setGame(null);
            }

            userProfileRepository.save(userProfile);
            return userProfile;
        });
    }

    public Optional<UserProfile> updateProfileImage(String username, MultipartFile profileImage) {
        return userProfileRepository.findByUsername(username).map(userProfile -> {
            try {
                userProfile.setProfileImage(profileImage.getBytes());
                userProfileRepository.save(userProfile);
            } catch (IOException e) {
                e.printStackTrace();
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
            }
            return userProfile;
        });
    }

    public Optional<UserProfile> updateUserProfileRole(String username, String newRole) {
        Optional<UserProfile> userProfileOpt = userProfileRepository.findByUsername(username);

        if (userProfileOpt.isPresent()) {
            UserProfile userProfile = userProfileOpt.get();
            userProfile.setRole(newRole);
            userProfileRepository.save(userProfile); // Update in MongoDB

            // Synchronize with MySQL
            userService.synchronizeRoleToMySQL(username);

            return Optional.of(userProfile);
        } else {
            return Optional.empty();
        }
    }

}

