package com.example.demo.controller;

import com.example.demo.service.DataInitializationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/data")
public class DataInitializationController {

    @Autowired
    private DataInitializationService dataInitializationService;

    @PostMapping("/initialize")
    public ResponseEntity<String> initializeData() {
        dataInitializationService.initializeData();
        return ResponseEntity.ok("Data initialized successfully");
    }
}