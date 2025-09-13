package com.awbd.cakeshop.controllers;

import com.awbd.cakeshop.DTOs.CakeDTO;
import com.awbd.cakeshop.DTOs.CategoryDTO;
import com.awbd.cakeshop.mappers.CakeMapper;
import com.awbd.cakeshop.mappers.CategoryMapper;
import com.awbd.cakeshop.models.Cake;
import com.awbd.cakeshop.models.Category;
import com.awbd.cakeshop.services.CategoryService;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CakeCategoryController {

    private final CategoryService categoryService;
    private final CategoryMapper categoryMapper;
    private final CakeMapper cakeMapper;
    private static final Logger logger = LoggerFactory.getLogger(CakeCategoryController.class);

    public CakeCategoryController(CategoryService categoryService,
                                  CategoryMapper categoryMapper,
                                  CakeMapper cakeMapper) {
        this.categoryService = categoryService;
        this.categoryMapper = categoryMapper;
        this.cakeMapper = cakeMapper;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Category> addCategory(@RequestBody @Valid CategoryDTO categoryDto) {
        Category category = categoryMapper.toEntity(categoryDto);
        Category savedCategory = categoryService.createCategory(category);
        logger.info("Category created: {}", savedCategory);
        return ResponseEntity.created(URI.create("/api/categories/" + savedCategory.getId()))
                .body(savedCategory);
    }

    // GET all cakes from a category
    @GetMapping("/{id}/cakes")
    public List<CakeDTO> getCakesInCategory(@PathVariable Long id) {
        List<Cake> cakes = categoryService.getCakesInCategory(id);
        logger.info("Retrieved {} cakes from category with id: {}", cakes.size(), id);
        return cakeMapper.toDtoList(cakes);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.delete(id);
        logger.info("Category with id {} deleted", id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public List<CategoryDTO> getAllCategories() {
        List<Category> categories = categoryService.getAllCategories();
        logger.info("Retrieved {} categories", categories.size());
        return categoryMapper.toDtoList(categories);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Category> updateCategory(
            @PathVariable Long id,
            @RequestBody @Valid CategoryDTO categoryDto) {

        if (categoryDto.getId() != null && !id.equals(categoryDto.getId())) {
            logger.warn("ID mismatch: path ID {} doesn't match body ID {}", id, categoryDto.getId());
            throw new RuntimeException("Id from path does not match with id from request");
        }

        Category category = categoryMapper.toEntity(categoryDto);
        Category updatedCategory = categoryService.update(id, category);
        logger.info("Category with id {} updated", id);
        return ResponseEntity.ok(updatedCategory);
    }
}
