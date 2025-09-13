package com.awbd.cakeshop.services;

import com.awbd.cakeshop.models.Cake;
import com.awbd.cakeshop.repositories.CakeRepository;
import jakarta.persistence.EntityNotFoundException;   // Spring Boot 3 (Jakarta)
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CakeService {

    private final CakeRepository cakeRepository;

    public CakeService(CakeRepository cakeRepository) {
        this.cakeRepository = cakeRepository;
    }

    public List<Cake> getAllCakes() {
        return cakeRepository.findAll();
    }

    public List<Cake> searchByName(String name) {
        return cakeRepository.findByNameContaining(name);
    }

    public Cake addCake(Cake cake) {
        return cakeRepository.save(cake);
    }

    public void updateCakeStock(Long cakeId, int quantity) {
        cakeRepository.updateStock(cakeId, quantity);
    }

    public List<Cake> getCakesInStock() {
        return cakeRepository.findByStockGreaterThan(0);
    }

    public List<Cake> getCakesByCategoryId(Long categoryId) {
        return cakeRepository.findByCategoryId(categoryId);
    }

    public void deleteCake(Long cakeId) {
        cakeRepository.deleteById(cakeId);
    }

    /** Update an existing cake by copying allowed fields. */
    public Cake update(Long id, Cake data) {
        Cake existing = cakeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cake not found: " + id));

        // scalar fields
        existing.setName(data.getName());
        existing.setPrice(data.getPrice());
        existing.setStock(data.getStock());
        existing.setWeight(data.getWeight());
        existing.setDescription(data.getDescription());

        // relations / optional fields
        if (data.getCategory() != null) {
            // If you need to ensure the Category is managed, resolve it in a CategoryService/Repository.
            existing.setCategory(data.getCategory());
        }

        if (data.getPastryChef() != null) existing.setPastryChef(data.getPastryChef());

        return cakeRepository.save(existing);
    }

    public Optional<Cake> findById(Long id) {
        return cakeRepository.findById(id);
    }
}
