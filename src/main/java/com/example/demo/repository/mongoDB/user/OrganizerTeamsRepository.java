package com.example.demo.repository.mongoDB.user;

import com.example.demo.data.user.OrganizerTeam;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrganizerTeamsRepository extends MongoRepository<OrganizerTeam, String> {

    List<OrganizerTeam> findByReputationGreaterThanEqual(double reputationThreshold);
}
