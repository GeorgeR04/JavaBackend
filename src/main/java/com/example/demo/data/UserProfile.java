// File: com/example/demo/data/UserProfile.java
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

    private byte[] profileImage; // Profile image data as a byte array
    private byte[] bannerImage;  // Banner image data as a byte array

    private List<String> tournamentImages; // URLs or IDs of tournament images
    private List<String> friendIds; // IDs of friends (users)
    private List<String> postIds;   // IDs of posts in the chat system

    private String role; // Role of the user (e.g., "member", "player", "organizer", "moderator")
    private String specialization; // Specialization if the role is "player"
    private String game; // Selected game if the role is "player"
    private String rank; // Player rank, null for new players
    private double trustFactor; // Trust factor to determine player's behavior (e.g., 0.0 - 1.0)
}
