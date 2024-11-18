package com.example.demo.repository.mongoDB.match;

import com.example.demo.data.match.Round;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class RoundRepository {

    private final MongoTemplate roundsMongoTemplate;

    public RoundRepository(@Qualifier("roundsMongoTemplate") MongoTemplate mongoTemplate) {
        this.roundsMongoTemplate = mongoTemplate;
    }

    // Save a round
    public Round save(Round round) {
        roundsMongoTemplate.save(round);
        return round;
    }

    // Fetch all rounds
    public List<Round> findAll() {
        return roundsMongoTemplate.findAll(Round.class);
    }

    // Fetch a round by its ID
    public Round findById(String id) {
        return roundsMongoTemplate.findById(id, Round.class);
    }

    // Fetch all rounds for a specific tournament
    public List<Round> findByTournamentId(String tournamentId) {
        return roundsMongoTemplate.find(
                new org.springframework.data.mongodb.core.query.Query(
                        org.springframework.data.mongodb.core.query.Criteria.where("tournamentId").is(tournamentId)
                ),
                Round.class
        );
    }

    // Delete a round by its ID
    public void deleteById(String id) {
        roundsMongoTemplate.remove(
                new org.springframework.data.mongodb.core.query.Query(
                        org.springframework.data.mongodb.core.query.Criteria.where("_id").is(id)
                ),
                Round.class
        );
    }
}
