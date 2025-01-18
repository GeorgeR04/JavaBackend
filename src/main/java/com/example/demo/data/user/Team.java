package com.example.demo.data.user;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Document(collection = "teams")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class Team {
    @Id
    @EqualsAndHashCode.Include
    private String id;

    private String name; // Team name
    private String gameId; // ID of the game the team is competing in
    private String rank; // Team rank (D, C, B, A, S)
    private String teamLeaderId; // ID of the team leader
    private byte[] teamLogo; // Team logo (optional)
    private String privacy; // Privacy level (optional)
    private String organizationId; // ID of the organization this team belongs to
    private int maxPlayers; // Maximum players allowed in the team (based on the game)
    private LocalDate dateOfExistence; // The date the team was created
    private LocalDate dateOfDisband; // The date the team was disbanded (null if active)
    private boolean isTeamLeader;
    @Getter
    private List<TeamMember> members = new ArrayList<>(); // List of team members with roles

    public List<String> getPlayerIds() {
        List<String> playerIds = new ArrayList<>();
        for (TeamMember member : members) {
            playerIds.add(member.getPlayerId());
        }
        return playerIds;
    }

    public void setPlayerIds(List<String> playerIds) {
        if (playerIds == null) {
            this.members.clear();
            return;
        }

        List<TeamMember> updatedMembers = new ArrayList<>();
        for (String playerId : playerIds) {
            TeamMember existingMember = members.stream()
                    .filter(member -> member.getPlayerId().equals(playerId))
                    .findFirst()
                    .orElse(null);

            if (existingMember != null) {
                updatedMembers.add(existingMember);
            } else {
                TeamMember newMember = new TeamMember();
                newMember.setPlayerId(playerId);
                newMember.setRole(Role.MEMBER);
                updatedMembers.add(newMember);
            }
        }
        this.members = updatedMembers;
    }

    @Data
    @ToString
    @EqualsAndHashCode
    public static class TeamMember {
        private String playerId; // ID of the player
        private Role role; // Role of the player in the team
    }

    public enum Role {
        LEADER,
        MODERATOR,
        MEMBER
    }
}
