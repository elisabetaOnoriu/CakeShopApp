package com.awbd.cakeshop.repositories;

import com.awbd.cakeshop.models.Cart;
import com.awbd.cakeshop.models.User;
import com.awbd.cakeshop.models.Cake;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    @Query("SELECT c FROM Cart c WHERE c.user.id = :userId")
    Optional<Cart> findByUserId(@Param("userId") Long userId);

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Cart c WHERE c.user.id = :userId")
    boolean existsByUserId(@Param("userId") Long userId);

    @Query("SELECT CASE WHEN COUNT(ca) > 0 THEN true ELSE false END FROM Cart c JOIN c.cakes ca WHERE c.id = :cartId AND ca.id = :cakeId")
    boolean existsCakeInCart(@Param("cartId") Long cartId, @Param("cakeId") Long cakeId);

    @Query("SELECT SUM(ca.price) FROM Cart c JOIN c.cakes ca WHERE c.id = :cartId")
    double calculateTotalPrice(@Param("cartId") Long cartId);

    @Query("SELECT ca FROM Cart c JOIN c.cakes ca WHERE c.id = :cartId")
    List<Cake> findCakesInCart(@Param("cartId") Long cartId);

    void deleteByUser(User user);
}
