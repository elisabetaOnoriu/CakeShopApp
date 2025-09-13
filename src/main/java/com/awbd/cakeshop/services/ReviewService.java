package com.awbd.cakeshop.services;// package com.awbd.cakeshop.services;
import com.awbd.cakeshop.models.Cake;
import com.awbd.cakeshop.models.Review;
import com.awbd.cakeshop.models.User;
import com.awbd.cakeshop.repositories.CakeRepository;
import com.awbd.cakeshop.repositories.ReviewRepository;
import com.awbd.cakeshop.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReviewService {
    private final ReviewRepository reviewRepo;
    private final CakeRepository cakeRepo;
    private final UserRepository userRepo;

    public ReviewService(ReviewRepository reviewRepo, CakeRepository cakeRepo, UserRepository userRepo) {
        this.reviewRepo = reviewRepo;
        this.cakeRepo = cakeRepo;
        this.userRepo = userRepo;
    }

    public List<Review> getByCake(Long cakeId) {
        return reviewRepo.findByCakeIdOrderByCreatedAtDesc(cakeId);
    }

    @Transactional
    public Review add(Long cakeId, String username, int rating, String comment) {
        Cake cake = cakeRepo.findById(cakeId)
                .orElseThrow(() -> new IllegalArgumentException("Cake not found: " + cakeId));

        User user = (username != null) ? userRepo.findByUsername(username).orElse(null) : null;

        Review r = new Review();
        r.setCake(cake);
        r.setUser(user);
        r.setRating(rating);
        r.setComment(comment);
        return reviewRepo.save(r);
    }
}
