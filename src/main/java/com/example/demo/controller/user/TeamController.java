package com.example.demo.controller.user;

import com.example.demo.data.user.Team;
import com.example.demo.service.user.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class TeamController {

    private final TeamService teamService;

    @GetMapping("/list")
    public ResponseEntity<List<Team>> getAllTeams() {
        List<Team> teams = teamService.getAllTeams();
        return teams.isEmpty() ? ResponseEntity.status(HttpStatus.NO_CONTENT).build() : ResponseEntity.ok(teams);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Team> getTeamById(@PathVariable String id) {
        return teamService.getTeamById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping
    public ResponseEntity<?> createTeam(@RequestBody Team team) {
        String validationError = validateTeam(team);
        if (validationError != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validationError);
        }

        Team createdTeam = teamService.createTeam(team);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTeam);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTeam(@PathVariable String id, @RequestBody Team team) {
        String validationError = validateTeamForUpdate(team);
        if (validationError != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validationError);
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
    public ResponseEntity<?> getTeamsByGame(@PathVariable String gameId) {
        List<Team> teams = teamService.getTeamsByGame(gameId);
        return teams.isEmpty()
                ? ResponseEntity.status(HttpStatus.NOT_FOUND).body("No teams found for the specified game.")
                : ResponseEntity.ok(teams);
    }

    @GetMapping("/{id}/leader/{username}")
    public ResponseEntity<?> isTeamLeader(@PathVariable String id, @PathVariable String username) {
        boolean isLeader = teamService.isTeamLeader(username, id);
        return ResponseEntity.ok(isLeader ? "User is the team leader." : "User is not the team leader.");
    }

    @PostMapping("/{id}/join")
    public ResponseEntity<?> joinTeam(@PathVariable String id, @RequestParam String playerId) {
        try {
            Team updatedTeam = teamService.addPlayerToTeam(id, playerId);
            return ResponseEntity.ok(updatedTeam);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while joining the team.");
        }
    }

    // Private helper methods
    private String validateTeam(Team team) {
        if (team.getName() == null || team.getName().isEmpty()) {
            return "Team name is required.";
        }
        if (team.getPlayerIds() == null || team.getPlayerIds().isEmpty()) {
            return "Team must have at least one player.";
        }
        if (team.getGameId() == null || team.getGameId().isEmpty()) {
            return "Game ID is required.";
        }
        return null;
    }

    private String validateTeamForUpdate(Team team) {
        if (team == null || team.getName() == null || team.getName().isEmpty()) {
            return "Team name is required.";
        }
        return null;
    }
}
