package com.example.demo.data.match;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document(collection = "rounds")
public class Round {
    @Id
    private String id;

    private String tournamentId; // ID of the parent tournament
    private String roundName; // Name of the round (e.g., "Quarter-finals", "Swiss Round 1")
    private String type; // Type of the round: "knockout", "swiss", "loser-bracket", etc.
    private int roundNumber; // Numeric identifier for the round order
    private List<String> matchIds; // IDs of matches in this round
}
