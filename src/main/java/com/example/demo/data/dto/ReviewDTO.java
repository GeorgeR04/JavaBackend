package com.example.demo.data.dto;

import com.example.demo.data.tournament.GameReview;
import com.example.demo.data.user.UserProfile;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Base64;

@Data
public class ReviewDTO {
    private String id;
    private String gameId;
    private String userId;
    private int rating;
    private String reviewText;
    private LocalDateTime timestamp;
    // Détails de l'utilisateur
    private String username;
    private String rank;
    private double trustFactor;
    private String team; // Champ optionnel si disponible
    private String profileImage; // Image encodée en Base64

    public ReviewDTO(GameReview review, UserProfile user) {
        this.id = review.getId();
        this.gameId = review.getGameId();
        this.userId = review.getUserId();
        this.rating = review.getRating();
        this.reviewText = review.getReviewText();
        this.timestamp = review.getTimestamp();
        if (user != null) {
            this.username = user.getUsername();
            this.rank = user.getRank();
            this.trustFactor = user.getTrustFactor();
            // Si une équipe est définie, vous pouvez l'affecter ici (sinon, laisser vide)
            this.team = ""; // Modifier si le UserProfile possède un champ "team"
            if (user.getProfileImage() != null) {
                this.profileImage = Base64.getEncoder().encodeToString(user.getProfileImage());
            }
        }
    }
}
