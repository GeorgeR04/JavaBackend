package com.example.demo.data.tournament;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document(collection = "games")
public class Game {
    @Id
    private String id;
    private String name; // Nom du jeu
    private String type; // Genre/type du jeu (ex : FPS, MOBA)
    private String description; // Description brève
    private Integer yearOfExistence; // Année de sortie du jeu
    private String lastTournamentDate; // Date du dernier tournoi
    private String bestPlayerId; // ID du meilleur joueur
    private String bestTeamId; // ID de la meilleure équipe
    private int totalPlayers; // Nombre total de joueurs actifs
    private int maxPlayersPerTeam; // Nombre maximum de joueurs par équipe
    private List<String> platforms; // Plateformes supportées (ex : PC, PlayStation)
    private String publisher; // Nom du développeur/éditeur
    private byte[] gameImage; // Image du jeu
    private List<String> tournamentIds; // Liste des IDs des tournois associés
    private String organizerId; // ID de l’organisateur ayant ajouté le jeu

    // Nouveaux champs pour les fonctionnalités étendues
    private Boolean approved;  // Statut d'approbation (true si le jeu est publié directement, false s'il est en attente)
    private String rules;      // Règles détaillées du jeu
    private String tutorial;   // URL ou texte pour les tutoriels/guide du jeu
}
