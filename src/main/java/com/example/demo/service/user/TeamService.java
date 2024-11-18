package com.example.demo.service.user;

import com.example.demo.data.user.Team;
import com.example.demo.data.user.UserProfile;
import com.example.demo.repository.mongoDB.user.TeamRepository;
import com.example.demo.repository.mongoDB.user.UserProfileRepository;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class TeamService {

    private final TeamRepository teamRepository;
    private final UserProfileRepository userProfileRepository;

    public TeamService(TeamRepository teamRepository, UserProfileRepository userProfileRepository) {
        this.teamRepository = teamRepository;
        this.userProfileRepository = userProfileRepository;
    }

    public List<Team> getAllTeams() {
        return teamRepository.findAll();
    }

    public Team getTeamById(String id) {
        return teamRepository.findById(id);
    }


    public Team createTeam(Team team) {
        // Check if the player has already created a team
        if (hasPlayerCreatedTeam(team.getTeamLeaderId())) {
            throw new IllegalStateException("You have already created a team. Each player can create only one team.");
        }

        // Ensure the team leader is part of the members list with the LEADER role
        Team.TeamMember leader = new Team.TeamMember();
        leader.setPlayerId(team.getTeamLeaderId());
        leader.setRole(Team.Role.LEADER); // Explicitly assign LEADER role

        if (team.getMembers() == null) {
            team.setMembers(List.of(leader)); // Initialize with leader if no members exist
        } else {
            // Ensure the leader is added to the members list
            boolean leaderExists = team.getMembers().stream()
                    .anyMatch(member -> member.getPlayerId().equals(team.getTeamLeaderId()));

            if (!leaderExists) {
                team.getMembers().add(leader);
            } else {
                // Ensure existing leader role is LEADER (in case of inconsistency)
                team.getMembers().forEach(member -> {
                    if (member.getPlayerId().equals(team.getTeamLeaderId())) {
                        member.setRole(Team.Role.LEADER);
                    }
                });
            }
        }

        // Ensure the team leader is part of the playerIds for backward compatibility
        if (team.getPlayerIds() == null) {
            team.setPlayerIds(List.of(team.getTeamLeaderId()));
        } else if (!team.getPlayerIds().contains(team.getTeamLeaderId())) {
            team.getPlayerIds().add(team.getTeamLeaderId());
        }

        // Calculate and set the team's rank
        String calculatedRank = calculateTeamRank(team.getPlayerIds());
        team.setRank(calculatedRank);

        // Save the team to the database
        return teamRepository.save(team);
    }

    private boolean hasPlayerCreatedTeam(String playerId) {
        return teamRepository.existsByTeamLeaderId(playerId);
    }

    private String calculateTeamRank(List<String> playerIds) {
        if (playerIds == null || playerIds.isEmpty()) {
            return "D"; // Default to lowest rank if no players
        }

        // Fetch user profiles for the player IDs
        List<UserProfile> userProfiles = userProfileRepository.findAllByIds(playerIds);

        if (userProfiles.isEmpty()) {
            return "D"; // Default to lowest rank if no user profiles found
        }

        // Calculate average rank based on player profiles
        double averageRank = userProfiles.stream()
                .mapToDouble(profile -> convertRankToNumeric(profile.getRank()))
                .average()
                .orElse(0);

        return determineRankFromAverage(averageRank);
    }

    private int convertRankToNumeric(String rank) {
        switch (rank) {
            case "S": return 10;
            case "A": return 8;
            case "B": return 6;
            case "C": return 4;
            case "D": return 2;
            default: return 0; // Default for unknown or null ranks
        }
    }

    private String determineRankFromAverage(double average) {
        if (average >= 8) return "S";
        if (average >= 6) return "A";
        if (average >= 4) return "B";
        if (average >= 2) return "C";
        return "D";
    }

    public boolean updateTeam(String id, Team team) {
        if (teamRepository.existsById(id)) {
            team.setId(id);
            teamRepository.save(team);
            return true;
        }
        return false;
    }

    public boolean deleteTeam(String id) {
        if (teamRepository.existsById(id)) {
            teamRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public boolean isTeamLeader(String username, String teamId) {
        Team team = teamRepository.findById(teamId);
        return team != null && username.equals(team.getTeamLeaderId());
    }

    public List<Team> getTeamsByGame(String gameId) {
        return teamRepository.findByGameId(gameId);
    }

    public Team addPlayerToTeam(String teamId, String playerId) {
        Team team = teamRepository.findById(teamId);
        if (team == null) {
            throw new IllegalStateException("Team not found.");
        }

        // Check if the player is already a member of the team
        boolean isAlreadyMember = team.getMembers().stream()
                .anyMatch(member -> member.getPlayerId().equals(playerId));
        if (isAlreadyMember) {
            throw new IllegalStateException("Player is already a member of the team.");
        }

        // Add the player to the team with MEMBER role
        Team.TeamMember newMember = new Team.TeamMember();
        newMember.setPlayerId(playerId);
        newMember.setRole(Team.Role.MEMBER);

        team.getMembers().add(newMember);

        // Ensure player ID is added to playerIds for backward compatibility
        if (!team.getPlayerIds().contains(playerId)) {
            team.getPlayerIds().add(playerId);
        }

        // Save the updated team
        return teamRepository.save(team);
    }


}
