package com.example.demo.data.tournament;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "tournaments")
public class Tournament {
    @Id
    private String id;

    private String name; // Tournament name
    private String description; // Short description
    private byte[] image; // Tournament image
    private String organizerId; // Organizer (User ID, optional)
    private String organizerTeamId; // Organizer team (Team ID, optional)
    private double cashPrize; // Prize money
    private int maxTeams; // Maximum teams allowed
    private List<String> participatingTeamIds; // List of participating team IDs
    private String gameId; // Game associated with this tournament
    private String type; // Tournament type (e.g., D, C, B, A, S)
    private String status; // "ONGOING" or "FINISHED"
    private LocalDateTime startDate; // Start date and time
    private LocalDateTime finishDate; // Finish date and time
    private String visibility; // "private" or "public"
    private Integer RankRequirement; // Player rank, null for new players
    private Integer TrustFactorRequirement; // Trust factor to determine player's behavior (e.g., 0.0 - 1.0)
}
