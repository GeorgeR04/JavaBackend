//package com.example.demo.repository.mongoDB.match;
//
//import com.example.demo.data.match.Round;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.data.mongodb.core.MongoTemplate;
//import org.springframework.stereotype.Repository;
//
//import java.util.List;
//
//@Repository
//public class RoundRepository {
//
//    private final MongoTemplate roundsMongoTemplate;
//
//    public RoundRepository(@Qualifier("roundsMongoTemplate") MongoTemplate mongoTemplate) {
//        this.roundsMongoTemplate = mongoTemplate;
//    }
//
//
//    public Round save(Round round) {
//        roundsMongoTemplate.save(round);
//        return round;
//    }
//
//
//    public List<Round> findAll() {
//        return roundsMongoTemplate.findAll(Round.class);
//    }
//
//
//    public Round findById(String id) {
//        return roundsMongoTemplate.findById(id, Round.class);
//    }
//
//
//    public List<Round> findByTournamentId(String tournamentId) {
//        return roundsMongoTemplate.find(
//                new org.springframework.data.mongodb.core.query.Query(
//                        org.springframework.data.mongodb.core.query.Criteria.where("tournamentId").is(tournamentId)
//                ),
//                Round.class
//        );
//    }
//
//
//    public void deleteById(String id) {
//        roundsMongoTemplate.remove(
//                new org.springframework.data.mongodb.core.query.Query(
//                        org.springframework.data.mongodb.core.query.Criteria.where("_id").is(id)
//                ),
//                Round.class
//        );
//    }
//}
