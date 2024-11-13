package com.example.demo.repository.mongoDB;

import com.example.demo.data.UserProfile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserProfileRepository extends MongoRepository<UserProfile, String> {
    Optional<UserProfile> findByUsername(String username);

    Optional<UserProfile> findByUserId(Long userId);
}