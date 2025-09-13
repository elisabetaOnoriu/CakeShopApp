package com.awbd.cakeshop.mappers;

import com.awbd.cakeshop.DTOs.SaleDTO;
import com.awbd.cakeshop.models.Category;
import com.awbd.cakeshop.models.Sale;
import com.awbd.cakeshop.repositories.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Component
public class SaleMapper {

    private final CategoryRepository categoryRepository;

    @Autowired
    public SaleMapper(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public SaleDTO toDto(Sale sale) {
        SaleDTO dto = new SaleDTO();
        dto.setId(sale.getId());
        dto.setSaleCode(sale.getSaleCode());
        dto.setDiscountPercentage(sale.getDiscountPercentage());
        dto.setStartDate(sale.getStartDate());
        dto.setEndDate(sale.getEndDate());
        dto.setDescription(sale.getDescription());
        dto.setIsActive(sale.getIsActive());

        if (sale.getCategories() != null && !sale.getCategories().isEmpty()) {
            dto.setCategoryIds(
                    sale.getCategories().stream()
                            .map(Category::getId)
                            .collect(Collectors.toList())
            );
        }

        return dto;
    }

    public Sale toEntity(SaleDTO dto) {
        Sale sale = new Sale();
        sale.setDiscountPercentage(dto.getDiscountPercentage());
        sale.setStartDate(dto.getStartDate());
        sale.setEndDate(dto.getEndDate());
        sale.setDescription(dto.getDescription());
        sale.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);

        if (dto.getCategoryIds() != null && !dto.getCategoryIds().isEmpty()) {
            for (Long categoryId : dto.getCategoryIds()) {
                Category category = categoryRepository.findById(categoryId)
                        .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + categoryId));
                sale.addCategory(category);
            }
        }

        return sale;
    }

    public List<SaleDTO> toDtoList(List<Sale> sales) {
        return sales.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public void updateEntityFromDto(SaleDTO dto, Sale sale) {
        sale.setDiscountPercentage(dto.getDiscountPercentage());
        sale.setStartDate(dto.getStartDate());
        sale.setEndDate(dto.getEndDate());
        sale.setDescription(dto.getDescription());

        if (dto.getIsActive() != null) {
            sale.setIsActive(dto.getIsActive());
        }

        if (dto.getCategoryIds() != null) {
            // Elimină categoriile care nu mai sunt prezente în DTO
            List<Category> existingCategories = new ArrayList<>(sale.getCategories());
            for (Category existing : existingCategories) {
                if (!dto.getCategoryIds().contains(existing.getId())) {
                    sale.removeCategory(existing);
                }
            }

            // Adaugă categoriile noi din DTO
            for (Long categoryId : dto.getCategoryIds()) {
                boolean alreadyLinked = sale.getCategories().stream()
                        .anyMatch(cat -> cat.getId() == categoryId);
                if (!alreadyLinked) {
                    Category category = categoryRepository.findById(categoryId)
                            .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + categoryId));
                    sale.addCategory(category);
                }
            }
        }
    }
}
