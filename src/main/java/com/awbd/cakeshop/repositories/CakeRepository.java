package com.awbd.cakeshop.repositories;

import com.awbd.cakeshop.models.Cake;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface CakeRepository extends JpaRepository<Cake, Long> {

    // căutare după nume
    List<Cake> findByNameContaining(String name);

    // căutare după categorie
    List<Cake> findByCategoryId(Long categoryId);

    // căutare după cofetar
    List<Cake> findByPastryChefId(Long pastryChefId);

    // actualizare stoc după comandă
    @Modifying
    @Transactional
    @Query("UPDATE Cake c SET c.stock = c.stock - :quantity WHERE c.id = :cakeId AND c.stock >= :quantity")
    int updateStock(@Param("cakeId") Long cakeId, @Param("quantity") int quantity);

    // update pentru admin
    @Modifying
    @Transactional
    @Query("UPDATE Cake c SET c.name = :name, c.price = :price, c.stock = :stock, c.weight = :weight, c.category.id = :categoryId, c.pastryChef.id = :pastryChefId WHERE c.id = :id")
    void updateCakeDetails(
            @Param("id") Long id,
            @Param("name") String name,
            @Param("price") double price,
            @Param("stock") int stock,
            @Param("weight") double weight,
            @Param("categoryId") Long categoryId,
            @Param("pastryChefId") Long pastryChefId
    );

    // filtre preț
    List<Cake> findByPriceLessThan(double maxPrice);
    List<Cake> findByPriceBetween(double minPrice, double maxPrice);

    // torturi cu stoc pozitiv
    List<Cake> findByStockGreaterThan(int stock);

    // număr review-uri
    @Query("SELECT COUNT(r) FROM Cake c JOIN c.reviews r WHERE c.id = :cakeId")
    long countReviews(@Param("cakeId") Long cakeId);

    // rating mediu
    @Query("SELECT AVG(r.rating) FROM Cake c JOIN c.reviews r WHERE c.id = :cakeId")
    Double getAverageRating(@Param("cakeId") Long cakeId);
}
