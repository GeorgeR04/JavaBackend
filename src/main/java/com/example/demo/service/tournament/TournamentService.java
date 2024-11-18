package com.example.demo.service.tournament;

import com.example.demo.data.tournament.Tournament;
import com.example.demo.data.user.Team;
import com.example.demo.data.user.UserProfile;
import com.example.demo.repository.mongoDB.user.TeamRepository;
import com.example.demo.repository.mongoDB.tournament.TournamentRepository;
import com.example.demo.repository.mongoDB.user.UserProfileRepository;
import com.example.demo.security.request.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Base64;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TournamentService {

    @Autowired
    private TournamentRepository tournamentRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private JwtUtil jwtUtil;

    public List<Tournament> getAllTournaments() {
        return tournamentRepository.findAll();
    }

    public Tournament getTournamentById(String id) {
        return tournamentRepository.findById(id);
    }

    public Tournament createTournament(Tournament tournament) {
        tournament.setStatus("ONGOING"); // Default status
        tournament.setStartDate(LocalDateTime.now()); // Set current time as start
        calculateReputationAndRank(tournament); // Calculate reputation and rank
        tournamentRepository.save(tournament);
        return tournament;
    }

    public Tournament updateTournament(String id, Tournament updatedTournament) {
        Tournament existingTournament = tournamentRepository.findById(id);
        if (existingTournament != null && !"FINISHED".equals(existingTournament.getStatus())) {
            updatedTournament.setId(id); // Keep the same ID
            calculateReputationAndRank(updatedTournament); // Recalculate reputation and rank
            tournamentRepository.save(updatedTournament);
            return updatedTournament;
        }
        return null; // Modification not allowed for finished tournaments
    }

    public boolean deleteTournament(String id) {
        Tournament tournament = tournamentRepository.findById(id);
        if (tournament != null && !"FINISHED".equals(tournament.getStatus())) {
            tournamentRepository.deleteById(id);
            return true;
        }
        return false; // Cannot delete a finished tournament
    }

    public Tournament finishTournament(String id) {
        Tournament tournament = tournamentRepository.findById(id);
        if (tournament != null && "ONGOING".equals(tournament.getStatus())) {
            tournament.setStatus("FINISHED");
            tournament.setFinishDate(LocalDateTime.now());
            tournamentRepository.save(tournament);
            return tournament;
        }
        return null; // Cannot finish a non-existent or already finished tournament
    }

    public List<Tournament> getTournamentsByStatus(String status) {
        return tournamentRepository.findByStatus(status);
    }

    public boolean addParticipant(String tournamentId, String participantId) {
        Tournament tournament = tournamentRepository.findById(tournamentId);
        if (tournament != null) {
            // Initialize participatingIds if null
            if (tournament.getParticipatingIds() == null) {
                tournament.setParticipatingIds(new ArrayList<>());
            }

            if ("solo".equals(tournament.getType())) {
                if (!tournament.getParticipatingIds().contains(participantId)) {
                    tournament.getParticipatingIds().add(participantId);
                    tournamentRepository.save(tournament);
                    return true;
                }
            } else if ("team".equals(tournament.getType())) {
                if (!tournament.getParticipatingIds().contains(participantId)) {
                    tournament.getParticipatingIds().add(participantId);
                    tournamentRepository.save(tournament);
                    return true;
                }
            }
        }
        return false;
    }

    public boolean removeParticipant(String tournamentId, String participantId) {
        Tournament tournament = tournamentRepository.findById(tournamentId);
        if (tournament != null && tournament.getParticipatingIds().contains(participantId)) {
            tournament.getParticipatingIds().remove(participantId);
            tournamentRepository.save(tournament);
            return true;
        }
        return false;
    }

    // **NEW**: Get role from token
    public String getRoleFromToken(String token) {
        String username = jwtUtil.extractUsername(token);
        if (username == null || username.isEmpty()) {
            return null; // Invalid token or no username found
        }
        Optional<UserProfile> userProfileOpt = userProfileRepository.findByUsername(username);
        return userProfileOpt.map(UserProfile::getRole).orElse(null);
    }

    // Encode the image for frontend rendering
    public String encodeImage(byte[] image) {
        return image != null ? Base64.getEncoder().encodeToString(image) : null;
    }

    // Decode Base64 image from frontend upload
    public byte[] decodeImage(String base64Image) {
        return base64Image != null ? Base64.getDecoder().decode(base64Image) : null;
    }

    // **NEW**: Calculate reputation and rank
    private void calculateReputationAndRank(Tournament tournament) {
        double totalReputation = 0;
        int totalOrganizers = 0;

        // Calculate reputation for organizer team
        if (tournament.getTeamId() != null) {
            Team organizerTeam = teamRepository.findById(tournament.getTeamId());
            if (organizerTeam != null) {
                List<UserProfile> teamMembers = organizerTeam.getPlayerIds().stream()
                        .map(userProfileRepository::findByUsername)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList());

                double teamReputation = teamMembers.stream()
                        .mapToDouble(UserProfile::getTrustFactor)
                        .average()
                        .orElse(0.0);

                totalReputation += teamReputation * teamMembers.size();
                totalOrganizers += teamMembers.size();
            }
        }

        // Calculate reputation for individual organizers
        if (tournament.getOrganizerIds() != null) {
            List<UserProfile> individualOrganizers = tournament.getOrganizerIds().stream()
                    .map(userProfileRepository::findByUsername)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());

            double individualReputation = individualOrganizers.stream()
                    .mapToDouble(UserProfile::getTrustFactor)
                    .sum();

            totalReputation += individualReputation;
            totalOrganizers += individualOrganizers.size();
        }

        // Calculate average reputation
        double reputation = totalOrganizers > 0 ? totalReputation / totalOrganizers : 0;
        tournament.setReputation(reputation);

        // Set rank based on reputation
        tournament.setRank(determineRank(reputation));
    }

    // **NEW**: Determine rank based on reputation
    private String determineRank(double reputation) {
        if (reputation >= 8) {
            return "S";
        } else if (reputation >= 6) {
            return "A";
        } else if (reputation >= 4) {
            return "B";
        } else if (reputation >= 2) {
            return "C";
        } else {
            return "D";
        }
    }

}
