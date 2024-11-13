//package com.example.demo.test;
//
//import com.example.demo.data.UserProfile;
//import com.example.demo.repository.mongoDB.UserProfileRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.stereotype.Component;
//
//@Component
//public class MongoDbTestRunner implements CommandLineRunner {
//
//    @Autowired
//    private UserProfileRepository userProfileRepository;
//
//    @Override
//    public void run(String... args) throws Exception {
//        // Test saving a UserProfile to MongoDB
//        UserProfile testProfile = new UserProfile();
//        testProfile.setUserId(1L);
//        testProfile.setUsername("testuser");
//        testProfile.setFirstname("Test");
//        testProfile.setLastname("User");
//
//        userProfileRepository.save(testProfile);
//        System.out.println("Test UserProfile saved in MongoDB with ID: " + testProfile.getId());
//    }
//}
