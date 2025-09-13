package com.awbd.cakeshop.repositories;


import com.awbd.cakeshop.models.Order;
import com.awbd.cakeshop.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserOrderByOrderDateDesc(User user);

    List<Order> findAllByOrderByOrderDateDesc();

    @Query("SELECT SUM(b.price) FROM Order o JOIN o.cakes b WHERE o.id = :orderId")
    Double calculateOrderTotal(@Param("orderId") Long orderId);

    void deleteByUser(User user);

    Optional<Order> findFirstByUserOrderByOrderDateDesc(User user);






}