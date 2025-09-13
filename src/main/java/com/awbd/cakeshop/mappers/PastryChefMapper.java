package com.awbd.cakeshop.mappers;

import com.awbd.cakeshop.DTOs.PastryChefDTO;
import com.awbd.cakeshop.models.Cake;
import com.awbd.cakeshop.models.PastryChef;
import com.awbd.cakeshop.repositories.CakeRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class PastryChefMapper {

    private final CakeRepository cakeRepository;

    @Autowired
    public PastryChefMapper(CakeRepository cakeRepository) {
        this.cakeRepository = cakeRepository;
    }

    public PastryChefDTO toDto(PastryChef chef) {
        PastryChefDTO dto = new PastryChefDTO();
        dto.setId(chef.getId());
        dto.setName(chef.getName());
        dto.setBiography(chef.getBiography());
        dto.setBirthDate(chef.getBirthDate());

        if (chef.getCakes() != null && !chef.getCakes().isEmpty()) {
            List<Long> cakeIds = chef.getCakes().stream()
                    .map(Cake::getId)
                    .collect(Collectors.toList());
            dto.setCakeIds(cakeIds);
        }

        return dto;
    }

    public PastryChef toEntity(PastryChefDTO dto) {
        PastryChef chef = new PastryChef();
        chef.setName(dto.getName());
        chef.setBiography(dto.getBiography());
        chef.setBirthDate(dto.getBirthDate());

        if (dto.getCakeIds() != null && !dto.getCakeIds().isEmpty()) {
            List<Cake> cakes = dto.getCakeIds().stream()
                    .map(id -> cakeRepository.findById(id)
                            .orElseThrow(() -> new EntityNotFoundException("Cake not found with id: " + id)))
                    .collect(Collectors.toList());

            cakes.forEach(chef::addCake);
        }

        return chef;
    }

    public List<PastryChefDTO> toDtoList(List<PastryChef> chefs) {
        return chefs.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public void updateEntityFromDto(PastryChefDTO dto, PastryChef chef) {
        chef.setName(dto.getName());
        chef.setBiography(dto.getBiography());
        chef.setBirthDate(dto.getBirthDate());

        if (dto.getCakeIds() != null && !dto.getCakeIds().isEmpty()) {
            List<Cake> newCakes = dto.getCakeIds().stream()
                    .map(id -> cakeRepository.findById(id)
                            .orElseThrow(() -> new EntityNotFoundException("Cake not found with id: " + id)))
                    .collect(Collectors.toList());

            if (chef.getCakes() != null) {
                List<Cake> currentCakes = new ArrayList<>(chef.getCakes());
                currentCakes.forEach(chef::removeCake);
            }

            newCakes.forEach(chef::addCake);
        }
    }
}
