package com.example.demo.repository.mongoDB;

import com.example.demo.data.user.OrganizerTeam;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class OrganizerTeamRepository {

    private final MongoTemplate mongoTemplate;

    public OrganizerTeamRepository(@Qualifier("organizerTeamsMongoTemplate") MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public OrganizerTeam save(OrganizerTeam organizerTeam) {
        try {
            return mongoTemplate.save(organizerTeam);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save organizer team", e);
        }
    }

    public OrganizerTeam findById(String id) {
        try {
            return mongoTemplate.findById(id, OrganizerTeam.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to find organizer team by ID", e);
        }
    }

    public List<OrganizerTeam> findAll() {
        try {
            return mongoTemplate.findAll(OrganizerTeam.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch all organizer teams", e);
        }
    }

    public void deleteById(String id) {
        try {
            mongoTemplate.remove(
                    new Query(Criteria.where("id").is(id)),
                    OrganizerTeam.class
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete organizer team", e);
        }
    }

    public boolean existsById(String id) {
        try {
            return mongoTemplate.findById(id, OrganizerTeam.class) != null;
        } catch (Exception e) {
            throw new RuntimeException("Failed to check organizer team existence", e);
        }
    }

    public List<OrganizerTeam> findByReputation(double reputationThreshold) {
        try {
            return mongoTemplate.find(
                    new Query(Criteria.where("reputation").gte(reputationThreshold)),
                    OrganizerTeam.class
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to find organizer teams by reputation", e);
        }
    }
}
