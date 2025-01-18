package com.example.demo.repository.mongoDB.tournament;

import com.example.demo.data.tournament.Tournament;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TournamentsRepository extends MongoRepository<Tournament, String> {

    // Custom query to find tournaments by status
    List<Tournament> findByStatus(String status);
}
