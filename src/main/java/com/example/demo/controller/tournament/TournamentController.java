package com.example.demo.controller.tournament;

import com.example.demo.data.tournament.Game;
import com.example.demo.data.tournament.Tournament;
import com.example.demo.data.user.UserProfile;
import com.example.demo.security.request.JwtUtil;
import com.example.demo.service.match.MatchService;
import com.example.demo.service.match.RoundService;
import com.example.demo.service.tournament.GameService;
import com.example.demo.service.user.TeamService;
import com.example.demo.service.tournament.TournamentService;
import com.example.demo.service.user.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tournaments")
@RequiredArgsConstructor
public class TournamentController {

    private final JwtUtil jwtUtil;
    private final TournamentService tournamentService;
    private final TeamService teamService;
    private final UserProfileService userProfileService;
    private final GameService gameService;
    private final RoundService roundService;
    private final MatchService matchService;

    private String getRoleFromToken(String token) {
        return Optional.ofNullable(jwtUtil.extractUsername(token))
                .flatMap(userProfileService::getUserProfileByUsername)
                .map(UserProfile::getRole)
                .orElse(null);
    }

    @GetMapping("/list")
    public ResponseEntity<List<Map<String, Object>>> getAllTournaments() {
        List<Tournament> tournaments = tournamentService.getAllTournaments();

        List<Map<String, Object>> response = tournaments.stream()
                .map(this::buildTournamentResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/create")
    public ResponseEntity<?> createTournament(@RequestBody Map<String, Object> payload, HttpServletRequest request) {
        String token = extractTokenFromRequest(request);
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid token.");
        }

        String username = jwtUtil.extractUsername(token);
        UserProfile userProfile = userProfileService.getUserProfileByUsername(username)
                .orElse(null);

        if (userProfile == null || !"organizer".equals(userProfile.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only organizers can create tournaments.");
        }

        try {
            Tournament tournament = buildTournamentFromPayload(payload, username);
            tournamentService.createTournament(tournament);
            return ResponseEntity.status(HttpStatus.CREATED).body(tournament);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating tournament.");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTournament(@PathVariable String id) {
        return tournamentService.getTournamentById(id)
                .map(tournament -> {
                    if (!"PUBLIC".equalsIgnoreCase(tournament.getVisibility())) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("This tournament is not publicly accessible.");
                    }
                    Map<String, Object> response = buildTournamentResponse(tournament);
                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("Tournament not found."));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTournament(
            @PathVariable String id,
            @RequestBody Map<String, Object> payload,
            HttpServletRequest request) {

        // Extract the token from the request
        String token = jwtUtil.extractToken(request.getHeader("Authorization"));
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid token.");
        }

        // Extract username and role from the token
        String username = jwtUtil.extractUsername(token);
        String role = tournamentService.getRoleFromToken(token);

        return tournamentService.getTournamentById(id)
                .map(existingTournament -> {
                    // Check if the user can update the tournament
                    if (!hasUpdateAccess(role, username, existingTournament)) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body("You do not have permission to modify this tournament.");
                    }

                    try {
                        // Update tournament details
                        tournamentService.updateTournamentDetails(existingTournament, payload);
                        return ResponseEntity.ok(existingTournament);
                    } catch (IllegalArgumentException e) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
                    } catch (Exception e) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body("Error updating tournament.");
                    }
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("Tournament not found."));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTournament(@PathVariable String id, HttpServletRequest request) {
        // Extract the token from the request
        String token = jwtUtil.extractToken(request.getHeader("Authorization"));
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid token.");
        }
        // Extract username and role from the token
        String username = jwtUtil.extractUsername(token);
        String role = tournamentService.getRoleFromToken(token);

        return tournamentService.getTournamentById(id)
                .map(tournament -> {
                    // Check if the user can delete the tournament
                    if (!canDeleteTournament(role, username, tournament)) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body("You can only delete your own tournaments.");
                    }

                    if ("FINISHED".equals(tournament.getStatus())) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body("Cannot delete a finished tournament.");
                    }

                    // Proceed with deletion
                    tournamentService.deleteTournament(id);
                    return ResponseEntity.ok("Tournament deleted successfully.");
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("Tournament not found."));
    }

    @PutMapping("/{id}/finish")
    public ResponseEntity<?> finishTournament(@PathVariable String id, HttpServletRequest request) {
        // Extract the token from the request
        String token = jwtUtil.extractToken(request.getHeader("Authorization"));
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid token.");
        }

