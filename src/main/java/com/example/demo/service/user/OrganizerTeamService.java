package com.example.demo.service.user;

import com.example.demo.data.user.OrganizerTeam;
import com.example.demo.repository.mongoDB.OrganizerTeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OrganizerTeamService {

    @Autowired
    private OrganizerTeamRepository organizerTeamRepository;

    public OrganizerTeam createOrganizerTeam(OrganizerTeam organizerTeam) {
        return organizerTeamRepository.save(organizerTeam);
    }

    public Optional<OrganizerTeam> getOrganizerTeamById(String id) {
        return Optional.ofNullable(organizerTeamRepository.findById(id));
    }

    public List<OrganizerTeam> getAllOrganizerTeams() {
        return organizerTeamRepository.findAll();
    }

    public List<OrganizerTeam> getTeamsByReputation(double reputationThreshold) {
        return organizerTeamRepository.findByReputation(reputationThreshold);
    }

    public void deleteOrganizerTeam(String id) {
        organizerTeamRepository.deleteById(id);
    }
}
