package com.example.demo.service.tournament;

import com.example.demo.data.tournament.GameReview;
import com.example.demo.repository.mongoDB.tournament.GameReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GameReviewService {
    private final GameReviewRepository gameReviewRepository;

    public List<GameReview> getReviewsByGameId(String gameId) {
        return gameReviewRepository.findByGameId(gameId);
    }

    public GameReview addReview(GameReview review) {
        review.setTimestamp(LocalDateTime.now());
        return gameReviewRepository.save(review);
    }

    // Méthode pour récupérer une revue par son ID
    public Optional<GameReview> getReviewById(String reviewId) {
        return gameReviewRepository.findById(reviewId);
    }

    // Méthode pour supprimer une revue par son ID
    public void deleteReview(String reviewId) {
        gameReviewRepository.deleteById(reviewId);
    }
}
