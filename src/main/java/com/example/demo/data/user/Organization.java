package com.example.demo.data.user;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.List;

@Data
@Document(collection = "organizations")
public class Organization {
    @Id
    private String id;

    private String name; // Name of the organization
    private String founderId; // ID of the founder (organizer or user)
    private List<String> teamIds; // List of team IDs within this organization
    private LocalDate dateFounded; // Date the organization was founded
    private String description; // Organization's description
    private byte[] logo; // Organization's logo

    private boolean isOrganizerTeam; // Indicates if this is an organizer-backed organization
}
