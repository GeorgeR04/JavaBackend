package com.example.demo.service.tournament;

import com.example.demo.data.tournament.Specialization;
import com.example.demo.repository.mongoDB.tournament.SpecializationRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SpecializationService {

    private final SpecializationRepository specializationRepository;

    public SpecializationService(SpecializationRepository specializationRepository) {
        this.specializationRepository = specializationRepository;
    }

    // Fetch all specializations
    public List<Specialization> getAllSpecializations() {
        return specializationRepository.findAll();
    }

    // Fetch specialization by ID
    public Optional<Specialization> getSpecializationById(String id) {
        Specialization specialization = specializationRepository.findById(id);
        return Optional.ofNullable(specialization); // Convert to Optional
    }

    // Add a new specialization
    public Specialization addSpecialization(Specialization specialization) {
        if (specialization == null) {
            throw new IllegalArgumentException("Specialization cannot be null.");
        }
        return specializationRepository.save(specialization);
    }

    // Update an existing specialization
    public Specialization updateSpecialization(String id, Specialization specialization) {
        if (!specializationRepository.existsById(id)) {
            throw new IllegalArgumentException("Specialization with ID " + id + " does not exist.");
        }
        specialization.setId(id); // Ensure ID consistency
        return specializationRepository.save(specialization);
    }

    // Delete a specialization
    public void deleteSpecialization(String id) {
        if (!specializationRepository.existsById(id)) {
            throw new IllegalArgumentException("Specialization with ID " + id + " does not exist.");
        }
        specializationRepository.deleteById(id);
    }
}
