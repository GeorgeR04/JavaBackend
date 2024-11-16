package com.example.demo.service;

import com.example.demo.data.Tournament;
import com.example.demo.repository.mongoDB.TournamentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Base64;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TournamentService {

    @Autowired
    private TournamentRepository tournamentRepository;

    public List<Tournament> getAllTournaments() {
        return tournamentRepository.findAll();
    }

    public Tournament getTournamentById(String id) {
        return tournamentRepository.findById(id);
    }

    public Tournament createTournament(Tournament tournament) {
        tournament.setStatus("ONGOING"); // Default status
        tournament.setStartDate(LocalDateTime.now()); // Set current time as start
        tournamentRepository.save(tournament);
        return tournament;
    }

    public Tournament updateTournament(String id, Tournament updatedTournament) {
        Tournament existingTournament = tournamentRepository.findById(id);
        if (existingTournament != null && !"FINISHED".equals(existingTournament.getStatus())) {
            updatedTournament.setId(id); // Keep the same ID
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
        if (tournament != null && !tournament.getParticipatingTeamIds().contains(participantId)) {
            if (tournament.getParticipatingTeamIds().size() < tournament.getMaxTeams()) {
                tournament.getParticipatingTeamIds().add(participantId);
                tournamentRepository.save(tournament);
                return true;
            }
        }
        return false;
    }

    public boolean removeParticipant(String tournamentId, String participantId) {
        Tournament tournament = tournamentRepository.findById(tournamentId);
        if (tournament != null && tournament.getParticipatingTeamIds().contains(participantId)) {
            tournament.getParticipatingTeamIds().remove(participantId);
            tournamentRepository.save(tournament);
            return true;
        }
        return false;
    }

    // Encode the image for frontend rendering
    public String encodeImage(byte[] image) {
        return image != null ? Base64.getEncoder().encodeToString(image) : null;
    }

    // Decode Base64 image from frontend upload
    public byte[] decodeImage(String base64Image) {
        return base64Image != null ? Base64.getDecoder().decode(base64Image) : null;
    }

}
