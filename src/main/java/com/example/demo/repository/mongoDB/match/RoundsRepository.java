package com.example.demo.repository.mongoDB.match;

import com.example.demo.data.match.Round;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface RoundsRepository extends MongoRepository<Round, String> {

    // Custom query to find rounds by tournament ID
    List<Round> findByTournamentId(String tournamentId);
}