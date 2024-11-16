package com.example.demo.controller;

import com.example.demo.data.Tournament;
import com.example.demo.data.UserProfile;
import com.example.demo.security.request.JwtUtil;
import com.example.demo.service.TeamService;
import com.example.demo.service.TournamentService;
import com.example.demo.service.UserProfileService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/tournaments")
public class TournamentController {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private TournamentService tournamentService;

    @Autowired
    private TeamService teamService;

    @Autowired
    private UserProfileService userProfileService;

    // Helper method to get the role from the user's profile
    private String getRoleFromToken(String token) {
        String username = jwtUtil.extractUsername(token);
        UserProfile userProfile = userProfileService.getUserProfileByUsername(username).orElse(null);
        return userProfile != null ? userProfile.getRole() : null;
    }

    // Fetch all tournaments
    @GetMapping
    public ResponseEntity<?> getAllTournaments(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid token.");
        }

        String token = authHeader.substring(7);
        String role = getRoleFromToken(token);

        if (role == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User role not found.");
        }

        // Allow all roles to fetch tournaments
        return ResponseEntity.ok(tournamentService.getAllTournaments());
    }

    // Create a tournament
    @PostMapping("/create")
    public ResponseEntity<?> createTournament(
            @RequestBody Map<String, Object> payload,
            HttpServletRequest request) {

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid token.");
        }

        String token = authHeader.substring(7);
        String username = jwtUtil.extractUsername(token);

        Optional<UserProfile> userProfileOpt = userProfileService.getUserProfileByUsername(username);
        if (userProfileOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User profile not found.");
        }

        UserProfile userProfile = userProfileOpt.get();
        if (!"organizer".equals(userProfile.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only organizers can create tournaments.");
        }

        try {
            Tournament tournament = new Tournament();
            tournament.setName((String) payload.get("name"));
            tournament.setDescription((String) payload.get("description"));
            tournament.setGameId((String) payload.get("gameId"));
            tournament.setRankRequirement(Integer.parseInt(payload.get("rankRequirement").toString()));
            tournament.setTrustFactorRequirement(Integer.parseInt(payload.get("trustFactorRequirement").toString()));
            tournament.setVisibility((String) payload.get("visibility"));

            // Decode the image from Base64
            String base64Image = (String) payload.get("image");
            tournament.setImage(base64Image != null ? Base64.getDecoder().decode(base64Image) : null);

            tournament.setOrganizerId(username);
            tournamentService.createTournament(tournament);

            return ResponseEntity.status(HttpStatus.CREATED).body(tournament);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating tournament.");
        }
    }


    @GetMapping("/{id}")
    public ResponseEntity<?> getTournament(@PathVariable String id) {
        Tournament tournament = tournamentService.getTournamentById(id);
        if (tournament == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Tournament not found.");
        }

        // Encode the image as Base64 for frontend
        Map<String, Object> response = new HashMap<>();
        response.put("id", tournament.getId());
        response.put("name", tournament.getName());
        response.put("description", tournament.getDescription());
        response.put("gameId", tournament.getGameId());
        response.put("rankRequirement", tournament.getRankRequirement());
        response.put("trustFactorRequirement", tournament.getTrustFactorRequirement());
        response.put("visibility", tournament.getVisibility());
        response.put("image", tournament.getImage() != null ? Base64.getEncoder().encodeToString(tournament.getImage()) : null);

        return ResponseEntity.ok(response);
    }




    // Update a tournament
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTournament(
            @PathVariable String id,
            @RequestBody Tournament tournament,
            HttpServletRequest request) {

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid token.");
        }

        String token = authHeader.substring(7);
        String username = jwtUtil.extractUsername(token);
        String role = getRoleFromToken(token);

        Tournament existingTournament = tournamentService.getTournamentById(id);
        if (existingTournament == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Tournament not found.");
        }

        if (!"organizer".equals(role) && !"moderator".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only organizers or moderators can modify this tournament.");
        }

        if ("organizer".equals(role) && !username.equals(existingTournament.getOrganizerId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You can only modify your own tournaments.");
        }

        if ("FINISHED".equals(existingTournament.getStatus())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Cannot modify a finished tournament.");
        }

        tournament.setId(id); // Preserve ID
        tournamentService.updateTournament(id, tournament);
        return ResponseEntity.ok(tournament);
    }

    // Delete a tournament
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTournament(
            @PathVariable String id,
            HttpServletRequest request) {

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid token.");
        }

        String token = authHeader.substring(7);
        String username = jwtUtil.extractUsername(token);
        String role = getRoleFromToken(token);

        Tournament tournament = tournamentService.getTournamentById(id);
        if (tournament == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Tournament not found.");
        }

        if ("organizer".equals(role) && !username.equals(tournament.getOrganizerId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You can only delete your own tournaments.");
        }

        if ("FINISHED".equals(tournament.getStatus())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Cannot delete a finished tournament.");
        }

        tournamentService.deleteTournament(id);
        return ResponseEntity.ok("Tournament deleted successfully.");
    }

    // Finish a tournament
    @PutMapping("/{id}/finish")
    public ResponseEntity<?> finishTournament(
            @PathVariable String id,
            HttpServletRequest request) {

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid token.");
        }

        String token = authHeader.substring(7);
        String username = jwtUtil.extractUsername(token);
        String role = getRoleFromToken(token);

        Tournament tournament = tournamentService.getTournamentById(id);
        if (tournament == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Tournament not found.");
        }

        if (!"organizer".equals(role) && !"moderator".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only organizers or moderators can finish this tournament.");
        }

        if ("organizer".equals(role) && !username.equals(tournament.getOrganizerId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You can only finish your own tournaments.");
        }

        tournamentService.finishTournament(id);
        return ResponseEntity.ok("Tournament finished successfully.");
    }

    // Join a tournament
    @PostMapping("/{id}/join")
    public ResponseEntity<?> joinTournament(
            @PathVariable String id,
            @RequestBody Map<String, Object> payload,
            HttpServletRequest request) {

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid token.");
        }

        String token = authHeader.substring(7);
        String username = jwtUtil.extractUsername(token);

        Tournament tournament = tournamentService.getTournamentById(id);
        if (tournament == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Tournament not found.");
        }

        String type = tournament.getType(); // "solo" or "team"

        if ("team".equals(type)) {
            String teamId = (String) payload.get("teamId");
            if (!teamService.isTeamLeader(username, teamId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only team leaders can join team tournaments.");
            }

            boolean added = tournamentService.addParticipant(tournament.getId(), teamId);
            if (added) {
                return ResponseEntity.ok("Team joined successfully.");
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Tournament may be full.");
            }
        } else if ("solo".equals(type)) {
            boolean added = tournamentService.addParticipant(tournament.getId(), username);
            if (added) {
                return ResponseEntity.ok("Joined successfully.");
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Tournament may be full.");
            }
        }

        return ResponseEntity.badRequest().body("Invalid tournament type.");
    }

    // Leave a tournament
    @PostMapping("/{id}/leave")
    public ResponseEntity<?> leaveTournament(
            @PathVariable String id,
            @RequestBody Map<String, Object> payload,
            HttpServletRequest request) {

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid token.");
        }

        String token = authHeader.substring(7);
        String username = jwtUtil.extractUsername(token);

        Tournament tournament = tournamentService.getTournamentById(id);
        if (tournament == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Tournament not found.");
        }

        String type = tournament.getType(); // "solo" or "team"

        if ("team".equals(type)) {
            String teamId = (String) payload.get("teamId");
            if (!teamService.isTeamLeader(username, teamId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only team leaders can leave team tournaments.");
            }

            boolean removed = tournamentService.removeParticipant(tournament.getId(), teamId);
            if (removed) {
                return ResponseEntity.ok("Team left successfully.");
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to leave the tournament.");
            }
        } else if ("solo".equals(type)) {
            boolean removed = tournamentService.removeParticipant(tournament.getId(), username);
            if (removed) {
                return ResponseEntity.ok("Left successfully.");
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to leave the tournament.");
            }
        }

        return ResponseEntity.badRequest().body("Invalid tournament type.");
    }
}
