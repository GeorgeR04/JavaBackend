package com.example.demo.controller.user;

import com.example.demo.data.user.Team;
import com.example.demo.service.user.TeamService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teams")
public class TeamController {

    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @GetMapping
    public ResponseEntity<List<Team>> getAllTeams() {
        return ResponseEntity.ok(teamService.getAllTeams());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Team> getTeamById(@PathVariable String id) {
        Team team = teamService.getTeamById(id);
        return team != null ? ResponseEntity.ok(team) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<?> createTeam(@RequestBody Team team) {
        if (team == null || team.getName() == null || team.getName().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Team name is required.");
        }
        if (team.getPlayerIds() == null || team.getPlayerIds().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Team must have at least one player.");
        }
        if (team.getGameId() == null || team.getGameId().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Game ID is required.");
        }

        Team createdTeam = teamService.createTeam(team);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTeam);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTeam(@PathVariable String id, @RequestBody Team team) {
        if (team == null || team.getName() == null || team.getName().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Team name is required.");
        }

        boolean updated = teamService.updateTeam(id, team);
        return updated ? ResponseEntity.ok("Team updated successfully.")
                : ResponseEntity.status(HttpStatus.NOT_FOUND).body("Team not found.");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTeam(@PathVariable String id) {
        boolean deleted = teamService.deleteTeam(id);
        return deleted ? ResponseEntity.ok("Team deleted successfully.")
                : ResponseEntity.status(HttpStatus.NOT_FOUND).body("Team not found.");
    }

    @GetMapping("/game/{gameId}")
    public ResponseEntity<List<Team>> getTeamsByGame(@PathVariable String gameId) {
        List<Team> teams = teamService.getTeamsByGame(gameId);
        return teams != null && !teams.isEmpty()
                ? ResponseEntity.ok(teams)
                : ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }

    @GetMapping("/{id}/leader/{username}")
    public ResponseEntity<?> isTeamLeader(@PathVariable String id, @PathVariable String username) {
        boolean isLeader = teamService.isTeamLeader(username, id);
        return ResponseEntity.ok(isLeader ? "User is the team leader." : "User is not the team leader.");
    }
}
