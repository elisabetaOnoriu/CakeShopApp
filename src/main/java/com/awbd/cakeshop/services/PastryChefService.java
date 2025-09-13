package com.awbd.cakeshop.services;

import com.awbd.cakeshop.exceptions.pastrychef.PastryChefAlreadyExistsException;
import com.awbd.cakeshop.exceptions.pastrychef.PastryChefNotFoundException;
import com.awbd.cakeshop.models.PastryChef;
import com.awbd.cakeshop.repositories.PastryChefRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PastryChefService {

    private final PastryChefRepository pastryChefRepository;

    public PastryChefService(PastryChefRepository pastryChefRepository) {
        this.pastryChefRepository = pastryChefRepository;
    }

    public PastryChef create(PastryChef pastryChef) {
        if (pastryChefRepository.existsByNameAndBirthDate(pastryChef.getName(), pastryChef.getBirthDate())) {
            throw new PastryChefAlreadyExistsException("Pastry chef already exists");
        }
        return pastryChefRepository.save(pastryChef);
    }

    public PastryChef getById(Long id) {
        return pastryChefRepository.findById(id)
                .orElseThrow(() -> new PastryChefNotFoundException("Pastry chef with ID " + id + " not found"));
    }

    public List<PastryChef> getAll() {
        return pastryChefRepository.findAll();
    }

    public PastryChef update(Long id, PastryChef updatedChef) {
        return pastryChefRepository.findById(id)
                .map(existingChef -> {
                    existingChef.setName(updatedChef.getName());
                    existingChef.setBiography(updatedChef.getBiography());
                    existingChef.setBirthDate(updatedChef.getBirthDate());
                    return pastryChefRepository.save(existingChef);
                })
                .orElseThrow(() -> new PastryChefNotFoundException("Pastry chef with ID " + id + " not found"));
    }

    public void delete(Long id) {
        if (!pastryChefRepository.existsById(id)) {
            throw new PastryChefNotFoundException("Pastry chef with ID " + id + " not found");
        }
        pastryChefRepository.deleteById(id);
    }
}
