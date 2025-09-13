package com.awbd.cakeshop.mappers;

import com.awbd.cakeshop.DTOs.CategoryDTO;
import com.awbd.cakeshop.models.Cake;
import com.awbd.cakeshop.models.Category;
import com.awbd.cakeshop.models.Sale;
import com.awbd.cakeshop.repositories.CakeRepository;
import com.awbd.cakeshop.repositories.SaleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CategoryMapper {

    private final CakeRepository cakeRepository;
    private final SaleRepository saleRepository;

    @Autowired
    public CategoryMapper(CakeRepository cakeRepository, SaleRepository saleRepository) {
        this.cakeRepository = cakeRepository;
        this.saleRepository = saleRepository;
    }

    public CategoryDTO toDto(Category category) {
        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());

        if (category.getCakes() != null && !category.getCakes().isEmpty()) {
            List<Long> cakeIds = category.getCakes().stream()
                    .map(Cake::getId)
                    .collect(Collectors.toList());
            dto.setCakeIds(cakeIds);
        }

        if (category.getSales() != null && !category.getSales().isEmpty()) {
            List<Long> saleIds = category.getSales().stream()
                    .map(Sale::getId)
                    .collect(Collectors.toList());
            dto.setSaleIds(saleIds);
        }

        return dto;
    }

    public Category toEntity(CategoryDTO dto) {
        Category category = new Category();
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        return category;
    }

    public List<CategoryDTO> toDtoList(List<Category> categories) {
        return categories.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public void updateEntityFromDto(CategoryDTO dto, Category category) {
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
    }
}
