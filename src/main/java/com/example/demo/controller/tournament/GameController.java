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
        System.out.println("Incoming request URL: " + request.getRequestURL());

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("Authorization header missing or invalid.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid Authorization header.");
        }

        try {
            String token = authHeader.substring(7);
            String username = jwtUtil.extractUsername(token);
            System.out.println("Extracted username: " + username);

            UserProfile user = userProfileService.getUserProfileByUsername(username).orElse(null);
            if (user == null) {
                System.out.println("User not found for username: " + username);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User not found.");
            }

            List<Game> games = gameService.getAllGames();
            System.out.println("Fetched games: " + games);
            return ResponseEntity.ok(games);

        } catch (Exception e) {
            System.out.println("Error processing request: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred.");
        }
    }

    // Create a new game
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

            long addedGamesCount = gameService.countGamesByOrganizer(user.getUserId());
            int maxGames = getMaxGamesByRank(user.getRank());
            if (maxGames != -1 && addedGamesCount >= maxGames) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("You have reached the maximum number of games allowed for your rank.");
            }

            gameService.createGame(game, user.getUserId());
            return ResponseEntity.status(HttpStatus.CREATED).body("Game created successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

    // Add a specialization to an existing game
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
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }


    private int getMaxGamesByRank(String rank) {
        switch (rank) {
            case "D": return 3;
            case "C": return 6;
            case "B": return 9;
            case "A": return 12;
            case "S": return -1; // No limit
            default: return 0;
        }
    }
}
