package com.example.demo.data.match;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "matches")
public class Match {
    @Id
    private String id;

    private String roundId; // ID of the parent round
    private String tournamentId; // ID of the parent tournament
    private String team1Id; // Team 1 or Player 1 ID
    private String team2Id; // Team 2 or Player 2 ID
    private String team1Name; // Team 1 or Player 1 name
    private String team2Name; // Team 2 or Player 2 name
    private byte[] team1Logo; // Logo for Team 1 (optional)
    private byte[] team2Logo; // Logo for Team 2 (optional)
    private int team1Score; // Score for Team 1
    private int team2Score; // Score for Team 2
    private String winnerId; // ID of the winner (team or player)
    private String loserId; // ID of the loser (if applicable, for loser's bracket)
    private LocalDateTime matchDate; // Date and time of the match
}
