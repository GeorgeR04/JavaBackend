package com.example.demo.controller.tournament;

import com.example.demo.data.tournament.Game;
import com.example.demo.data.tournament.Specialization;
import com.example.demo.data.user.UserProfile;
import com.example.demo.security.request.JwtUtil;
import com.example.demo.service.tournament.GameService;
import com.example.demo.service.tournament.SpecializationService;
import com.example.demo.service.user.UserProfileService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@RestController
@RequestMapping("/api/games")
public class GameController {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private GameService gameService;

    @Autowired
    private SpecializationService specializationService;

    @Autowired
    private UserProfileService userProfileService;

    @GetMapping
    public ResponseEntity<List<Game>> getAllGames() {
        List<Game> games = gameService.getAllGames();
        return ResponseEntity.ok(games);
    }

    @GetMapping("/list")
    public ResponseEntity<?> getAllGames(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid Authorization header.");
        }

        try {
            String token = authHeader.substring(7);
            String username = jwtUtil.extractUsername(token);

            UserProfile user = userProfileService.getUserProfileByUsername(username).orElse(null);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User not found.");
            }

            List<Game> games = gameService.getAllGames();
            return ResponseEntity.ok(games);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred.");
        }
    }

    @PostMapping("/create")
    public ResponseEntity<?> createGame(@RequestBody Game game, HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid Authorization header.");
        }

        try {
            String token = authHeader.substring(7);
            String username = jwtUtil.extractUsername(token);

            UserProfile user = userProfileService.getUserProfileByUsername(username).orElse(null);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User not found.");
            }

            if (!user.getRole().equalsIgnoreCase("organizer") && !user.getRole().equalsIgnoreCase("moderator")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not have permission to add games.");
            }

            // Validate required fields
            if (game.getName() == null || game.getName().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Game name is required.");
            }
            if (game.getType() == null || game.getType().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Game type is required.");
            }
            if (game.getYearOfExistence() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Year of existence is required.");
            }
            if (game.getPublisher() == null || game.getPublisher().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Publisher must be a valid string.");
            }
            if (game.getPlatforms() == null || game.getPlatforms().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("At least one platform must be specified.");
            }
            if (game.getDescription() != null && game.getDescription().length() > 1000) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Description must be under 1000 characters.");
            }

            // Rank-based game limit (unless moderator)
            int maxGames = getMaxGamesByRank(user.getRank(), user.getRole());
            if (maxGames != -1) { // If there's a limit
                long addedGamesCount = gameService.countGamesByOrganizer(user.getUserId());
                if (addedGamesCount >= maxGames) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body("You have reached the maximum number of games allowed for your rank.");
                }
            }

            game.setOrganizerId(user.getUserId().toString());
            // Save the game
            gameService.createGame(game, user.getUserId());
            return ResponseEntity.status(HttpStatus.CREATED).body("Game created successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }


    private int getMaxGamesByRank(String rank, String role) {
        if (role.equalsIgnoreCase("moderator")) {
            return -1; // No limit for moderators
        }

        if (rank == null) {
            throw new IllegalArgumentException("User rank cannot be null.");
        }

        switch (rank) {
            case "D": return 3;
            case "C": return 6;
            case "B": return 9;
            case "A": return 12;
            case "S": return -1; // No limit
            default: return 0;
        }
    }

    @GetMapping("/organizer/{organizerId}")
    public ResponseEntity<?> getGamesByOrganizer(@PathVariable String organizerId) {
        try {
            List<Game> games = gameService.getGamesByOrganizer(organizerId);
            return ResponseEntity.ok(games);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

    @PostMapping("/{gameId}/specializations")
    public ResponseEntity<?> addSpecialization(
            @PathVariable String gameId,
            @RequestBody Specialization specialization,
            HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid Authorization header.");
        }

        try {
            String token = authHeader.substring(7);
            String username = jwtUtil.extractUsername(token);

            UserProfile user = userProfileService.getUserProfileByUsername(username).orElse(null);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User not found.");
            }

            if (!user.getRole().equalsIgnoreCase("organizer") && !user.getRole().equalsIgnoreCase("moderator")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not have permission to add specializations.");
            }

            Game game = gameService.getGameById(gameId);
            if (game == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Game not found.");
            }

            specialization.setPreferredGames(List.of(gameId));
            specializationService.addSpecialization(specialization);
            return ResponseEntity.status(HttpStatus.CREATED).body("Specialization added successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }


    // Update a game
    @PutMapping("/{gameId}/update")
    public ResponseEntity<?> updateGame(@PathVariable String gameId, @RequestBody Game updatedGame, HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid Authorization header.");
        }

        try {
            String token = authHeader.substring(7);
            String username = jwtUtil.extractUsername(token);

            UserProfile user = userProfileService.getUserProfileByUsername(username).orElse(null);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User not found.");
            }

            Game existingGame = gameService.getGameById(gameId);
            if (existingGame == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Game not found.");
            }

            // Permission check: Organizer can only modify their own games, moderators can modify any game
            if (!user.getRole().equalsIgnoreCase("moderator") && !existingGame.getOrganizerId().equals(user.getUserId().toString())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not have permission to modify this game.");
            }

            // Update fields
            existingGame.setName(updatedGame.getName());
            existingGame.setType(updatedGame.getType());
            existingGame.setDescription(updatedGame.getDescription());
            existingGame.setYearOfExistence(updatedGame.getYearOfExistence());
            existingGame.setPublisher(updatedGame.getPublisher());
            existingGame.setPlatforms(updatedGame.getPlatforms());
            existingGame.setMaxPlayersPerTeam(updatedGame.getMaxPlayersPerTeam());

            gameService.updateGame(existingGame);
            return ResponseEntity.status(HttpStatus.OK).body("Game updated successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

    // Delete a game
    @DeleteMapping("/{gameId}/delete")
    public ResponseEntity<?> deleteGame(@PathVariable String gameId, HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid Authorization header.");
        }

        try {
            String token = authHeader.substring(7);
            String username = jwtUtil.extractUsername(token);

            UserProfile user = userProfileService.getUserProfileByUsername(username).orElse(null);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User not found.");
            }

            Game existingGame = gameService.getGameById(gameId);
            if (existingGame == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Game not found.");
            }

            // Permission check: Organizer can delete their own games, moderators can delete any game
            if (!user.getRole().equalsIgnoreCase("moderator") && !existingGame.getOrganizerId().equals(user.getUserId().toString())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not have permission to delete this game.");
            }

            gameService.deleteGame(gameId);
            return ResponseEntity.status(HttpStatus.OK).body("Game deleted successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

}
