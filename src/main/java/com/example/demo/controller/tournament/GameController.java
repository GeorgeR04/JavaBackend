package com.example.demo.controller.tournament;

import com.example.demo.data.tournament.Game;
import com.example.demo.data.tournament.GameReview;
import com.example.demo.data.tournament.Specialization;
import com.example.demo.data.user.UserProfile;
import com.example.demo.data.dto.ReviewDTO;
import com.example.demo.security.request.JwtUtil;
import com.example.demo.service.tournament.GameReviewService;
import com.example.demo.service.tournament.GameService;
import com.example.demo.service.tournament.SpecializationService;
import com.example.demo.service.user.UserProfileService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/games")
public class GameController {

    private final JwtUtil jwtUtil;
    private final GameService gameService;
    private final SpecializationService specializationService;
    private final UserProfileService userProfileService;
    private final GameReviewService gameReviewService;

    public GameController(JwtUtil jwtUtil, GameService gameService,
                          SpecializationService specializationService,
                          UserProfileService userProfileService,
                          GameReviewService gameReviewService) {
        this.jwtUtil = jwtUtil;
        this.gameService = gameService;
        this.specializationService = specializationService;
        this.userProfileService = userProfileService;
        this.gameReviewService = gameReviewService;
    }

    // Get all games (listing all games, including those created directly or via suggestions)
    @GetMapping("/list")
    public ResponseEntity<List<Game>> getAllGames() {
        List<Game> games = gameService.getAllGames();
        return ResponseEntity.ok(games);
    }

