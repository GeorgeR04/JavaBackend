// File: com/example/demo/data/Game.java
package com.example.demo.data;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document(collection = "games")
public class Game {
    @Id
    private String id;

    private String name; // Name of the game
    private String type; // Genre/type of the game (e.g., FPS, MOBA)
    private String description; // Brief description
    private int yearOfExistence; // Year the game was released
    private String lastTournamentDate; // Date of the most recent tournament
    private String bestPlayerId; // Best player ID
    private String bestTeamId; // Best team ID
    private int totalPlayers; // Total active players
    private List<String> platforms; // Supported platforms (e.g., PC, PlayStation)
    private String developer; // Name of the game developer
    private byte[] gameImage; // Game image
    private List<String> tournamentIds; // List of tournament IDs associated with the game
}
