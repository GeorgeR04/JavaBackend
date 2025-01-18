package com.example.demo.service.match;

import com.example.demo.data.match.Round;
import com.example.demo.data.match.Match;
import com.example.demo.repository.mongoDB.match.RoundsRepository;
import com.example.demo.repository.mongoDB.user.TeamsRepository;
import com.example.demo.repository.mongoDB.tournament.TournamentsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


@Service
@RequiredArgsConstructor
public class RoundService {

    private final RoundsRepository roundsRepository;
    private final MatchService matchService;
    private final TeamsRepository teamsRepository;
    private final TournamentsRepository tournamentsRepository;

    // Fetch all rounds by tournament ID
    public List<Round> getRoundsByTournamentId(String tournamentId) {
        return roundsRepository.findByTournamentId(tournamentId);
    }

    // Create a new round
    public Round createRound(Round round) {
        return roundsRepository.save(round);
    }

    // Delete a round and associated matches
    public boolean deleteRound(String roundId) {
        return roundsRepository.findById(roundId)
                .map(round -> {
                    round.getMatchIds().forEach(matchService::deleteMatch);
                    roundsRepository.deleteById(roundId);
                    return true;
                })
                .orElse(false);
    }

    // Generate playoff bracket for a tournament
    public List<Round> generatePlayoffBracket(String tournamentId) {
        List<String> participantIds = getParticipantIdsForTournament(tournamentId);
        if (participantIds == null || participantIds.isEmpty()) {
            return null;
        }

        List<Round> rounds = new ArrayList<>();
        int roundNumber = 1;

        while (participantIds.size() > 1) {
            Round round = new Round();
            round.setTournamentId(tournamentId);
            round.setRoundNumber(roundNumber);
            round.setRoundName("Round " + roundNumber);
            round.setType("knockout");

            List<String> matchIds = createMatchesForRound(participantIds, tournamentId, round.getId());
            round.setMatchIds(matchIds);

            roundsRepository.save(round);
            rounds.add(round);

            participantIds = getWinners(matchIds);
            roundNumber++;
        }

        return rounds;
    }

    private List<String> createMatchesForRound(List<String> participantIds, String tournamentId, String roundId) {
        List<String> matchIds = new ArrayList<>();
        for (int i = 0; i < participantIds.size(); i += 2) {
            if (i + 1 < participantIds.size()) {
                Match match = new Match();
                match.setRoundId(roundId);
                match.setTournamentId(tournamentId);
                match.setTeam1Id(participantIds.get(i));
                match.setTeam2Id(participantIds.get(i + 1));
                matchService.createMatch(match);
                matchIds.add(match.getId());
            }
        }
        return matchIds;
    }

    private List<String> getParticipantIdsForTournament(String tournamentId) {
        return tournamentsRepository.findById(tournamentId)
                .map(tournament -> {
                    List<String> participantIds = new ArrayList<>();
                    if ("team".equalsIgnoreCase(tournament.getType())) {
                        participantIds.addAll(fetchTeamParticipants(tournament.getParticipatingIds()));
                    } else if ("solo".equalsIgnoreCase(tournament.getType())) {
                        participantIds.addAll(tournament.getParticipatingIds());
                    } else {
                        throw new IllegalArgumentException("Unknown tournament type: " + tournament.getType());
                    }
                    return participantIds;
                })
                .orElseThrow(() -> new RuntimeException("Tournament not found or no participants registered."));
    }

    private List<String> fetchTeamParticipants(List<String> teamIds) {
        List<String> participantIds = new ArrayList<>();
        teamIds.forEach(teamId -> teamsRepository.findById(teamId)
                .ifPresent(team -> participantIds.add(team.getId())));
        return participantIds;
    }

    private List<String> getWinners(List<String> matchIds) {
        List<String> winners = new ArrayList<>();
        matchIds.forEach(matchId -> matchService.getMatchById(matchId)
                .ifPresent(match -> {
                    if (match.getWinnerId() != null) {
                        winners.add(match.getWinnerId());
                    }
                }));
        return winners;
    }

    // Process the next round for a tournament
    public Round processNextRound(String tournamentId) {
        List<Round> rounds = roundsRepository.findByTournamentId(tournamentId);
        Round lastRound = rounds.stream()
                .max(Comparator.comparingInt(Round::getRoundNumber))
                .orElseThrow(() -> new IllegalStateException("No rounds found for this tournament."));

        validateMatchesInRound(lastRound);

        List<String> winners = getWinners(lastRound.getMatchIds());
        if (winners.size() < 2) {
            throw new IllegalStateException("Not enough participants to create the next round.");
        }

        return createNextRound(tournamentId, lastRound, winners);
    }

    private void validateMatchesInRound(Round lastRound) {
        lastRound.getMatchIds().forEach(matchId -> {
            Match match = matchService.getMatchById(matchId)
                    .orElseThrow(() -> new IllegalStateException("Match not found: " + matchId));
            if (match.getWinnerId() == null) {
                throw new IllegalStateException("Not all matches in the last round have winners.");
            }
        });
    }

    private Round createNextRound(String tournamentId, Round lastRound, List<String> winners) {
        Round nextRound = new Round();
        nextRound.setTournamentId(tournamentId);
        nextRound.setRoundNumber(lastRound.getRoundNumber() + 1);
        nextRound.setRoundName("Round " + nextRound.getRoundNumber());
        nextRound.setType("knockout");

        List<String> nextRoundMatchIds = createMatchesForRound(winners, tournamentId, nextRound.getId());
        nextRound.setMatchIds(nextRoundMatchIds);

        return roundsRepository.save(nextRound);
    }
}
