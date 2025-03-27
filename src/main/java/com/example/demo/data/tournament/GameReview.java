package com.example.demo.data.tournament;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "gameReviews")
public class GameReview {
    @Id
    private String id;
    private String gameId;   // ID du jeu concerné
    private String userId;   // ID de l’utilisateur qui a posté la revue
    private int rating;      // Note (par exemple de 1 à 5)
    private String reviewText; // Texte de la revue
    private LocalDateTime timestamp; // Date et heure de la revue
}
