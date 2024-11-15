package com.example.demo.service;

import com.example.demo.data.Game;
import com.example.demo.data.Specialization;
import com.example.demo.repository.mongoDB.GameRepository;
import com.example.demo.repository.mongoDB.SpecializationRepository;
import org.springframework.stereotype.Service;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



@Service
public class DataInitializationService {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializationService.class);

    private final GameRepository gameRepository;
    private final SpecializationRepository specializationRepository;

    public DataInitializationService(GameRepository gameRepository, SpecializationRepository specializationRepository) {
        this.gameRepository = gameRepository;
        this.specializationRepository = specializationRepository;
    }

    public void initializeData() {
        logger.info("Starting data initialization...");

        System.out.println("Game database: " + gameRepository.getDatabaseName());
        System.out.println("Specializations database: " + specializationRepository.getDatabaseName());
        // Add sample games
        Game game1 = new Game();
        game1.setName("Call of Duty");
        game1.setType("FPS");
        game1.setDescription("A popular first-person shooter game.");
        game1.setYearOfExistence(2003);
        game1.setLastTournamentDate("2024-11-01");
        game1.setPlatforms(Arrays.asList("PC", "PlayStation", "Xbox"));
        game1.setDeveloper("Infinity Ward");
        gameRepository.save(game1);
        logger.info("Saved game: {}", game1);

        Game game2 = new Game();
        game2.setName("League of Legends");
        game2.setType("MOBA");
        game2.setDescription("A competitive multiplayer online battle arena game.");
        game2.setYearOfExistence(2009);
        game2.setPlatforms(Arrays.asList("PC"));
        game2.setDeveloper("Riot Games");
        gameRepository.save(game2);
        logger.info("Saved game: {}", game2);

        // Add sample specializations
        Specialization specialization1 = new Specialization();
        specialization1.setName("Sniper");
        specialization1.setDescription("Long-range specialist.");
        specialization1.setSkillsRequired(Arrays.asList("Accuracy", "Positioning"));
        specialization1.setPreferredGames(Arrays.asList(game1.getId()));
        specializationRepository.save(specialization1);
        logger.info("Saved specialization: {}", specialization1);

        Specialization specialization2 = new Specialization();
        specialization2.setName("Strategist");
        specialization2.setDescription("Master of tactics and strategy.");
        specialization2.setSkillsRequired(Arrays.asList("Game Knowledge", "Decision Making"));
        specialization2.setPreferredGames(Arrays.asList(game2.getId()));
        specializationRepository.save(specialization2);
        logger.info("Saved specialization: {}", specialization2);

        logger.info("Data initialization completed.");
    }
}

