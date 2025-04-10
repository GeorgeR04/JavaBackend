package com.example.demo.service.match;

import com.example.demo.data.match.Round;
import com.example.demo.data.match.Match;
import com.example.demo.data.tournament.Tournament;
import com.example.demo.data.user.Team;
import com.example.demo.repository.mongoDB.match.RoundRepository;
import com.example.demo.repository.mongoDB.tournament.TournamentRepository;
import com.example.demo.repository.mongoDB.user.TeamRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class RoundService {

    private final RoundRepository roundRepository;
    private final MatchService matchService;

    private final TeamRepository teamRepository;
    private final TournamentRepository tournamentRepository;

    public RoundService(RoundRepository roundRepository, MatchService matchService, TeamRepository teamRepository, TournamentRepository tournamentRepository) {
        this.roundRepository = roundRepository;
        this.matchService = matchService;
        this.teamRepository = teamRepository;
        this.tournamentRepository = tournamentRepository;
    }

    // Get all rounds for a specific tournament
    public List<Round> getRoundsByTournamentId(String tournamentId) {
        return roundRepository.findByTournamentId(tournamentId);
    }

    // Create a new round
    public Round createRound(Round round) {
        return roundRepository.save(round);
    }

    // Delete a round by ID
    public boolean deleteRound(String roundId) {
        Round round = roundRepository.findById(roundId);
        if (round != null) {
            // Delete associated matches
            for (String matchId : round.getMatchIds()) {
                matchService.deleteMatch(matchId);
            }
            roundRepository.deleteById(roundId);
            return true;
        }
        return false;
    }

    // Generate playoff bracket for a tournament
    public List<Round> generatePlayoffBracket(String tournamentId) {
        List<String> participantIds = getParticipantIdsForTournament(tournamentId);
        if (participantIds == null || participantIds.isEmpty()) {
            return null;
        }

        List<Round> rounds = new ArrayList<>();
        int roundNumber = 1;

        // Generate rounds until one winner remains
        while (participantIds.size() > 1) {
            Round round = new Round();
            round.setTournamentId(tournamentId);
            round.setRoundNumber(roundNumber);
            round.setRoundName("Round " + roundNumber);
            round.setType("knockout");

            List<String> matchIds = new ArrayList<>();

            // Pair participants into matches
            for (int i = 0; i < participantIds.size(); i += 2) {
                if (i + 1 < participantIds.size()) {
                    Match match = new Match();
                    match.setRoundId(round.getId());
                    match.setTournamentId(tournamentId);
                    match.setTeam1Id(participantIds.get(i));
                    match.setTeam2Id(participantIds.get(i + 1));
                    matchService.createMatch(match); // Save the match
                    matchIds.add(match.getId());
                }
            }

            round.setMatchIds(matchIds);
            roundRepository.save(round);
            rounds.add(round);

            // Prepare winners for the next round
            participantIds = getWinners(matchIds);
            roundNumber++;
        }

        return rounds;
    }

    private List<String> getParticipantIdsForTournament(String tournamentId) {
        // Fetch the tournament details using the TournamentRepository
        Tournament tournament = tournamentRepository.findById(tournamentId);

        if (tournament == null || tournament.getParticipatingIds() == null) {
            throw new RuntimeException("Tournament not found or no participants registered.");
        }

        List<String> participantIds = new ArrayList<>();

        // Handle team tournaments
        if ("team".equalsIgnoreCase(tournament.getType())) {
            for (String teamId : tournament.getParticipatingIds()) {
                Team team = teamRepository.findById(teamId);
                if (team != null) {
                    participantIds.add(team.getId()); // Add team ID to the participant list
                }
            }
        }
        // Handle solo tournaments
        else if ("solo".equalsIgnoreCase(tournament.getType())) {
            participantIds.addAll(tournament.getParticipatingIds()); // Add player IDs directly
        }
        // Handle unknown types
        else {
            throw new RuntimeException("Unknown tournament type: " + tournament.getType());
        }

        return participantIds;
    }

    private List<String> getWinners(List<String> matchIds) {
        List<String> winners = new ArrayList<>();
        for (String matchId : matchIds) {
            Match match = matchService.getMatchById(matchId);
            if (match != null && match.getWinnerId() != null) {
                winners.add(match.getWinnerId());
            }
        }
        return winners;
    }

    public Round processNextRound(String tournamentId) {
        // Fetch all rounds for the tournament
        List<Round> rounds = roundRepository.findByTournamentId(tournamentId);

        // Find the most recent round based on roundNumber
        Round lastRound = rounds.stream()
                .max(Comparator.comparingInt(Round::getRoundNumber))
                .orElse(null);

        if (lastRound == null) {
            throw new IllegalStateException("No rounds found for this tournament. Cannot process the next round.");
        }

        // Ensure all matches in the last round have winners
        List<String> lastRoundMatchIds = lastRound.getMatchIds();
        List<String> winners = new ArrayList<>();
        for (String matchId : lastRoundMatchIds) {
            Match match = matchService.getMatchById(matchId);
            if (match.getWinnerId() == null) {
                throw new IllegalStateException("Not all matches in the last round have winners.");
            }
            winners.add(match.getWinnerId());
        }

        // Check if we have enough participants for the next round
        if (winners.size() < 2) {
            throw new IllegalStateException("Not enough participants to create the next round.");
        }

        // Create the next round
        Round nextRound = new Round();
        nextRound.setTournamentId(tournamentId);
        nextRound.setRoundNumber(lastRound.getRoundNumber() + 1);
        nextRound.setRoundName("Round " + nextRound.getRoundNumber());
        nextRound.setType("knockout"); // Default to knockout

        // Generate matches for the next round
        List<String> nextRoundMatchIds = new ArrayList<>();
        for (int i = 0; i < winners.size(); i += 2) {
            if (i + 1 < winners.size()) {
                Match match = new Match();
                match.setRoundId(nextRound.getId());
                match.setTournamentId(tournamentId);
                match.setTeam1Id(winners.get(i));
                match.setTeam2Id(winners.get(i + 1));
                match.setWinnerId(null); // No winner yet
                matchService.createMatch(match); // Save the match
                nextRoundMatchIds.add(match.getId());
            }
        }

        nextRound.setMatchIds(nextRoundMatchIds);

        // Save the new round
        roundRepository.save(nextRound);

        return nextRound;
    }


}
