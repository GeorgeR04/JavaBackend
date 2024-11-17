package com.example.demo.security.connection;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;

@Configuration
public class MultipleMongoConfig {

    @Bean(name = "userProfileMongoTemplate")
    public MongoTemplate userProfileMongoTemplate() {
        MongoClient client = MongoClients.create("mongodb://localhost:27018");
        return new MongoTemplate(client, "UserProfil"); // Connect to UserProfil database
    }

    @Bean(name = "gameMongoTemplate")
    public MongoTemplate gameMongoTemplate() {
        MongoClient client = MongoClients.create("mongodb://localhost:27018");
        return new MongoTemplate(client, "Game"); // Connect to Game database
    }

    @Bean(name = "specializationsMongoTemplate")
    public MongoTemplate specializationsMongoTemplate() {
        MongoClient client = MongoClients.create("mongodb://localhost:27018");
        return new MongoTemplate(client, "Specialization"); // Connect to Specializations database
    }

    @Bean(name = "teamsMongoTemplate")
    public MongoTemplate teamsMongoTemplate() {
        MongoClient client = MongoClients.create("mongodb://localhost:27018");
        return new MongoTemplate(client, "Teams"); // Connect to Teams database
    }

    @Bean(name = "organizerTeamsMongoTemplate")
    public MongoTemplate organizerTeamsMongoTemplate() {
        MongoClient client = MongoClients.create("mongodb://localhost:27018");
        return new MongoTemplate(client, "Organizer"); // Connect to Organizer database
    }

    @Bean(name = "tournamentsMongoTemplate")
    public MongoTemplate tournamentsMongoTemplate() {
        MongoClient client = MongoClients.create("mongodb://localhost:27018");
        return new MongoTemplate(client, "Tournaments"); // Connect to Tournaments database
    }


    @Bean
    public GridFsTemplate gridFsTemplate(@Qualifier("userProfileMongoTemplate") MongoTemplate userProfileMongoTemplate) {
        return new GridFsTemplate(userProfileMongoTemplate.getMongoDatabaseFactory(), userProfileMongoTemplate.getConverter());
    }
}
