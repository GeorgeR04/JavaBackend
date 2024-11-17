package com.example.demo.data.user;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document(collection = "teams")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class Team {
    @Id
    @EqualsAndHashCode.Include
    private String id;

    private String name;
    private List<String> playerIds;
    private byte[] teamLogo;
    private String gameId;
    private String rank;

    private String teamLeaderId; // Explicit leader field
}
