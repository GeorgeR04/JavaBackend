package com.example.demo.repository.mongoDB.match;

import com.example.demo.data.match.Match;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MatchRepository {

    private final MongoTemplate matchesMongoTemplate;

    public MatchRepository(@Qualifier("matchesMongoTemplate") MongoTemplate mongoTemplate) {
        this.matchesMongoTemplate = mongoTemplate;
    }

    // Save a match and return it
    public Match save(Match match) {
        matchesMongoTemplate.save(match);
        return match; // Return the saved match
    }

    // Fetch all matches
    public List<Match> findAll() {
        return matchesMongoTemplate.findAll(Match.class);
    }

    // Fetch a match by its ID
    public Match findById(String id) {
        return matchesMongoTemplate.findById(id, Match.class);
    }

    // Fetch all matches for a specific round
    public List<Match> findByRoundId(String roundId) {
        return matchesMongoTemplate.find(
                new org.springframework.data.mongodb.core.query.Query(
                        org.springframework.data.mongodb.core.query.Criteria.where("roundId").is(roundId)
                ),
                Match.class
        );
    }

    // Fetch all matches for a specific tournament
    public List<Match> findByTournamentId(String tournamentId) {
        return matchesMongoTemplate.find(
                new org.springframework.data.mongodb.core.query.Query(
                        org.springframework.data.mongodb.core.query.Criteria.where("tournamentId").is(tournamentId)
                ),
                Match.class
        );
    }

    // Delete a match by its ID
    public void deleteById(String id) {
        matchesMongoTemplate.remove(
                new org.springframework.data.mongodb.core.query.Query(
                        org.springframework.data.mongodb.core.query.Criteria.where("_id").is(id)
                ),
                Match.class
        );
    }
}
