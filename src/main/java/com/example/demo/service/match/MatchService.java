package com.example.demo.service.match;

import com.example.demo.data.match.Match;
import com.example.demo.repository.mongoDB.match.MatchsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MatchService {

    private final MatchsRepository matchsRepository;

    // Fetch match by ID
    public Optional<Match> getMatchById(String id) {
        return matchsRepository.findById(id);
    }

    // Fetch matches by round ID
    public List<Match> getMatchesByRoundId(String roundId) {
        return matchsRepository.findByRoundId(roundId);
    }

    // Fetch matches by tournament ID
    public List<Match> getMatchesByTournamentId(String tournamentId) {
        return matchsRepository.findByTournamentId(tournamentId);
    }

    // Create a new match
    public Match createMatch(Match match) {
        return matchsRepository.save(match);
    }

    // Update an existing match
    public boolean updateMatch(String id, Match match) {
        return matchsRepository.findById(id)
                .map(existingMatch -> {
                    match.setId(id); // Ensure ID consistency
                    matchsRepository.save(match);
                    return true;
                })
                .orElse(false);
    }

    // Delete a match by ID
    public boolean deleteMatch(String id) {
        return matchsRepository.findById(id)
                .map(match -> {
                    matchsRepository.deleteById(id);
                    return true;
                })
                .orElse(false);
    }

    // Set the winner of a match
    public boolean setMatchWinner(String matchId, String winnerId) {
        return matchsRepository.findById(matchId)
                .map(match -> {
                    match.setWinnerId(winnerId);
                    matchsRepository.save(match);
                    return true;
                })
                .orElse(false);
    }
}
