package com.example.demo.service.match;

import com.example.demo.data.match.Match;
import com.example.demo.repository.mongoDB.match.MatchRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MatchService {

    private final MatchRepository matchRepository;

    public MatchService(MatchRepository matchRepository) {
        this.matchRepository = matchRepository;
    }

    // Get all matches
    public List<Match> getAllMatches() {
        return matchRepository.findAll();
    }

    // Get a specific match by ID
    public Match getMatchById(String id) {
        return matchRepository.findById(id);
    }

    // Get matches by round ID
    public List<Match> getMatchesByRoundId(String roundId) {
        return matchRepository.findByRoundId(roundId);
    }

    // Get matches by tournament ID
    public List<Match> getMatchesByTournamentId(String tournamentId) {
        return matchRepository.findByTournamentId(tournamentId);
    }

    // Create a match
    public Match createMatch(Match match) {
        return matchRepository.save(match);
    }

    // Update a match
    public boolean updateMatch(String id, Match match) {
        Match existingMatch = matchRepository.findById(id);
        if (existingMatch != null) {
            match.setId(id);
            matchRepository.save(match);
            return true;
        }
        return false;
    }

    // Delete a match by ID
    public boolean deleteMatch(String id) {
        Match existingMatch = matchRepository.findById(id);
        if (existingMatch != null) {
            matchRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // Assign a winner to a match
    public boolean setMatchWinner(String matchId, String winnerId) {
        Match match = matchRepository.findById(matchId);
        if (match != null) {
            match.setWinnerId(winnerId);
            matchRepository.save(match);
            return true;
        }
        return false;
    }
}
