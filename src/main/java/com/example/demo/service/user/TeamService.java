package com.example.demo.service.user;

import com.example.demo.data.user.Team;
import com.example.demo.repository.mongoDB.TeamRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeamService {

    private final TeamRepository teamRepository;

    public TeamService(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }

    public List<Team> getAllTeams() {
        return teamRepository.findAll();
    }

    public Team getTeamById(String id) {
        return teamRepository.findById(id);
    }

    public Team createTeam(Team team) {
        return teamRepository.save(team);
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

    public boolean validateTeamCriteria(Team team, int requiredSize) {
        return team != null &&
                team.getPlayerIds() != null &&
                team.getPlayerIds().size() == requiredSize;
    }

    public List<Team> getTeamsByGame(String gameId) {
        return teamRepository.findByGameId(gameId);
    }
}
