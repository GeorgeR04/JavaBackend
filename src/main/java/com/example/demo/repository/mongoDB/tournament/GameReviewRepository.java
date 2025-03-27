package com.example.demo.repository.mongoDB.tournament;
import com.example.demo.data.tournament.GameReview;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface GameReviewRepository extends MongoRepository<GameReview, String> {
    List<GameReview> findByGameId(String gameId);
}
