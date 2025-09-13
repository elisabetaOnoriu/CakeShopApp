package com.awbd.cakeshop.services;

import com.awbd.cakeshop.exceptions.category.CategoryAlreadyExistsException;
import com.awbd.cakeshop.exceptions.category.CategoryNotFoundException;
import com.awbd.cakeshop.exceptions.category.DuplicateCategoryException;
import com.awbd.cakeshop.models.Cake;
import com.awbd.cakeshop.models.Category;
import com.awbd.cakeshop.repositories.CategoryRepository;
import jakarta.transaction.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public Category createCategory(Category category) {
        // sanitize / validate
        String rawName = category.getName();
        String name = rawName == null ? null : rawName.trim();

        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be empty");
        }

        // opțional: normalizare pentru verificarea de duplicat (ex. case-insensitive)
        // dacă vrei match case-insensitive, folosește existsByNameIgnoreCase în repo.
        if (categoryRepository.existsByName(name)) {
            throw new CategoryAlreadyExistsException(
                    "Category with name '" + name + "' already exists"
            );
        }

        category.setName(name);

        try {
            return categoryRepository.save(category);
        } catch (DataIntegrityViolationException e) {
            throw new CategoryAlreadyExistsException(
                    "Category with name '" + name + "' already exists"
            );
        }
    }

    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Category with ID " + id + " not found"));
    }

    public List<Cake> getCakesInCategory(Long categoryId) {
        Category category = getCategoryById(categoryId);
        return category.getCakes();
    }

    public void delete(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new CategoryNotFoundException("Category with ID " + id + " not found");
        }
        categoryRepository.deleteById(id);
    }

    public Category update(Long id, Category updatedCategory) {
        String newName = updatedCategory.getName() == null ? null : updatedCategory.getName().trim();

        return categoryRepository.findById(id)
                .map(existingCategory -> {
                    if (newName == null || newName.isEmpty()) {
                        throw new IllegalArgumentException("Category name cannot be empty");
                    }
                    if (!existingCategory.getName().equals(newName)
                            && categoryRepository.existsByName(newName)) {
                        throw new DuplicateCategoryException(
                                "Category with name '" + newName + "' already exists"
                        );
                    }

                    existingCategory.setName(newName);
                    existingCategory.setDescription(updatedCategory.getDescription());

                    return categoryRepository.save(existingCategory);
                })
                .orElseThrow(() -> new CategoryNotFoundException("Category with ID " + id + " not found"));
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }
}
