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
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

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

    @Autowired
    private GameService gameService;

    @Autowired
    private RoundService roundService;

    @Autowired
    private MatchService matchService;



    // Helper method to get the role from the user's profile
    private String getRoleFromToken(String token) {
        String username = jwtUtil.extractUsername(token);
        UserProfile userProfile = userProfileService.getUserProfileByUsername(username).orElse(null);
        return userProfile != null ? userProfile.getRole() : null;
    }


    // Fetch all tournaments
    @GetMapping("/list")
    public ResponseEntity<?> getAllTournaments() {
        List<Tournament> tournaments = tournamentService.getAllTournaments();

        List<Map<String, Object>> response = tournaments.stream().map(tournament -> {
            Map<String, Object> tournamentData = new HashMap<>();
            tournamentData.put("id", tournament.getId());
            tournamentData.put("name", tournament.getName());
            tournamentData.put("description", tournament.getDescription());
            tournamentData.put("type", tournament.getType());
            tournamentData.put("minRankRequirement", tournament.getMinRankRequirement());
            tournamentData.put("maxRankRequirement", tournament.getMaxRankRequirement());
            tournamentData.put("trustFactorRequirement", tournament.getTrustFactorRequirement());
            tournamentData.put("cashPrize", tournament.getCashPrize());
            tournamentData.put("reputation", tournament.getReputation());
            tournamentData.put("rank", tournament.getRank());
            tournamentData.put("status", tournament.getStatus());
            tournamentData.put("visibility", tournament.getVisibility());
            tournamentData.put("maxTeams", tournament.getMaxTeams());
            tournamentData.put("victory", tournament.getVictory());
            tournamentData.put("rule", tournament.getRule());
            // Encode the image as Base64
            if (tournament.getImage() != null) {
                tournamentData.put("image", Base64.getEncoder().encodeToString(tournament.getImage()));
            } else {
                tournamentData.put("image", null);
            }

            // Fetch game details
            Game game = gameService.getGameById(tournament.getGameId());
            tournamentData.put("gameName", game != null ? game.getName() : "Unknown Game");

            // Fetch organizer names instead of IDs
            List<String> organizerIds = tournament.getOrganizerIds();
            List<String> organizerNames = organizerIds.stream()
                    .map(userProfileService::getUserProfileByUsername) // Fetch UserProfile for each ID
                    .filter(Optional::isPresent) // Filter out missing profiles
                    .map(Optional::get) // Unwrap Optional
                    .map(UserProfile::getUsername) // Get the username (or any other identifying field)
                    .collect(Collectors.toList());
            tournamentData.put("organizerNames", organizerNames);

            return tournamentData;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(response);
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
            System.out.println("Payload: " + payload); // Debug incoming payload

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
            Integer minRank = payload.get("minRankRequirement") != null
                    ? Integer.parseInt(payload.get("minRankRequirement").toString())
                    : 0;
            Integer maxRank = payload.get("maxRankRequirement") != null
                    ? Integer.parseInt(payload.get("maxRankRequirement").toString())
                    : 0;

            if (minRank != null && maxRank != null && minRank > maxRank) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Minimum rank cannot be greater than maximum rank.");
            }

            tournament.setMinRankRequirement(minRank);
            tournament.setMaxRankRequirement(maxRank);

            // Handle trust factor
            Integer trustFactor = payload.get("trustFactorRequirement") != null
                    ? Integer.parseInt(payload.get("trustFactorRequirement").toString())
                    : 0;
            tournament.setTrustFactorRequirement(trustFactor);

            // Handle maxTeams
            Integer maxTeams = payload.get("maxTeams") != null
                    ? Integer.parseInt(payload.get("maxTeams").toString())
                    : 0; // Default to 0 for solo tournaments
            tournament.setMaxTeams(maxTeams);

            tournament.setCashPrize(Double.parseDouble(payload.get("cashPrize").toString()));
            tournament.setOrganizerIds(List.of(username)); // Add the creator as the initial organizer

            tournamentService.createTournament(tournament);

            return ResponseEntity.status(HttpStatus.CREATED).body(tournament);
        } catch (Exception e) {
            e.printStackTrace(); // Log the exception
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating tournament.");
        }
    }



    // Fetch a single tournament
    @GetMapping("/{id}")
    public ResponseEntity<?> getTournament(@PathVariable String id) {
        Tournament tournament = tournamentService.getTournamentById(id);
        if (tournament == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Tournament not found.");
        }

        // Check if the tournament is public
        if (!"PUBLIC".equalsIgnoreCase(tournament.getVisibility())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("This tournament is not publicly accessible.");
        }

        // Encode the image as Base64 for the frontend
        Map<String, Object> response = new HashMap<>();
        response.put("id", tournament.getId());
        response.put("name", tournament.getName());
        response.put("description", tournament.getDescription());
        response.put("gameId", tournament.getGameId());
        response.put("gameName", gameService.getGameById(tournament.getGameId()) != null ?
                gameService.getGameById(tournament.getGameId()).getName() : "Unknown Game");
        response.put("minRankRequirement", tournament.getMinRankRequirement());
        response.put("maxRankRequirement", tournament.getMaxRankRequirement());
        response.put("trustFactorRequirement", tournament.getTrustFactorRequirement());
        response.put("visibility", tournament.getVisibility());
        response.put("cashPrize", tournament.getCashPrize());
        response.put("rank", tournament.getRank());
        response.put("status", tournament.getStatus());
        response.put("startDate", tournament.getStartDate());
        response.put("finishDate", tournament.getFinishDate());
        response.put("image", tournament.getImage() != null ? Base64.getEncoder().encodeToString(tournament.getImage()) : null);
        response.put("reputation", tournament.getReputation());
        response.put("rank", tournament.getRank());
        response.put("maxTeams", tournament.getMaxTeams());
        response.put("rule", tournament.getRule());

        // Fetch organizer names instead of IDs
        List<String> organizerIds = tournament.getOrganizerIds();
        List<String> organizerNames = organizerIds.stream()
                .map(userProfileService::getUserProfileByUsername) // Fetch UserProfile for each ID
                .filter(Optional::isPresent) // Filter out missing profiles
                .map(Optional::get) // Unwrap Optional
                .map(UserProfile::getUsername) // Get the username (or any other identifying field)
                .collect(Collectors.toList());
        response.put("organizerNames", organizerNames);


        // Include MVP info only if the tournament is finished
        if ("FINISHED".equals(tournament.getStatus())) {
            response.put("mvpPlayerId", tournament.getMvpPlayerId());
            response.put("victory",tournament.getVictory());
        }

        return ResponseEntity.ok(response);
    }


    // Update a tournament
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTournament(
            @PathVariable String id,
            @RequestBody Map<String, Object> payload,
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

            // Restrict access to organizers or moderators
            if (!"organizer".equals(role) && !"moderator".equals(role)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only organizers or moderators can modify this tournament.");
            }

            // Ensure that only the tournament creator can update it
            if ("organizer".equals(role) && !existingTournament.getOrganizerIds().contains(username)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You can only modify tournaments you organize.");
            }

            // Update tournament details
            try {
                if (payload.containsKey("name")) {
                    existingTournament.setName((String) payload.get("name"));
                }
                if (payload.containsKey("description")) {
                    existingTournament.setDescription((String) payload.get("description"));
                }
                if (payload.containsKey("gameId")) {
                    existingTournament.setGameId((String) payload.get("gameId"));
                }
                if (payload.containsKey("visibility")) {
                    existingTournament.setVisibility((String) payload.get("visibility"));
                }
                if (payload.containsKey("type")) {
                    String type = (String) payload.get("type");
                    if (!type.equalsIgnoreCase("solo") && !type.equalsIgnoreCase("team")) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid tournament type.");
                    }
                    existingTournament.setType(type);
                }
                if (payload.containsKey("minRankRequirement")) {
                    existingTournament.setMinRankRequirement((Integer) payload.get("minRankRequirement"));
                }
                if (payload.containsKey("maxRankRequirement")) {
                    existingTournament.setMaxRankRequirement((Integer) payload.get("maxRankRequirement"));
                }
                if (payload.containsKey("trustFactorRequirement")) {
                    existingTournament.setTrustFactorRequirement((Integer) payload.get("trustFactorRequirement"));
                }
                if (payload.containsKey("cashPrize")) {
                    existingTournament.setCashPrize(Double.parseDouble(payload.get("cashPrize").toString()));
                }

                // Update image if provided
                if (payload.containsKey("image")) {
                    String base64Image = (String) payload.get("image");
                    if (base64Image != null) {
                        existingTournament.setImage(Base64.getDecoder().decode(base64Image));
                    }
                }

                // Save updates
                tournamentService.updateTournament(id, existingTournament);
                return ResponseEntity.ok(existingTournament);

            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating tournament.");
            }
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

        if ("organizer".equals(role) && !tournament.getOrganizerIds().contains(username)) {
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

        // Only organizers or moderators can finish a tournament
        if (!"organizer".equals(role) && !"moderator".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only organizers or moderators can finish this tournament.");
        }

        // Ensure that only the organizer or a moderator can finish it
        if ("organizer".equals(role) && !tournament.getOrganizerIds().contains(username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You can only finish tournaments you organize.");
        }

        // Check if the tournament is already finished
        if ("FINISHED".equals(tournament.getStatus())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Tournament is already finished.");
        }

        try {
            tournamentService.finishTournament(id);
            return ResponseEntity.ok("Tournament finished successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error finishing tournament.");
        }
    }


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
