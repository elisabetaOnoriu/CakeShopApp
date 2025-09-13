package com.awbd.cakeshop.mappers;// package com.awbd.cakeshop.mappers;
import com.awbd.cakeshop.DTOs.ReviewDTO;
import com.awbd.cakeshop.models.Review;
import org.springframework.stereotype.Component;

@Component
public class ReviewMapper {
    public ReviewDTO toDto(Review r) {
        String username = r.getUser() != null ? r.getUser().getUsername() : "Anonymous";
        return new ReviewDTO(r.getId(), r.getRating(), r.getComment(), username, r.getCreatedAt());
    }
}
