package com.example.demo.data.tournament;

import com.example.demo.data.match.Match;
import com.example.demo.data.match.Round;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Document(collection = "tournaments")
public class Tournament {
    @Id
    private String id;

    private String name; // Tournament name
    private String description; // Tournament description
    private byte[] image; // Tournament image/logo
    private List<String> organizerIds; // List of individual organizer IDs
    private String teamId; // Organizer team ID (if applicable)
    private double reputation; // Calculated reputation
    private String rank; // Calculated rank (D, C, B, A, S)
    private double cashPrize; // Prize pool
    private int maxTeams; // Maximum teams/players allowed
    private List<String> participatingIds = new ArrayList<>();// Ensure participatingIds defaults to an empty list
    private String gameId; // Game ID for the tournament
    private String type; // "solo" or "team"
    private String status; // Tournament status: "ONGOING", "FINISHED", etc.
    private LocalDateTime startDate; // Tournament start date
    private LocalDateTime finishDate; // Tournament finish date
    private String visibility; // "public" or "private"
    private Integer minRankRequirement; // Minimum rank required
    private Integer maxRankRequirement; // Maximum rank allowed
    private Integer trustFactorRequirement; // Trust factor requirement
    private String mvpPlayerId; // MVP player ID
    private String victory; // Final winner (team/player name or ID)
    private String rule; // Tournament rules

    // New fields for format
    private String format; // Format: "single-elimination", "double-elimination", "swiss", etc.
    private List<Round> rounds; // All rounds in the tournament
    private List<Match> loserBracketMatches; // Matches for the loser's bracket in double-elimination format
}
