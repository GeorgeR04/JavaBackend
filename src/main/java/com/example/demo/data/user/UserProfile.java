package com.example.demo.data.user;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document(collection = "DataProfil")
public class UserProfile {
    @Id
    private String id;
    private Long userId; // Lien vers l'utilisateur dans MySQL
    private String username; // Nom d'utilisateur
    private String firstname; // Prénom
    private String lastname;  // Nom
    private byte[] profileImage; // Image de profil sous forme de tableau de bytes
    private byte[] bannerImage;  // Image bannière
    private List<String> tournamentImages; // URLs ou IDs des images de tournois
    private List<String> friendIds; // IDs des amis (utilisateurs)
    private List<String> postIds;   // IDs des posts du système de chat
    private String role; // Rôle de l'utilisateur (ex : "member", "player", "organizer", "moderator")
    private String specialization; // Spécialisation si le rôle est "player"
    private String game; // Jeu sélectionné si le rôle est "player"
    private String rank; // Rang du joueur, null pour les nouveaux joueurs
    private double trustFactor; // Facteur de confiance (par exemple, entre 0.0 et 1.0)
    private List<String> pastTournaments; // IDs des tournois auxquels l'utilisateur a participé
    private List<String> currentTournaments; // IDs des tournois en cours
    private boolean isTeamLeader = false; // Valeur par défaut false
}
