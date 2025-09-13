package com.awbd.cakeshop.controllers;

import com.awbd.cakeshop.DTOs.PastryChefDTO;
import com.awbd.cakeshop.models.PastryChef;
import com.awbd.cakeshop.services.PastryChefService;
import com.awbd.cakeshop.mappers.PastryChefMapper;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/pastry-chefs")
public class PastryChefController {

    private final PastryChefService pastryChefService;
    private final PastryChefMapper pastryChefMapper;
    private static final Logger logger = LoggerFactory.getLogger(PastryChefController.class);

    public PastryChefController(PastryChefService pastryChefService, PastryChefMapper pastryChefMapper) {
        this.pastryChefService = pastryChefService;
        this.pastryChefMapper = pastryChefMapper;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PastryChef> createPastryChef(
            @RequestBody @Valid PastryChefDTO pastryChefDTO) {
        logger.info("Creating new pastry chef: {}", pastryChefDTO.getName());
        PastryChef pastryChef = pastryChefMapper.toEntity(pastryChefDTO);
        PastryChef createdPastryChef = pastryChefService.create(pastryChef);
        return ResponseEntity.created(URI.create("/api/pastry-chefs/" + createdPastryChef.getId()))
                .body(createdPastryChef);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PastryChef> getPastryChefById(@PathVariable Long id) {
        PastryChef pastryChef = pastryChefService.getById(id);
        return ResponseEntity.ok(pastryChef);
    }

    @GetMapping
    public List<PastryChef> getAllPastryChefs() {
        return pastryChefService.getAll();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PastryChef> updatePastryChef(
            @PathVariable Long id,
            @RequestBody @Valid PastryChefDTO pastryChefDTO) {
        if (pastryChefDTO.getId() != null && !id.equals(pastryChefDTO.getId())) {
            throw new RuntimeException("Id from path does not match with id from request");
        }

        PastryChef pastryChef = pastryChefMapper.toEntity(pastryChefDTO);
        logger.info("Updating pastry chef with id: {}", id);
        return ResponseEntity.ok(pastryChefService.update(id, pastryChef));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePastryChef(@PathVariable Long id) {
        pastryChefService.delete(id);
        logger.info("Deleted pastry chef with id: {}", id);
        return ResponseEntity.noContent().build();
    }
}
