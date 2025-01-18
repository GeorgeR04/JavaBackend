package com.example.demo.service.tournament;

import com.example.demo.data.tournament.Specialization;
import com.example.demo.repository.mongoDB.tournament.SpecializationsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SpecializationService {

    private final SpecializationsRepository specializationsRepository;

    // Fetch all specializations
    public List<Specialization> getAllSpecializations() {
        return specializationsRepository.findAll();
    }

    // Fetch specialization by ID
    public Optional<Specialization> getSpecializationById(String id) {
        return specializationsRepository.findById(id);
    }

    // Add a new specialization
    public Specialization addSpecialization(Specialization specialization) {
        if (specialization == null) {
            throw new IllegalArgumentException("Specialization cannot be null.");
        }
        return specializationsRepository.save(specialization);
    }

    // Update an existing specialization
    public Specialization updateSpecialization(String id, Specialization specialization) {
        return specializationsRepository.findById(id)
                .map(existingSpecialization -> {
                    specialization.setId(id); // Ensure ID consistency
                    return specializationsRepository.save(specialization);
                })
                .orElseThrow(() -> new IllegalArgumentException("Specialization with ID " + id + " does not exist."));
    }

    // Delete a specialization
    public void deleteSpecialization(String id) {
        if (!specializationsRepository.existsById(id)) {
            throw new IllegalArgumentException("Specialization with ID " + id + " does not exist.");
        }
        specializationsRepository.deleteById(id);
    }
}
