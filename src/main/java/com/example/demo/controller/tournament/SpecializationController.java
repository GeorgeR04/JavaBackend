package com.example.demo.controller.tournament;

import com.example.demo.data.tournament.Specialization;
import com.example.demo.service.tournament.SpecializationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/specializations")
public class SpecializationController {

    @Autowired
    private SpecializationService specializationService;

    @GetMapping
    public ResponseEntity<List<Specialization>> getAllSpecializations() {
        return ResponseEntity.ok(specializationService.getAllSpecializations());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Specialization> getSpecializationById(@PathVariable String id) {
        return specializationService.getSpecializationById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Specialization> addSpecialization(@RequestBody Specialization specialization) {
        return ResponseEntity.ok(specializationService.addSpecialization(specialization));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Specialization> updateSpecialization(
            @PathVariable String id, @RequestBody Specialization specialization) {
        return ResponseEntity.ok(specializationService.updateSpecialization(id, specialization));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSpecialization(@PathVariable String id) {
        specializationService.deleteSpecialization(id);
        return ResponseEntity.noContent().build();
    }
}
