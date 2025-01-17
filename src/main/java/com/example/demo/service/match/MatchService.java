//package com.example.demo.service.match;
//
//import com.example.demo.data.match.Match;
//import com.example.demo.repository.mongoDB.match.MatchRepository;
//import lombok.Data;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//@Data
//@Service
//public class MatchService {
//
//    private final MatchRepository matchRepository;
//
//    public MatchService(MatchRepository matchRepository) {
//        this.matchRepository = matchRepository;
//    }
//
//
//    public Match getMatchById(String id) {
//        return matchRepository.findById(id);
//    }
//
//
//    public List<Match> getMatchesByRoundId(String roundId) {
//        return matchRepository.findByRoundId(roundId);
//    }
//
//
//    public List<Match> getMatchesByTournamentId(String tournamentId) {
//        return matchRepository.findByTournamentId(tournamentId);
//    }
//
//
//    public Match createMatch(Match match) {
//        return matchRepository.save(match);
//    }
//
//
//    public boolean updateMatch(String id, Match match) {
//        Match existingMatch = matchRepository.findById(id);
//        if (existingMatch != null) {
//            match.setId(id);
//            matchRepository.save(match);
//            return true;
//        }
//        return false;
//    }
//
//
//    public boolean deleteMatch(String id) {
//        Match existingMatch = matchRepository.findById(id);
//        if (existingMatch != null) {
//            matchRepository.deleteById(id);
//            return true;
//        }
//        return false;
//    }
//
//
//    public boolean setMatchWinner(String matchId, String winnerId) {
//        Match match = matchRepository.findById(matchId);
//        if (match != null) {
//            match.setWinnerId(winnerId);
//            matchRepository.save(match);
//            return true;
//        }
//        return false;
//    }
//}
