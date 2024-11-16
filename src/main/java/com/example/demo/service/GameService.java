package com.example.demo.service;

import com.example.demo.data.Game;
import com.example.demo.repository.mongoDB.GameRepository;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class GameService {

    private final GameRepository gameRepository;

    public GameService(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    public List<Game> getAllGames() {
        try {
            List<Game> games = gameRepository.findAll();
            System.out.println("Number of games found: " + games.size());
            System.out.println("Games: " + games);
            return games;
        } catch (Exception e) {
            System.out.println("Error fetching games: " + e.getMessage());
            return Collections.emptyList();
        }
    }
}
