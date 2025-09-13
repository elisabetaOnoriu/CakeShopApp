package com.awbd.cakeshop.controllers;

import com.awbd.cakeshop.DTOs.ReviewDTO;
import com.awbd.cakeshop.mappers.ReviewMapper;
import com.awbd.cakeshop.models.Review;
import com.awbd.cakeshop.services.ReviewService;
import com.awbd.cakeshop.utils.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/cakes/{cakeId}/reviews")
public class ReviewController {

    private final ReviewService reviewService;
    private final ReviewMapper mapper;
    private final JwtUtil jwtUtil;

    public ReviewController(ReviewService reviewService, ReviewMapper mapper, JwtUtil jwtUtil) {
        this.reviewService = reviewService;
        this.mapper = mapper;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping
    public List<ReviewDTO> list(@PathVariable Long cakeId) {
        return reviewService.getByCake(cakeId).stream().map(mapper::toDto).toList();
    }

    @PostMapping
    public ResponseEntity<ReviewDTO> create(@PathVariable Long cakeId,
                                            @RequestHeader(value = "Authorization", required = false) String auth,
                                            @RequestBody ReviewCreateReq req) {
        String token = (auth != null && auth.startsWith("Bearer ")) ? auth.substring(7) : null;
        String username = (token != null) ? jwtUtil.getUsernameFromToken(token) : null;

        // (opțional) validare simplă
        int rating = req.rating();
        if (rating < 1 || rating > 5) {
            return ResponseEntity.badRequest().build();
        }

        Review saved = reviewService.add(cakeId, username, rating, req.comment());
        ReviewDTO dto = mapper.toDto(saved);
        return ResponseEntity
                .created(URI.create("/api/cakes/" + cakeId + "/reviews/" + saved.getId()))
                .body(dto);
    }

    /** DTO-ul cererii – are metodele rating() și comment() */
    public static record ReviewCreateReq(int rating, String comment) {}
}
