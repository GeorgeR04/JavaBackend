package com.example.demo.repository.mongoDB;

import com.example.demo.data.Specialization;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SpecializationRepository {

    private final MongoTemplate specializationsMongoTemplate;

    public SpecializationRepository(MongoTemplate specializationsMongoTemplate) {
        this.specializationsMongoTemplate = specializationsMongoTemplate;
    }

    public void save(Specialization specialization) {
        specializationsMongoTemplate.save(specialization);
    }

    public List<Specialization> findAll() {
        return specializationsMongoTemplate.findAll(Specialization.class);
    }

    public String getDatabaseName() {
        return specializationsMongoTemplate.getDb().getName();
    }
}
