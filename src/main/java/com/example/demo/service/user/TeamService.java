package com.example.demo.service.user;

import com.example.demo.data.user.Team;
import com.example.demo.data.user.UserProfile;
import com.example.demo.repository.mongoDB.user.TeamsRepository;
import com.example.demo.repository.mongoDB.user.UserProfilesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamsRepository teamsRepository;
    private final UserProfilesRepository userProfilesRepository;

    public List<Team> getAllTeams() {
        return teamsRepository.findAll();
    }

    public Optional<Team> getTeamById(String id) {
        return teamsRepository.findById(id);
    }

    public Team createTeam(Team team) {
        validateTeamLeader(team.getTeamLeaderId());
        ensureLeaderIsInMembers(team);
        ensureLeaderInPlayerIds(team);
        team.setRank(calculateTeamRank(team.getPlayerIds()));

        // Update the UserProfile to set isTeamLeader to true
        userProfilesRepository.findById(team.getTeamLeaderId())
                .ifPresent(userProfile -> {
                    userProfile.setTeamLeader(true);
                    userProfilesRepository.save(userProfile);
                });

        return teamsRepository.save(team);
    }

    private void validateTeamLeader(String teamLeaderId) {
        if (teamsRepository.existsByTeamLeaderId(teamLeaderId)) {
            throw new IllegalStateException("You have already created a team. Each player can create only one team.");
        }
    }

    private void ensureLeaderIsInMembers(Team team) {
        Team.TeamMember leader = new Team.TeamMember();
        leader.setPlayerId(team.getTeamLeaderId());
        leader.setRole(Team.Role.LEADER);

        if (team.getMembers() == null) {
            team.setMembers(List.of(leader));
        } else {
            if (team.getMembers().stream().noneMatch(member -> member.getPlayerId().equals(team.getTeamLeaderId()))) {
                team.getMembers().add(leader);
            } else {
                team.getMembers().forEach(member -> {
                    if (member.getPlayerId().equals(team.getTeamLeaderId())) {
                        member.setRole(Team.Role.LEADER);
                    }
                });
            }
        }
    }

    private void ensureLeaderInPlayerIds(Team team) {
        if (team.getPlayerIds() == null) {
            team.setPlayerIds(new ArrayList<>());
        }
        if (!team.getPlayerIds().contains(team.getTeamLeaderId())) {
            team.getPlayerIds().add(team.getTeamLeaderId());
        }
    }

    private String calculateTeamRank(List<String> playerIds) {
        if (playerIds == null || playerIds.isEmpty()) {
            return "D";
        }

        List<UserProfile> userProfiles = userProfilesRepository.findByIdIn(playerIds);
        if (userProfiles.isEmpty()) {
            return "D";
        }

        double averageRank = userProfiles.stream()
                .mapToDouble(profile -> convertRankToNumeric(profile.getRank()))
                .average()
                .orElse(0);

        return determineRankFromAverage(averageRank);
    }

    private int convertRankToNumeric(String rank) {
        return switch (rank) {
            case "S" -> 10;
            case "A" -> 8;
            case "B" -> 6;
            case "C" -> 4;
            case "D" -> 2;
            default -> 0;
        };
    }

    private String determineRankFromAverage(double average) {
        if (average >= 8) return "S";
        if (average >= 6) return "A";
        if (average >= 4) return "B";
        if (average >= 2) return "C";
        return "D";
    }

    public boolean updateTeam(String id, Team team) {
        if (teamsRepository.existsById(id)) {
            team.setId(id);
            teamsRepository.save(team);
            return true;
        }
        return false;
    }

    public boolean deleteTeam(String id) {
        return teamsRepository.findById(id)
                .map(team -> {
                    teamsRepository.deleteById(id);
                    // Reset the `isTeamLeader` flag for the team leader
                    userProfilesRepository.findById(team.getTeamLeaderId())
                            .ifPresent(userProfile -> {
                                userProfile.setTeamLeader(false);
                                userProfilesRepository.save(userProfile);
                            });
                    return true;
                })
                .orElse(false);
    }

    public boolean isTeamLeader(String username, String teamId) {
        return teamsRepository.isTeamLeader(teamId, username);
    }

    public List<Team> getTeamsByGame(String gameId) {
        return teamsRepository.findByGameId(gameId);
    }

    public Team addPlayerToTeam(String teamId, String playerId) {
        Team team = teamsRepository.findById(teamId)
                .orElseThrow(() -> new IllegalStateException("Team not found."));

        if (team.getMembers().stream().anyMatch(member -> member.getPlayerId().equals(playerId))) {
            throw new IllegalStateException("Player is already a member of the team.");
        }

        Team.TeamMember newMember = new Team.TeamMember();
        newMember.setPlayerId(playerId);
        newMember.setRole(Team.Role.MEMBER);
        team.getMembers().add(newMember);

        if (!team.getPlayerIds().contains(playerId)) {
            team.getPlayerIds().add(playerId);
        }

        return teamsRepository.save(team);
    }
}
