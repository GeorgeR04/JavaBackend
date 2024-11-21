package com.example.demo.repository.mongoDB.tournament;

import com.example.demo.data.tournament.Game;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class GameRepository {

    private final MongoTemplate gameMongoTemplate;

    public GameRepository(@Qualifier("gameMongoTemplate") MongoTemplate mongoTemplate) {
        this.gameMongoTemplate = mongoTemplate;
    }
    public void save(Game game) {
        gameMongoTemplate.save(game);
    }

    public List<Game> findAll() {
        return gameMongoTemplate.findAll(Game.class);
    }

    public Game findById(String id) {
        return gameMongoTemplate.findById(id, Game.class);
    }

    public long countByDeveloper(String developerId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("developer").is(developerId));
        return gameMongoTemplate.count(query, Game.class);
    }

    public List<Game> findByOrganizerId(String organizerId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("organizerId").is(organizerId));
        return gameMongoTemplate.find(query, Game.class);
    }

    public void deleteById(String gameId) {
    }
}

