package com.example.demo.service.tournament;

import com.example.demo.data.tournament.Game;
import com.example.demo.repository.mongoDB.tournament.GamesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GameService {

    private final GamesRepository gamesRepository;

    // Récupérer tous les jeux
    public List<Game> getAllGames() {
        try {
            return gamesRepository.findAll();
        } catch (Exception e) {
            throw new RuntimeException("Error fetching games: " + e.getMessage(), e);
        }
    }

    // Récupérer un jeu par ID
    public Optional<Game> getGameById(String gameId) {
        return gamesRepository.findById(gameId);
    }

    // Créer un nouveau jeu
    public Game createGame(Game game, Long organizerId) {
        validateGame(game);
        game.setOrganizerId(organizerId.toString());
        return gamesRepository.save(game);
    }

    // Compter les jeux par organisateur
    public long countGamesByOrganizer(Long organizerId) {
        return gamesRepository.countByOrganizerId(organizerId.toString());
    }

    // Récupérer les jeux par ID d’organisateur
    public List<Game> getGamesByOrganizer(String organizerId) {
        return gamesRepository.findAllByOrganizerId(organizerId);
    }

    // Mettre à jour un jeu existant
    public Game updateGame(Game game) {
        validateGame(game);
        return gamesRepository.save(game);
    }

    // Supprimer un jeu par ID
    public void deleteGame(String gameId) {
        if (!gamesRepository.existsById(gameId)) {
            throw new IllegalArgumentException("Game with ID " + gameId + " does not exist.");
        }
        gamesRepository.deleteById(gameId);
    }

    // Nouvel endpoint : Récupérer les jeux populaires (ex. trié par nombre de joueurs)
    public List<Game> getPopularGames() {
        List<Game> games = getAllGames();
        return games.stream()
                .sorted(Comparator.comparingInt(Game::getTotalPlayers).reversed())
                .limit(10) // Retourne les 10 jeux les plus populaires
                .collect(Collectors.toList());
    }

    // Validation privée
    private void validateGame(Game game) {
        if (game.getPublisher() == null || game.getPublisher().isEmpty()) {
            throw new IllegalArgumentException("Publisher cannot be null or empty.");
        }
        // Vous pouvez ajouter ici d'autres validations pour les nouveaux champs si nécessaire
    }
}
