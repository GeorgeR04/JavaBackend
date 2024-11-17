package com.example.demo.data.user;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document(collection = "organizer_teams")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class OrganizerTeam {
    @Id
    @EqualsAndHashCode.Include
    private String id;

    private String name; // Organizer team name
    private List<String> organizerIds; // IDs of the organizers in the team
    private String primaryOrganizerId; // Main organizer ID
    private byte[] teamLogo; // Team logo
    private String description; // Optional description of the team
    private double reputation; // Reputation score for the team
}
