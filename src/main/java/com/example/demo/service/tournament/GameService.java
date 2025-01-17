package com.example.demo.service.tournament;

import com.example.demo.data.tournament.Game;
import com.example.demo.repository.mongoDB.tournament.GamesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GameService {

    private final GamesRepository gamesRepository;

    // Fetch all games
    public List<Game> getAllGames() {
        try {
            return gamesRepository.findAll();
        } catch (Exception e) {
            throw new RuntimeException("Error fetching games: " + e.getMessage(), e);
        }
    }

    // Fetch game by ID
    public Optional<Game> getGameById(String gameId) {
        return gamesRepository.findById(gameId);
    }

    // Create a new game
    public Game createGame(Game game, Long organizerId) {
        validateGame(game);
        game.setOrganizerId(organizerId.toString()); // Associate the organizer ID
        return gamesRepository.save(game);
    }

    // Count games by organizer
    public long countGamesByOrganizer(Long organizerId) {
        return gamesRepository.countByOrganizerId(organizerId.toString());
    }

    // Fetch games by organizer ID
    public List<Game> getGamesByOrganizer(String organizerId) {
        return gamesRepository.findAllByOrganizerId(organizerId);
    }

    // Update an existing game
    public Game updateGame(Game game) {
        validateGame(game);
        return gamesRepository.save(game);
    }

    // Delete a game by ID
    public void deleteGame(String gameId) {
        if (!gamesRepository.existsById(gameId)) {
            throw new IllegalArgumentException("Game with ID " + gameId + " does not exist.");
        }
        gamesRepository.deleteById(gameId);
    }

    // Private validation helper
    private void validateGame(Game game) {
        if (game.getPublisher() == null || game.getPublisher().isEmpty()) {
            throw new IllegalArgumentException("Publisher cannot be null or empty.");
        }
    }
}
