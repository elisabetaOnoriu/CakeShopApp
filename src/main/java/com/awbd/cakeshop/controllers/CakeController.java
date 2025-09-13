package com.awbd.cakeshop.controllers;

import com.awbd.cakeshop.DTOs.CakeDTO;
import com.awbd.cakeshop.mappers.CakeMapper;
import com.awbd.cakeshop.models.Cake;
import com.awbd.cakeshop.services.CakeService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@RestController
@RequestMapping("/api/cakes")
public class CakeController {

    private final CakeService cakeService;
    private final CakeMapper cakeMapper;
    private static final Logger log = LoggerFactory.getLogger(CakeController.class);

    public CakeController(CakeService cakeService, CakeMapper cakeMapper) {
        this.cakeService = cakeService;
        this.cakeMapper = cakeMapper;
    }

    /**
     * GET /api/cakes
     *   - fără parametri: toate prăjiturile
     *   - ?categoryId=ID: filtrează pe categorie
     *   - ?q=text: caută după nume (contains, case-insensitive – după implementarea repo)
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<CakeDTO> getAllCakes(
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "q", required = false) String q) {

        if (categoryId != null) {
            log.info("Fetching cakes by categoryId={}", categoryId);
            return cakeMapper.toDtoList(cakeService.getCakesByCategoryId(categoryId));
        }
        if (q != null && !q.isBlank()) {
            log.info("Searching cakes by name q='{}'", q);
            return cakeMapper.toDtoList(cakeService.searchByName(q));
        }

        log.info("Fetching all cakes");
        return cakeMapper.toDtoList(cakeService.getAllCakes());
    }

    /** GET /api/cakes/{id} – un singur cake. */
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CakeDTO> getOne(@PathVariable Long id) {
        Optional<Cake> opt = cakeService.findById(id);
        return opt
                .map(cakeMapper::toDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> {
                    log.info("Cake id={} not found", id);
                    return ResponseEntity.notFound().build();
                });
    }

    /** GET /api/cakes/search/{name} – shortcut pentru căutare. */
    @GetMapping(value = "/search/{name}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<CakeDTO> searchCakes(@PathVariable String name) {
        log.info("Searching cakes with name path='{}'", name);
        return cakeMapper.toDtoList(cakeService.searchByName(name));
    }

    /** POST /api/cakes – create (ADMIN). */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CakeDTO> addCake(@RequestBody @Valid CakeDTO dto) {
        Cake toSave = cakeMapper.toEntity(dto);
        Cake saved = cakeService.addCake(toSave);
        log.info("Added cake id={}, name='{}'", saved.getId(), saved.getName());
        return ResponseEntity
                .created(URI.create("/api/cakes/" + saved.getId()))
                .body(cakeMapper.toDto(saved));
    }

    /** PUT /api/cakes/{id} – update (ADMIN). */
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CakeDTO> updateCake(@PathVariable Long id,
                                              @RequestBody @Valid CakeDTO dto) {
        try {
            Cake updated = cakeService.update(id, cakeMapper.toEntity(dto));
            log.info("Updated cake id={}", id);
            return ResponseEntity.ok(cakeMapper.toDto(updated));
        } catch (NoSuchElementException | IllegalArgumentException e) {
            log.info("Update failed, cake id={} not found", id);
            return ResponseEntity.notFound().build();
        }
    }

    /** GET /api/cakes/available – doar cele cu stock > 0. */
    @GetMapping(value = "/available", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<CakeDTO> getAvailableCakes() {
        log.info("Fetching available cakes");
        return cakeMapper.toDtoList(cakeService.getCakesInStock());
    }

    /** DELETE /api/cakes/{id} – delete (ADMIN). */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCake(@PathVariable Long id) {
        cakeService.deleteCake(id);
        log.info("Deleted cake id={}", id);
        return ResponseEntity.noContent().build();
    }
}