    @GetMapping("/{gameId}")
    public ResponseEntity<Game> getGameById(@PathVariable String gameId) {
        Optional<Game> game = gameService.getGameById(gameId);
        if (game.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.ok(game.get());
    }

    @GetMapping("/popular")
    public ResponseEntity<List<Game>> getPopularGames() {
        List<Game> popularGames = gameService.getPopularGames();
        return ResponseEntity.ok(popularGames);
    }

    // Endpoint for organizers or moderators to create a game (auto-approved)
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
                                .body("You do not have permission to add games directly.");
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
                    // For direct creation, the game is auto-approved.
                    game.setApproved(true);
                    gameService.createGame(game, user.getUserId());
                    return ResponseEntity.status(HttpStatus.CREATED).body("Game created successfully.");
                })
                .orElse(ResponseEntity.status(HttpStatus.FORBIDDEN).body("User not found."));
    }

    // New endpoint for users to suggest a game (pending approval)
    // Only players can suggest a game; organisers must create games directly.
    @PostMapping("/suggest")
    public ResponseEntity<?> suggestGame(@RequestBody Game game, HttpServletRequest request) {
        String username = extractUsernameFromRequest(request);
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Missing or invalid Authorization header.");
        }
        return userProfileService.getUserProfileByUsername(username)
                .map(user -> {
                    // Only players are allowed to suggest games
                    if (!"player".equalsIgnoreCase(user.getRole())) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body("Only players can suggest new games. Organisers should use the game creation feature.");
                    }

                    String validationError = validateGamePayload(game);
                    if (validationError != null) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validationError);
                    }
                    // Mark the game as pending approval.
                    game.setApproved(false);
                    game.setOrganizerId(user.getUserId().toString());
                    gameService.createGame(game, user.getUserId());

                    // TODO: Integrate email notification logic here.
                    // In the future, when a suggestion is submitted,
                    // send an email to all organisers notifying them of the new game suggestion.

                    return ResponseEntity.status(HttpStatus.CREATED).body("Game suggestion submitted for review.");
                })
                .orElse(ResponseEntity.status(HttpStatus.FORBIDDEN).body("User not found."));
    }

    // New endpoint for moderators/organizers to approve a suggested game.
    @PutMapping("/{gameId}/approve")
    public ResponseEntity<?> approveGame(@PathVariable String gameId, HttpServletRequest request) {
        return processAuthorizedRequest(request, user -> {
            if (!isOrganizerOrModerator(user.getRole())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("You do not have permission to approve games.");
            }
            return gameService.getGameById(gameId)
                    .map(game -> {
                        game.setApproved(true);
                        gameService.updateGame(game);
                        return ResponseEntity.ok("Game approved successfully.");
                    })
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body("Game not found."));
        });
    }

    // New endpoint to retrieve the tutorial for a game.
    @GetMapping("/{gameId}/tutorial")
    public ResponseEntity<?> getGameTutorial(@PathVariable String gameId) {
        Optional<Game> gameOpt = gameService.getGameById(gameId);
        if (gameOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Game not found.");
        }
        String tutorial = gameOpt.get().getTutorial();
        if (tutorial == null || tutorial.trim().isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Tutorial not available.");
        }
        return ResponseEntity.ok(tutorial);
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

    // -------------------- Review Endpoints --------------------

    // Retrieve enriched reviews for a game.
    @GetMapping("/{gameId}/reviews")
    public ResponseEntity<?> getGameReviews(@PathVariable String gameId) {
        List<GameReview> reviews = gameReviewService.getReviewsByGameId(gameId);
        List<ReviewDTO> enrichedReviews = reviews.stream().map(review -> {
            UserProfile user = userProfileService.getUserProfileById(review.getUserId());
            return new ReviewDTO(review, user);
        }).collect(Collectors.toList());
        return ResponseEntity.ok(enrichedReviews);
    }

    // Add a review for a game.
    @PostMapping("/{gameId}/reviews")
    public ResponseEntity<?> addGameReview(@PathVariable String gameId, @RequestBody GameReview review, HttpServletRequest request) {
        String username = extractUsernameFromRequest(request);
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Missing or invalid Authorization header.");
        }
        Optional<Game> gameOptional = gameService.getGameById(gameId);
        if (gameOptional.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Game not found.");
        }
        Game game = gameOptional.get();
        return userProfileService.getUserProfileByUsername(username)
                .map(user -> {
                    if ("player".equalsIgnoreCase(user.getRole())) {
                        if (user.getGame() == null || !user.getGame().equalsIgnoreCase(game.getName())) {
                            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                    .body("You are not allowed to review this game.");
                        }
                    }
                    // Prevent multiple reviews from the same user.
                    List<GameReview> existingReviews = gameReviewService.getReviewsByGameId(gameId);
                    boolean alreadyReviewed = existingReviews.stream()
                            .anyMatch(r -> r.getUserId().equals(user.getUserId().toString()));
                    if (alreadyReviewed) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body("You have already submitted a review for this game.");
                    }
                    review.setUserId(user.getUserId().toString());
                    review.setGameId(gameId);
                    gameReviewService.addReview(review);
                    return ResponseEntity.status(HttpStatus.CREATED).body("Review added successfully.");
                })
                .orElse(ResponseEntity.status(HttpStatus.FORBIDDEN).body("User not found."));
    }

    // Delete a review.
    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<?> deleteReview(@PathVariable String reviewId, HttpServletRequest request) {
        String username = extractUsernameFromRequest(request);
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        Optional<GameReview> reviewOpt = gameReviewService.getReviewById(reviewId);
        if (reviewOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Review not found");
        }
        GameReview review = reviewOpt.get();
        Optional<UserProfile> userOpt = userProfileService.getUserProfileByUsername(username);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User not found");
        }
        UserProfile user = userOpt.get();
        if (user.getUserId().toString().equals(review.getUserId()) || "moderator".equalsIgnoreCase(user.getRole())) {
            gameReviewService.deleteReview(reviewId);
            return ResponseEntity.ok("Review deleted successfully");
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not allowed to delete this review");
    }

    // -------------------- Helper Methods --------------------

    private boolean canCreateMoreGames(UserProfile user) {
        int maxGames = getMaxGamesByRank(user.getRank(), user.getRole());
        if (maxGames == -1) return true;
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
        return null;
    }

    private int getMaxGamesByRank(String rank, String role) {
        if ("moderator".equalsIgnoreCase(role)) return -1;
        if (rank == null) throw new IllegalArgumentException("User rank cannot be null.");
        return switch (rank) {
            case "D" -> 3;
            case "C" -> 6;
            case "B" -> 9;
            case "A" -> 12;
            case "S" -> -1;
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
        existingGame.setRules(updatedGame.getRules());
        existingGame.setTutorial(updatedGame.getTutorial());
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
