package com.example.demo.repository.mongoDB.tournament;

import com.example.demo.data.tournament.Specialization;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpecializationsRepository extends MongoRepository<Specialization, String> {

    // Retrieve all specializations (already provided by MongoRepository)
    @Override
    List<Specialization> findAll();

    // Retrieve a specialization by ID (already provided by MongoRepository)
    @Override
    Optional<Specialization> findById(String id);

    // Check if a specialization exists by ID (already provided by MongoRepository)
    @Override
    boolean existsById(String id);

    // Delete a specialization by ID (already provided by MongoRepository)
    @Override
    void deleteById(String id);

    // Additional custom queries can be added here if needed
}