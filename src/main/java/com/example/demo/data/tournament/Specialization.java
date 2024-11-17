package com.example.demo.data.tournament;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document(collection = "specializations")
public class Specialization {
    @Id
    private String id;

    private String name; // Name of the specialization
    private String description; // Description of the specialization
    private List<String> preferredGames; // List of game IDs this specialization applies to
    private List<String> skillsRequired; // Skills required for this specialization
    private List<String> popularPlayers; // List of popular player IDs
    private double averageSuccessRate; // Average success rate
}
