package com.example.demo.repository.mongoDB.tournament;

import com.example.demo.data.tournament.Game;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GamesRepository extends MongoRepository<Game, String> {
    Optional<Game> findByOrganizerId(String s);
    List<Game> findAllByOrganizerId(String id);
    long countByOrganizerId(String id);

}
