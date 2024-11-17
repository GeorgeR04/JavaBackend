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

    private String name;
    private String description;
    private byte[] image;
    private List<String> organizerIds; // List of individual organizer IDs
    private String teamId; // Organizer team ID (if applicable)
    private double reputation; // Calculated reputation
    private String rank; // Calculated rank (D, C, B, A, S)
    private double cashPrize;
    private int maxTeams;
    private List<String> participatingIds; // IDs of participants (players or teams, depending on type)
    private String gameId;
    private String type; // "solo" or "team" tournament type
    private String status; // Tournament status (e.g., "ONGOING", "FINISHED")
    private LocalDateTime startDate;
    private LocalDateTime finishDate;
    private String visibility; // "public" or "private"
    private Integer minRankRequirement;
    private Integer maxRankRequirement;
    private Integer trustFactorRequirement;
    private String mvpPlayerId; // Most Valuable Player ID
}
