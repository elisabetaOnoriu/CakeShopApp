package com.awbd.cakeshop.mappers;

import com.awbd.cakeshop.DTOs.CakeDTO;
import com.awbd.cakeshop.models.Cake;
import com.awbd.cakeshop.models.Category;
import com.awbd.cakeshop.models.PastryChef;
import com.awbd.cakeshop.repositories.CategoryRepository;
import com.awbd.cakeshop.repositories.PastryChefRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class CakeMapper {

    private final CategoryRepository categoryRepository;
    private final PastryChefRepository pastryChefRepository;

    @Autowired
    public CakeMapper(CategoryRepository categoryRepository, PastryChefRepository pastryChefRepository) {
        this.categoryRepository = categoryRepository;
        this.pastryChefRepository = pastryChefRepository;
    }

    public CakeDTO toDto(Cake cake) {
        CakeDTO dto = new CakeDTO();
        dto.setId(cake.getId());
        dto.setName(cake.getName());
        dto.setPrice(cake.getPrice());
        dto.setStock(cake.getStock());
        dto.setWeight(cake.getWeight());

        dto.setDescription(cake.getDescription());

        if (cake.getCategory() != null) {
            dto.setCategoryId(cake.getCategory().getId());
        }

        if (cake.getPastryChef() != null) {
            dto.setPastryChefId(cake.getPastryChef().getId());
            // NEW: also expose the name
            dto.setPastryChefName(cake.getPastryChef().getName());
        }

        return dto;
    }

    public Cake toEntity(CakeDTO dto) {
        Cake cake = new Cake();
        cake.setName(dto.getName());
        cake.setPrice(dto.getPrice());
        cake.setStock(dto.getStock());
        cake.setWeight(dto.getWeight());

        // NEW: description <- DTO
        cake.setDescription(dto.getDescription());

        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + dto.getCategoryId()));
            cake.setCategory(category);
        }

        // Accept either pastryChefId or pastryChefName
        PastryChef chef = resolveChef(dto);
        if (chef != null) {
            cake.setPastryChef(chef);
        }

        return cake;
    }

    public List<CakeDTO> toDtoList(List<Cake> cakes) {
        return cakes.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public void updateEntityFromDto(CakeDTO dto, Cake cake) {
        cake.setName(dto.getName());
        cake.setPrice(dto.getPrice());
        cake.setStock(dto.getStock());
        cake.setWeight(dto.getWeight());

        // NEW: description on update
        cake.setDescription(dto.getDescription());

        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + dto.getCategoryId()));
            cake.setCategory(category);
        }

        // Accept either pastryChefId or pastryChefName on update
        PastryChef chef = resolveChef(dto);
        if (chef != null) {
            cake.setPastryChef(chef);
        }
    }

    /**
     * Resolve a PastryChef from DTO by id (preferred) or by name (fallback).
     * Returns null if neither is provided.
     */
    private PastryChef resolveChef(CakeDTO dto) {
        if (dto.getPastryChefId() != null) {
            return pastryChefRepository.findById(dto.getPastryChefId())
                    .orElseThrow(() ->
                            new EntityNotFoundException("PastryChef not found with id: " + dto.getPastryChefId()));
        }

        String name = dto.getPastryChefName();
        if (name != null && !name.isBlank()) {
            Optional<PastryChef> byName = pastryChefRepository.findByNameIgnoreCase(name.trim());
            return byName.orElseThrow(() ->
                    new EntityNotFoundException("PastryChef not found with name: " + name));
        }

        return null; // not specified
    }
}
