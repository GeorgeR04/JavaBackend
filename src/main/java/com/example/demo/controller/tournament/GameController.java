package com.example.demo.controller.tournament;

import com.example.demo.data.tournament.Game;
import com.example.demo.data.user.UserProfile;
import com.example.demo.security.request.JwtUtil;
import com.example.demo.service.tournament.GameService;
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


            // Fetch games
            List<Game> games = gameService.getAllGames();
            System.out.println("Fetched games: " + games);
            return ResponseEntity.ok(games);

        } catch (Exception e) {
            System.out.println("Error processing request: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred.");
        }
    }

}

