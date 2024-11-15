package com.example.demo.repository.mongoDB;

import com.example.demo.data.Game;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class GameRepository {

    private final MongoTemplate gameMongoTemplate;

    public GameRepository(MongoTemplate gameMongoTemplate) {
        this.gameMongoTemplate = gameMongoTemplate;
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
}
