package com.example.demo.data;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Base64;
import java.util.List;

@Data
@Document(collection = "DataProfil")
public class UserProfile {
    @Id
    private String id;

    private Long userId; // Link to the User in MySQL
    private String username; // Username from MySQL User entity
    private String firstname; // First name from MySQL User entity
    private String lastname;  // Last name from MySQL User entity

    private byte[] profileImage; // Profile image data as a byte array
    private byte[] bannerImage;  // Banner image data as a byte array

    private List<String> tournamentImages; // URLs or IDs of tournament images
    private List<String> friendIds; // IDs of friends (users)
    private List<String> postIds;   // IDs of posts in the chat system

    public String getProfileImageAsBase64() {
        return profileImage != null ? Base64.getEncoder().encodeToString(profileImage) : null;
    }

    public String getBannerImageAsBase64() {
        return bannerImage != null ? Base64.getEncoder().encodeToString(bannerImage) : null;
    }

}
