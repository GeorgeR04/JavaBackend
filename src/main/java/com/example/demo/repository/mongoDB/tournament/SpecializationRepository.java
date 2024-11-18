package com.example.demo.repository.mongoDB.tournament;

import com.example.demo.data.tournament.Specialization;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SpecializationRepository {

    private final MongoTemplate specializationsMongoTemplate;

    public SpecializationRepository(@Qualifier("specializationsMongoTemplate") MongoTemplate specializationsMongoTemplate) {
        this.specializationsMongoTemplate = specializationsMongoTemplate;
    }

    // Save or update a specialization
    public Specialization save(Specialization specialization) {
        specializationsMongoTemplate.save(specialization);
        return specialization; // Align with expected return type
    }

    // Retrieve all specializations
    public List<Specialization> findAll() {
        return specializationsMongoTemplate.findAll(Specialization.class);
    }

    // Retrieve a specialization by ID
    public Specialization findById(String id) {
        return specializationsMongoTemplate.findById(id, Specialization.class);
    }

    // Check if a specialization exists by ID
    public boolean existsById(String id) {
        return specializationsMongoTemplate.exists(
                new org.springframework.data.mongodb.core.query.Query(
                        org.springframework.data.mongodb.core.query.Criteria.where("id").is(id)
                ),
                Specialization.class
        );
    }

    // Delete a specialization by ID
    public void deleteById(String id) {
        Specialization specialization = findById(id);
        if (specialization != null) {
            specializationsMongoTemplate.remove(specialization);
        }
    }

    // Retrieve the name of the database
    public String getDatabaseName() {
        return specializationsMongoTemplate.getDb().getName();
    }
}
