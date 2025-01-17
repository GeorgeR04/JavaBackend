package com.example.demo.controller.tournament;

import com.example.demo.data.tournament.Game;
import com.example.demo.data.tournament.Specialization;
import com.example.demo.data.user.UserProfile;
import com.example.demo.security.request.JwtUtil;
import com.example.demo.service.tournament.GameService;
import com.example.demo.service.tournament.SpecializationService;
import com.example.demo.service.user.UserProfileService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.function.Function;

@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@RestController
@RequestMapping("/api/games")
public class GameController {

    private final JwtUtil jwtUtil;
    private final GameService gameService;
    private final SpecializationService specializationService;
    private final UserProfileService userProfileService;

    public GameController(JwtUtil jwtUtil, GameService gameService,
                          SpecializationService specializationService,
                          UserProfileService userProfileService) {
        this.jwtUtil = jwtUtil;
        this.gameService = gameService;
        this.specializationService = specializationService;
        this.userProfileService = userProfileService;
    }
    @GetMapping("/list")
    public ResponseEntity<List<Game>> getAllGames(HttpServletRequest request) {
        String username = extractUsernameFromRequest(request);
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return userProfileService.getUserProfileByUsername(username)
                .map(user -> ResponseEntity.ok(gameService.getAllGames()))
                .orElse(ResponseEntity.status(HttpStatus.FORBIDDEN).body(List.of())); // Return an empty list if the user is not found
    }

    @PostMapping("/create")
    public ResponseEntity<?> createGame(@RequestBody Game game, HttpServletRequest request) {
        String username = extractUsernameFromRequest(request);
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Missing or invalid Authorization header.");
        }

        return userProfileService.getUserProfileByUsername(username)
                .map(user -> {
                    if (!isOrganizerOrModerator(user.getRole())) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body("You do not have permission to add games.");
                    }

                    String validationError = validateGamePayload(game);
                    if (validationError != null) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validationError);
                    }

                    if (!canCreateMoreGames(user)) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body("You have reached the maximum number of games allowed for your rank.");
                    }

                    game.setOrganizerId(user.getUserId().toString());
                    gameService.createGame(game, user.getUserId());
                    return ResponseEntity.status(HttpStatus.CREATED).body("Game created successfully.");
                })
                .orElse(ResponseEntity.status(HttpStatus.FORBIDDEN).body("User not found."));
    }

    @GetMapping("/organizer/{organizerId}")
    public ResponseEntity<?> getGamesByOrganizer(@PathVariable String organizerId) {
        try {
            List<Game> games = gameService.getGamesByOrganizer(organizerId);
            return ResponseEntity.ok(games);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred: " + e.getMessage());
        }
    }

    @PostMapping("/{gameId}/specializations")
    public ResponseEntity<?> addSpecialization(
            @PathVariable String gameId,
            @RequestBody Specialization specialization,
            HttpServletRequest request) {
        return processAuthorizedRequest(request, user -> {
            if (!isOrganizerOrModerator(user.getRole())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("You do not have permission to add specializations.");
            }

            return gameService.getGameById(gameId)
                    .map(game -> {
                        specialization.setPreferredGames(List.of(gameId));
                        specializationService.addSpecialization(specialization);
                        return ResponseEntity.status(HttpStatus.CREATED).body("Specialization added successfully.");
                    })
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body("Game not found."));
        });
    }

    @PutMapping("/{gameId}/update")
    public ResponseEntity<?> updateGame(
            @PathVariable String gameId,
            @RequestBody Game updatedGame,
            HttpServletRequest request) {
        return processAuthorizedRequest(request, user -> {
            return gameService.getGameById(gameId)
                    .map(existingGame -> {
                        if (!canModifyGame(user, existingGame)) {
                            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                    .body("You do not have permission to modify this game.");
                        }

                        updateGameFields(existingGame, updatedGame);
                        gameService.updateGame(existingGame);
                        return ResponseEntity.ok("Game updated successfully.");
                    })
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body("Game not found."));
        });
    }

    @DeleteMapping("/{gameId}/delete")
    public ResponseEntity<?> deleteGame(
            @PathVariable String gameId,
            HttpServletRequest request) {
        return processAuthorizedRequest(request, user -> {
            return gameService.getGameById(gameId)
                    .map(existingGame -> {
                        if (!canModifyGame(user, existingGame)) {
                            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                    .body("You do not have permission to delete this game.");
                        }
                        gameService.deleteGame(gameId);
                        return ResponseEntity.ok("Game deleted successfully.");
                    })
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body("Game not found."));
        });
    }

    // Helper Methods

    private boolean canCreateMoreGames(UserProfile user) {
        int maxGames = getMaxGamesByRank(user.getRank(), user.getRole());
        if (maxGames == -1) return true; // No limit
        long addedGamesCount = gameService.countGamesByOrganizer(user.getUserId());
        return addedGamesCount < maxGames;
    }

    private String validateGamePayload(Game game) {
        if (game.getName() == null || game.getName().trim().isEmpty()) return "Game name is required.";
        if (game.getType() == null || game.getType().trim().isEmpty()) return "Game type is required.";
        if (game.getYearOfExistence() == null) return "Year of existence is required.";
        if (game.getPublisher() == null || game.getPublisher().trim().isEmpty()) return "Publisher must be valid.";
        if (game.getPlatforms() == null || game.getPlatforms().isEmpty()) return "At least one platform must be specified.";
        if (game.getDescription() != null && game.getDescription().length() > 1000) return "Description must be under 1000 characters.";
        return null; // No errors
    }

    private int getMaxGamesByRank(String rank, String role) {
        if ("moderator".equalsIgnoreCase(role)) return -1; // No limit
        if (rank == null) throw new IllegalArgumentException("User rank cannot be null.");

        return switch (rank) {
            case "D" -> 3;
            case "C" -> 6;
            case "B" -> 9;
            case "A" -> 12;
            case "S" -> -1; // No limit
            default -> 0;
        };
    }

    private boolean isOrganizerOrModerator(String role) {
        return "organizer".equalsIgnoreCase(role) || "moderator".equalsIgnoreCase(role);
    }

    private boolean canModifyGame(UserProfile user, Game game) {
        return "moderator".equalsIgnoreCase(user.getRole())
                || game.getOrganizerId().equals(user.getUserId().toString());
    }

    private void updateGameFields(Game existingGame, Game updatedGame) {
        existingGame.setName(updatedGame.getName());
        existingGame.setType(updatedGame.getType());
        existingGame.setDescription(updatedGame.getDescription());
        existingGame.setYearOfExistence(updatedGame.getYearOfExistence());
        existingGame.setPublisher(updatedGame.getPublisher());
        existingGame.setPlatforms(updatedGame.getPlatforms());
        existingGame.setMaxPlayersPerTeam(updatedGame.getMaxPlayersPerTeam());
    }
    private ResponseEntity<?> processAuthorizedRequest(HttpServletRequest request,
                                                       Function<UserProfile, ResponseEntity<?>> handler) {
        String username = extractUsernameFromRequest(request);
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Missing or invalid Authorization header.");
        }

        return userProfileService.getUserProfileByUsername(username)
                .map(handler)
                .orElse(ResponseEntity.status(HttpStatus.FORBIDDEN).body("User not found."));
    }

    private String extractUsernameFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return jwtUtil.extractUsername(authHeader.substring(7));
        }
        return null;
    }

}

