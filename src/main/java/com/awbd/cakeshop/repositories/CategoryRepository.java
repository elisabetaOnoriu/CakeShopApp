package com.awbd.cakeshop.repositories;

import com.awbd.cakeshop.models.Cake;
import com.awbd.cakeshop.models.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Category findByName(String name);
    boolean existsByName(String name);

    // torturi dintr-o categorie
    @Query("SELECT c FROM Cake c WHERE c.category.id = :categoryId")
    List<Cake> findCakesByCategoryId(@Param("categoryId") Long categoryId);

    // categorii cu promotii active
    @Query("SELECT c FROM Category c JOIN c.sales s WHERE s.isActive = true")
    List<Category> findCategoriesWithActiveSales();
}
