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

    /**
     * Endpoint: GET /api/teams/list
     * Description: Retrieve all teams.
     */
    @GetMapping("/list")
    public ResponseEntity<List<Team>> getAllTeams() {
        return ResponseEntity.ok(teamService.getAllTeams());
    }

    /**
     * Endpoint: GET /api/teams/{id}
     * Description: Retrieve team details by team ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Team> getTeamById(@PathVariable String id) {
        Team team = teamService.getTeamById(id);
        return team != null ? ResponseEntity.ok(team) : ResponseEntity.notFound().build();
    }

    /**
     * Endpoint: POST /api/teams
     * Description: Create a new team.
     */

    @PostMapping
    public ResponseEntity<?> createTeam(@RequestBody Team team) {
        if (team.getName() == null || team.getName().isEmpty()) {
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

    /**
     * Endpoint: PUT /api/teams/{id}
     * Description: Update team details by ID.
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTeam(@PathVariable String id, @RequestBody Team team) {
        if (team == null || team.getName() == null || team.getName().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Team name is required.");
        }

        boolean updated = teamService.updateTeam(id, team);
        return updated ? ResponseEntity.ok("Team updated successfully.")
                : ResponseEntity.status(HttpStatus.NOT_FOUND).body("Team not found.");
    }

    /**
     * Endpoint: DELETE /api/teams/{id}
     * Description: Delete a team by ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTeam(@PathVariable String id) {
        boolean deleted = teamService.deleteTeam(id);
        return deleted ? ResponseEntity.ok("Team deleted successfully.")
                : ResponseEntity.status(HttpStatus.NOT_FOUND).body("Team not found.");
    }

    /**
     * Endpoint: GET /api/teams/game/{gameId}
     * Description: Retrieve teams by game ID.
     */
    @GetMapping("/game/{gameId}")
    public ResponseEntity<List<Team>> getTeamsByGame(@PathVariable String gameId) {
        List<Team> teams = teamService.getTeamsByGame(gameId);
        return teams != null && !teams.isEmpty()
                ? ResponseEntity.ok(teams)
                : ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }

    /**
     * Endpoint: GET /api/teams/{id}/leader/{username}
     * Description: Check if a user is the leader of a team.
     */
    @GetMapping("/{id}/leader/{username}")
    public ResponseEntity<?> isTeamLeader(@PathVariable String id, @PathVariable String username) {
        boolean isLeader = teamService.isTeamLeader(username, id);
        return ResponseEntity.ok(isLeader ? "User is the team leader." : "User is not the team leader.");
    }

    /**
     * Endpoint: POST /api/teams/{id}/join
     * Description: Add a player to a team.
     */
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


}
