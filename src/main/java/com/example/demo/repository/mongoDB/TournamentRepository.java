package com.example.demo.repository.mongoDB;

import com.example.demo.data.Tournament;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class TournamentRepository {

    private final MongoTemplate tournamentMongoTemplate;

    public TournamentRepository(@Qualifier("tournamentsMongoTemplate") MongoTemplate tournamentMongoTemplate) {
        this.tournamentMongoTemplate = tournamentMongoTemplate;
    }

    public void save(Tournament tournament) {
        tournamentMongoTemplate.save(tournament);
    }

    public Tournament findById(String id) {
        return tournamentMongoTemplate.findById(id, Tournament.class);
    }

    public List<Tournament> findAll() {
        return tournamentMongoTemplate.findAll(Tournament.class);
    }

    public void deleteById(String id) {
        tournamentMongoTemplate.remove(
                tournamentMongoTemplate.findById(id, Tournament.class)
        );
    }

    public boolean existsById(String id) {
        return tournamentMongoTemplate.findById(id, Tournament.class) != null;
    }

    public List<Tournament> findByStatus(String status) {
        return tournamentMongoTemplate.find(
                new Query(
                        Criteria.where("status").is(status)
                ),
                Tournament.class
        );
    }
}
