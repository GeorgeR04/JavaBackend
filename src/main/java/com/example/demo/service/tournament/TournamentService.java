package com.example.demo.service.tournament;

import com.example.demo.data.tournament.Tournament;
import com.example.demo.data.user.Team;
import com.example.demo.data.user.UserProfile;
import com.example.demo.repository.mongoDB.user.TeamsRepository;
import com.example.demo.repository.mongoDB.tournament.TournamentsRepository;
import com.example.demo.repository.mongoDB.user.UserProfilesRepository;
import com.example.demo.security.request.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TournamentService {

    private final TournamentsRepository tournamentsRepository;
    private final UserProfilesRepository userProfilesRepository;
    private final TeamsRepository teamsRepository;
    private final JwtUtil jwtUtil;

    public List<Tournament> getAllTournaments() {
        return tournamentsRepository.findAll();
    }

    public Optional<Tournament> getTournamentById(String id) {
        return tournamentsRepository.findById(id);
    }

    public Tournament createTournament(Tournament tournament) {
        tournament.setStatus("ONGOING");
        tournament.setStartDate(LocalDateTime.now());
        calculateReputationAndRank(tournament);
        return tournamentsRepository.save(tournament);
    }

    public Optional<Tournament> updateTournament(String id, Tournament updatedTournament) {
        return tournamentsRepository.findById(id)
                .filter(tournament -> !"FINISHED".equals(tournament.getStatus()))
                .map(existingTournament -> {
                    updatedTournament.setId(id);
                    calculateReputationAndRank(updatedTournament);
                    return tournamentsRepository.save(updatedTournament);
                });
    }

    public boolean deleteTournament(String id) {
        return tournamentsRepository.findById(id)
                .filter(tournament -> !"FINISHED".equals(tournament.getStatus()))
                .map(tournament -> {
                    tournamentsRepository.deleteById(id);
                    return true;
                })
                .orElse(false);
    }

    public Optional<Tournament> finishTournament(String id) {
        return tournamentsRepository.findById(id)
                .filter(tournament -> "ONGOING".equals(tournament.getStatus()))
                .map(tournament -> {
                    tournament.setStatus("FINISHED");
                    tournament.setFinishDate(LocalDateTime.now());
                    return tournamentsRepository.save(tournament);
                });
    }

    public List<Tournament> getTournamentsByStatus(String status) {
        return tournamentsRepository.findByStatus(status);
    }

    public boolean addParticipant(String tournamentId, String participantId) {
        return tournamentsRepository.findById(tournamentId)
                .map(tournament -> {
                    if (tournament.getParticipatingIds() == null) {
                        tournament.setParticipatingIds(new ArrayList<>());
                    }
                    if (!tournament.getParticipatingIds().contains(participantId)) {
                        tournament.getParticipatingIds().add(participantId);
                        tournamentsRepository.save(tournament);
                        return true;
                    }
                    return false;
                })
                .orElse(false);
    }

    public boolean removeParticipant(String tournamentId, String participantId) {
        return tournamentsRepository.findById(tournamentId)
                .filter(tournament -> tournament.getParticipatingIds() != null)
                .map(tournament -> {
                    if (tournament.getParticipatingIds().remove(participantId)) {
                        tournamentsRepository.save(tournament);
                        return true;
                    }
                    return false;
                })
                .orElse(false);
    }

    public String getRoleFromToken(String token) {
        String username = jwtUtil.extractUsername(token);
        return Optional.ofNullable(username)
                .flatMap(userProfilesRepository::findByUsername)
                .map(UserProfile::getRole)
                .orElse(null);
    }

    public String encodeImage(byte[] image) {
        return image != null ? Base64.getEncoder().encodeToString(image) : null;
    }

    public byte[] decodeImage(String base64Image) {
        return base64Image != null ? Base64.getDecoder().decode(base64Image) : null;
    }

    private void calculateReputationAndRank(Tournament tournament) {
        double totalReputation = 0;
        int totalOrganizers = 0;

        if (tournament.getTeamId() != null) {
            totalReputation += calculateTeamReputation(tournament.getTeamId());
            totalOrganizers += countTeamMembers(tournament.getTeamId());
        }

        if (tournament.getOrganizerIds() != null) {
            List<UserProfile> organizers = getUserProfilesByIds(tournament.getOrganizerIds());
            totalReputation += organizers.stream().mapToDouble(UserProfile::getTrustFactor).sum();
            totalOrganizers += organizers.size();
        }

        double reputation = totalOrganizers > 0 ? totalReputation / totalOrganizers : 0;
        tournament.setReputation(reputation);
        tournament.setRank(determineRank(reputation));
    }

    private double calculateTeamReputation(String teamId) {
        return teamsRepository.findById(teamId)
                .map(team -> team.getPlayerIds().stream()
                        .map(userProfilesRepository::findById)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .mapToDouble(UserProfile::getTrustFactor)
                        .average()
                        .orElse(0))
                .orElse(0.0);
    }

    private int countTeamMembers(String teamId) {
        return teamsRepository.findById(teamId)
                .map(team -> team.getPlayerIds().size())
                .orElse(0);
    }

    private List<UserProfile> getUserProfilesByIds(List<String> ids) {
        return ids.stream()
                .map(userProfilesRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    public void updateTournamentDetails(Tournament existingTournament, Map<String, Object> payload) {
        // Update the name
        if (payload.containsKey("name")) {
            existingTournament.setName((String) payload.get("name"));
        }

        // Update the description
        if (payload.containsKey("description")) {
            existingTournament.setDescription((String) payload.get("description"));
        }

        // Update game ID
        if (payload.containsKey("gameId")) {
            existingTournament.setGameId((String) payload.get("gameId"));
        }

        // Update visibility
        if (payload.containsKey("visibility")) {
            existingTournament.setVisibility((String) payload.get("visibility"));
        }

        // Update trust factor requirement
        if (payload.containsKey("trustFactorRequirement")) {
            Integer trustFactor = parseInteger(payload.get("trustFactorRequirement"), null);
            if (trustFactor != null) {
                existingTournament.setTrustFactorRequirement(trustFactor);
            }
        }

        // Update rank requirements
        Integer minRank = parseInteger(payload.get("minRankRequirement"), null);
        Integer maxRank = parseInteger(payload.get("maxRankRequirement"), null);
        if (minRank != null && maxRank != null) {
            if (minRank > maxRank) {
                throw new IllegalArgumentException("Minimum rank cannot be greater than maximum rank.");
            }
            existingTournament.setMinRankRequirement(minRank);
            existingTournament.setMaxRankRequirement(maxRank);
        }

        // Update cash prize
        if (payload.containsKey("cashPrize")) {
            Double cashPrize = parseDouble(payload.get("cashPrize"), null);
            if (cashPrize != null) {
                existingTournament.setCashPrize(cashPrize);
            }
        }

        // Update image
        if (payload.containsKey("image")) {
            String base64Image = (String) payload.get("image");
            existingTournament.setImage(base64Image != null ? Base64.getDecoder().decode(base64Image) : null);
        }

        // Update other fields as necessary...

        // Save the updated tournament
        tournamentsRepository.save(existingTournament);
    }

    // Helper methods for parsing
    private Integer parseInteger(Object value, Integer defaultValue) {
        try {
            return value != null ? Integer.parseInt(value.toString()) : defaultValue;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid integer value: " + value);
        }
    }

    private Double parseDouble(Object value, Double defaultValue) {
        try {
            return value != null ? Double.parseDouble(value.toString()) : defaultValue;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid double value: " + value);
        }
    }






    private String determineRank(double reputation) {
        if (reputation >= 8) return "S";
        if (reputation >= 6) return "A";
        if (reputation >= 4) return "B";
        if (reputation >= 2) return "C";
        return "D";
    }
}
