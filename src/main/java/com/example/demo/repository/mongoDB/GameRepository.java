package com.example.demo.repository.mongoDB;

import com.example.demo.data.tournament.Game;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
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

    public String getDatabaseName() {
        return gameMongoTemplate.getDb().getName();
    }

    // Fetch a Game by its ID
    public Game findById(String id) {
        return gameMongoTemplate.findById(id, Game.class);
    }
}

