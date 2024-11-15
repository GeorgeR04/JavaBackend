package com.example.demo.security.connection;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;

@Configuration
public class MultipleMongoConfig {

    @Bean
    public MongoTemplate userProfileMongoTemplate() {
        MongoClient client = MongoClients.create("mongodb://localhost:27018");
        return new MongoTemplate(client, "UserProfil"); // Connect to UserProfil database
    }

    @Bean
    public MongoTemplate gameMongoTemplate() {
        MongoClient client = MongoClients.create("mongodb://localhost:27018");
        return new MongoTemplate(client, "Game"); // Connect to Game database
    }

    @Bean
    public MongoTemplate specializationsMongoTemplate() {
        MongoClient client = MongoClients.create("mongodb://localhost:27018");
        return new MongoTemplate(client, "Specialization"); // Connect to Specializations database
    }

    @Bean
    public GridFsTemplate gridFsTemplate(MongoTemplate userProfileMongoTemplate) {
        return new GridFsTemplate(userProfileMongoTemplate.getMongoDatabaseFactory(), userProfileMongoTemplate.getConverter());
    }
}
