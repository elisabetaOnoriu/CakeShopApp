package com.awbd.cakeshop.DTOs;// package com.awbd.cakeshop.DTOs;
import java.time.Instant;

public record ReviewDTO(
        Long id,
        int rating,
        String comment,
        String username,
        Instant createdAt
) {}
