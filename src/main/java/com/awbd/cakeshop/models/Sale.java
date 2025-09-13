package com.awbd.cakeshop.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "sales")
@Getter
@Setter
@NoArgsConstructor
public class Sale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String saleCode = generateSaleCode();

    @Column(nullable = false)
    private Double discountPercentage;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    private String description;

    @Column(nullable = false)
    private Boolean isActive = true;

    @ManyToMany
    @JoinTable(
            name = "sale_category",
            joinColumns = @JoinColumn(name = "sale_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private List<Category> categories = new ArrayList<>();

    // Constructor custom
    public Sale(Double discountPercentage, LocalDate startDate, LocalDate endDate) {
        this.discountPercentage = discountPercentage;
        this.startDate = startDate;
        this.endDate = endDate;
        this.saleCode = generateSaleCode();
        this.isActive = true;
    }

    // Generează un cod unic de reducere
    private String generateSaleCode() {
        return "SALE-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public void addCategory(Category category) {
        if (!categories.contains(category)) {
            categories.add(category);
            category.getSales().add(this);
        }
    }

    public void removeCategory(Category category) {
        if (categories.contains(category)) {
            categories.remove(category);
            category.getSales().remove(this);
        }
    }

    // Verifică și actualizează starea promoției
    public boolean updateStatusIfNeeded() {
        LocalDate today = LocalDate.now();
        boolean active = !today.isBefore(startDate) && !today.isAfter(endDate);
        this.isActive = active;
        return active;
    }
}
