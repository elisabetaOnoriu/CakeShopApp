package com.awbd.cakeshop.repositories;// package com.awbd.cakeshop.repositories;
import com.awbd.cakeshop.models.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByCakeIdOrderByCreatedAtDesc(Long cakeId);
}
