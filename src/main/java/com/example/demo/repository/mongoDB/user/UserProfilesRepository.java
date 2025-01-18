package com.example.demo.repository.mongoDB.user;

import com.example.demo.data.user.UserProfile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserProfilesRepository extends MongoRepository<UserProfile, String> {

    // Custom query to find a user profile by username
    Optional<UserProfile> findByUsername(String username);

    // Custom query to find a user profile by userId
    Optional<UserProfile> findByUserId(Long userId);

    // Custom query to find all user profiles by a list of IDs
    List<UserProfile> findByIdIn(List<String> ids);
}
