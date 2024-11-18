package com.example.demo.repository.mongoDB.user;

import com.example.demo.data.user.UserProfile;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class UserProfileRepository {

    private final MongoTemplate userProfileMongoTemplate;



    public UserProfileRepository(@Qualifier("userProfileMongoTemplate") MongoTemplate userProfileMongoTemplate) {
        this.userProfileMongoTemplate = userProfileMongoTemplate;
    }

    public UserProfile save(UserProfile userProfile) {
        userProfileMongoTemplate.save(userProfile);
        return userProfile;
    }

    public Optional<UserProfile> findByUsername(String username) {
        UserProfile userProfile = userProfileMongoTemplate
                .findOne(Query.query(Criteria.where("username").is(username)), UserProfile.class);
        return Optional.ofNullable(userProfile);
    }

    public Optional<UserProfile> findByUserId(Long userId) {
        UserProfile userProfile = userProfileMongoTemplate
                .findOne(Query.query(Criteria.where("userId").is(userId)), UserProfile.class);
        return Optional.ofNullable(userProfile);
    }

    public List<UserProfile> findAll() {
        return userProfileMongoTemplate.findAll(UserProfile.class);
    }


    public void deleteById(String id) {
        userProfileMongoTemplate.remove(userProfileMongoTemplate.findById(id, UserProfile.class));
    }

    public List<UserProfile> findAllByIds(List<String> ids) {
        Query query = new Query(Criteria.where("id").in(ids));
        return userProfileMongoTemplate.find(query, UserProfile.class);
    }

}