        // Extract the username from the token
        String username = jwtUtil.extractUsername(token);
        // Get the user's role from the token
        String role = tournamentService.getRoleFromToken(token);
        // Retrieve the tournament by ID
        return tournamentService.getTournamentById(id)
                .map(tournament -> {
                    // Check if the user is authorized to finish the tournament
                    if (!canFinishTournament(role, username, tournament)) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You can only finish tournaments you organize.");
                    }
                    try {
                        // Finish the tournament
                        tournamentService.finishTournament(id);
                        return ResponseEntity.ok("Tournament finished successfully.");
                    } catch (Exception e) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error finishing tournament.");
                    }
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("Tournament not found."));
    }


    @PostMapping("/{id}/join")
    public ResponseEntity<?> joinTournament(@PathVariable String id, @RequestBody Map<String, Object> payload, HttpServletRequest request) {
        return manageTournamentParticipation(id, payload, request, true);
    }

    @PostMapping("/{id}/leave")
    public ResponseEntity<?> leaveTournament(@PathVariable String id, @RequestBody Map<String, Object> payload, HttpServletRequest request) {
        return manageTournamentParticipation(id, payload, request, false);
    }

    // Helper Methods

    private String extractUsernameFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        return jwtUtil.extractUsername(authHeader.substring(7));
    }

    private boolean hasUpdateAccess(String role, String username, Tournament tournament) {
        return "moderator".equals(role) || ("organizer".equals(role) && tournament.getOrganizerIds().contains(username));
    }

    private boolean canDeleteTournament(String role, String username, Tournament tournament) {
        return "moderator".equals(role) || ("organizer".equals(role) && tournament.getOrganizerIds().contains(username));
    }

    private boolean canFinishTournament(String role, String username, Tournament tournament) {
        return "moderator".equals(role) || ("organizer".equals(role) && tournament.getOrganizerIds().contains(username));
    }

    private ResponseEntity<?> manageTournamentParticipation(String id, Map<String, Object> payload, HttpServletRequest request, boolean isJoining) {
        String username = extractUsernameFromRequest(request);
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid token.");
        }

        return tournamentService.getTournamentById(id)
                .map(tournament -> {
                    String type = tournament.getType(); // "solo" or "team"
                    String action = isJoining ? "join" : "leave";

                    try {
                        if ("team".equals(type)) {
                            String teamId = (String) payload.get("teamId");
                            if (!teamService.isTeamLeader(username, teamId)) {
                                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                        .body("Only team leaders can " + action + " team tournaments.");
                            }
                            boolean success = isJoining
                                    ? tournamentService.addParticipant(tournament.getId(), teamId)
                                    : tournamentService.removeParticipant(tournament.getId(), teamId);

                            return success
                                    ? ResponseEntity.ok("Team " + action + "ed successfully.")
                                    : ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                    .body("Failed to " + action + " the tournament.");
                        } else if ("solo".equals(type)) {
                            boolean success = isJoining
                                    ? tournamentService.addParticipant(tournament.getId(), username)
                                    : tournamentService.removeParticipant(tournament.getId(), username);

                            return success
                                    ? ResponseEntity.ok("Successfully " + action + "ed.")
                                    : ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                    .body("Failed to " + action + " the tournament.");
                        }
                    } catch (Exception e) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body("Error processing tournament " + action + ".");
                    }

                    return ResponseEntity.badRequest().body("Invalid tournament type.");
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("Tournament not found."));
    }


    private String extractTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        return (authHeader != null && authHeader.startsWith("Bearer ")) ? authHeader.substring(7) : null;
    }

    private Tournament buildTournamentFromPayload(Map<String, Object> payload, String username) {
        Tournament tournament = new Tournament();
        tournament.setName((String) payload.get("name"));
        tournament.setDescription((String) payload.get("description"));
        tournament.setGameId((String) payload.get("gameId"));
        tournament.setVisibility((String) payload.get("visibility"));
        tournament.setType((String) payload.get("type"));

        // Handle image
        String base64Image = (String) payload.get("image");
        tournament.setImage(base64Image != null ? Base64.getDecoder().decode(base64Image) : null);

        // Set rank requirements
        Integer minRank = parseInteger(payload.get("minRankRequirement"), 0);
        Integer maxRank = parseInteger(payload.get("maxRankRequirement"), 0);
        if (minRank > maxRank) {
            throw new IllegalArgumentException("Minimum rank cannot be greater than maximum rank.");
        }
        tournament.setMinRankRequirement(minRank);
        tournament.setMaxRankRequirement(maxRank);

        // Handle trust factor and maxTeams
        tournament.setTrustFactorRequirement(parseInteger(payload.get("trustFactorRequirement"), 0));
        tournament.setMaxTeams(parseInteger(payload.get("maxTeams"), 0));

        // Set other fields
        tournament.setCashPrize(parseDouble(payload.get("cashPrize"), 0.0));
        tournament.setOrganizerIds(List.of(username)); // Add the creator as the initial organizer

        return tournament;
    }

    private Integer parseInteger(Object value, Integer defaultValue) {
        if (value == null) return defaultValue;
        return Integer.parseInt(value.toString());
    }

    private Double parseDouble(Object value, Double defaultValue) {
        if (value == null) return defaultValue;
        return Double.parseDouble(value.toString());
    }

    private Map<String, Object> buildTournamentResponse(Tournament tournament) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", tournament.getId());
        response.put("name", tournament.getName());
        response.put("description", tournament.getDescription());
        response.put("type", tournament.getType());
        response.put("minRankRequirement", tournament.getMinRankRequirement());
        response.put("maxRankRequirement", tournament.getMaxRankRequirement());
        response.put("trustFactorRequirement", tournament.getTrustFactorRequirement());
        response.put("cashPrize", tournament.getCashPrize());
        response.put("reputation", tournament.getReputation());
        response.put("rank", tournament.getRank());
        response.put("status", tournament.getStatus());
        response.put("visibility", tournament.getVisibility());
        response.put("maxTeams", tournament.getMaxTeams());
        response.put("victory", tournament.getVictory());
        response.put("rule", tournament.getRule());
        response.put("image", tournament.getImage() != null ? Base64.getEncoder().encodeToString(tournament.getImage()) : null);

        // Handle Optional<Game>
        String gameName = gameService.getGameById(tournament.getGameId())
                .map(Game::getName)
                .orElse("Unknown Game");
        response.put("gameName", gameName);

        // Handle organizer names
        List<String> organizerNames = tournament.getOrganizerIds().stream()
                .map(userProfileService::getUserProfileByUsername)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(UserProfile::getUsername)
                .collect(Collectors.toList());
        response.put("organizerNames", organizerNames);

        return response;
    }

}
