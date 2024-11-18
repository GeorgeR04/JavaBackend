package com.example.demo.repository.mongoDB.user;

import com.example.demo.data.user.Team;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class TeamRepository {

    private final MongoTemplate mongoTemplate;

    public TeamRepository(@Qualifier("teamsMongoTemplate") MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public Team save(Team team) {
        try {
            return mongoTemplate.save(team);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save team", e);
        }
    }

    public Team findById(String id) {
        try {
            return mongoTemplate.findById(id, Team.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to find team by ID", e);
        }
    }

    public List<Team> findAll() {
        try {
            return mongoTemplate.findAll(Team.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch all teams", e);
        }
    }

    public void deleteById(String id) {
        try {
            mongoTemplate.remove(
                    new Query(Criteria.where("id").is(id)),
                    Team.class
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete team", e);
        }
    }

    public boolean existsById(String id) {
        try {
            return mongoTemplate.findById(id, Team.class) != null;
        } catch (Exception e) {
            throw new RuntimeException("Failed to check team existence", e);
        }
    }

    public List<Team> findByGameId(String gameId) {
        try {
            return mongoTemplate.find(
                    new Query(Criteria.where("gameId").is(gameId)),
                    Team.class
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to find teams by game ID", e);
        }
    }

    // New method to check if a player has created a team
    public boolean existsByTeamLeaderId(String teamLeaderId) {
        try {
            Query query = new Query(Criteria.where("teamLeaderId").is(teamLeaderId));
            return mongoTemplate.exists(query, Team.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to check if player has created a team", e);
        }
    }

}
