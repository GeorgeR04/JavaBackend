package com.example.demo.repository.mongoDB.user;

import com.example.demo.data.user.Team;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamsRepository extends MongoRepository<Team, String> {

    // Custom query to find teams by game ID
    List<Team> findByGameId(String gameId);

    // Custom query to check if a player has created a team
    boolean existsByTeamLeaderId(String teamLeaderId);

    boolean isTeamLeader(String id, String teamLeaderId);
}
