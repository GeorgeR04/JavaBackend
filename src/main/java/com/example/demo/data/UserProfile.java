package com.example.demo.data;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

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
    private String profileImage; // URL or path for the profile image
    private String bannerImage;  // URL or path for the banner image
    private List<String> tournamentImages; // URLs or IDs of tournament images
    private List<String> friendIds; // IDs of friends (users)
    private List<String> postIds;   // IDs of posts in the chat system
}
