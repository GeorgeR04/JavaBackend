package com.example.demo.controller.match;
import com.example.demo.data.match.Round;
import com.example.demo.data.match.Match;
import com.example.demo.data.tournament.Tournament;
import com.example.demo.security.request.JwtUtil;
import com.example.demo.service.match.MatchService;
import com.example.demo.service.match.RoundService;
import com.example.demo.service.tournament.TournamentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

@RestController
@RequestMapping("/api/playoffs")
public class PlayoffController {

    private final TournamentService tournamentService;
    private final RoundService roundService;
    private final MatchService matchService;
    private final JwtUtil jwtUtil;

    public PlayoffController(
            TournamentService tournamentService,
            RoundService roundService,
            MatchService matchService,
            JwtUtil jwtUtil) {
        this.tournamentService = tournamentService;
        this.roundService = roundService;
        this.matchService = matchService;
        this.jwtUtil = jwtUtil;
    }

    // Helper method to validate user role
    private boolean isAuthorizedToModify(String token, Tournament tournament) {
        String username = jwtUtil.extractUsername(token);
        String role = tournamentService.getRoleFromToken(token);

        // Moderators can modify any tournament
        if ("moderator".equalsIgnoreCase(role)) {
            return true;
        }

        // Only the creator organizer can modify the tournament
        return "organizer".equalsIgnoreCase(role) && tournament.getOrganizerIds().contains(username);
    }

    /**
     * Generate playoff brackets for a tournament
     */
    @PostMapping("/{tournamentId}/generate")
    public ResponseEntity<?> generatePlayoffBracket(
            @PathVariable String tournamentId,
            HttpServletRequest request) {

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid token.");
        }

        String token = authHeader.substring(7);
        Tournament tournament = tournamentService.getTournamentById(tournamentId);

        if (tournament == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Tournament not found.");
        }

        if (!isAuthorizedToModify(token, tournament)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not authorized to modify this tournament.");
        }

        if (!"ONGOING".equalsIgnoreCase(tournament.getStatus())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Playoff brackets can only be generated for ongoing tournaments.");
        }

        List<Round> rounds = roundService.generatePlayoffBracket(tournamentId);
        if (rounds == null || rounds.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to generate playoff brackets.");
        }

        return ResponseEntity.ok(rounds);
    }

    /**
     * Get all rounds for a tournament
     */
    @GetMapping("/{tournamentId}/rounds")
    public ResponseEntity<?> getRounds(@PathVariable String tournamentId) {
        List<Round> rounds = roundService.getRoundsByTournamentId(tournamentId);

        if (rounds == null || rounds.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No rounds found for this tournament.");
        }

        return ResponseEntity.ok(rounds);
    }

    /**
     * Get all matches for a specific round
     */
    @GetMapping("/rounds/{roundId}/matches")
    public ResponseEntity<?> getMatchesByRound(@PathVariable String roundId) {
        List<Match> matches = matchService.getMatchesByRoundId(roundId);

        if (matches == null || matches.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No matches found for this round.");
        }

        return ResponseEntity.ok(matches);
    }

    /**
     * Assign a winner to a match
     */
    @PostMapping("/matches/{matchId}/winner")
    public ResponseEntity<?> setMatchWinner(
            @PathVariable String matchId,
            @RequestBody String winnerId,
            HttpServletRequest request) {

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid token.");
        }

        String token = authHeader.substring(7);
        Match match = matchService.getMatchById(matchId);

        if (match == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Match not found.");
        }

        Tournament tournament = tournamentService.getTournamentById(match.getTournamentId());
        if (!isAuthorizedToModify(token, tournament)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not authorized to modify this tournament.");
        }

        if (winnerId == null || winnerId.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Winner ID must be provided.");
        }

        boolean updated = matchService.setMatchWinner(matchId, winnerId);
        if (!updated) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to assign winner.");
        }

        // Process the next round
        roundService.processNextRound(tournament.getId());

        return ResponseEntity.ok("Winner assigned successfully!");
    }

    /**
     * View the entire playoff structure
     */
    @GetMapping("/{tournamentId}/structure")
    public ResponseEntity<?> viewPlayoffStructure(@PathVariable String tournamentId) {
        List<Round> rounds = roundService.getRoundsByTournamentId(tournamentId);

        if (rounds == null || rounds.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Playoff structure not available.");
        }

        return ResponseEntity.ok(rounds);
    }
}
