package com.example.demo.repository.mongoDB.match;

import com.example.demo.data.match.Match;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface MatchsRepository extends MongoRepository<Match, String> {

    // Custom query to find matches by round ID
    List<Match> findByRoundId(String roundId);

    // Custom query to find matches by tournament ID
    List<Match> findByTournamentId(String tournamentId);
}