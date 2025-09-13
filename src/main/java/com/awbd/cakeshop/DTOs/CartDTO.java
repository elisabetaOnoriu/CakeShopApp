package com.awbd.cakeshop.DTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartDTO {
    private Long id;
    private Long userId;
    private Set<Long> cakeIds;

    public CartDTO(Long userId, Set<Long> cakeIds) {
        this.userId = userId;
        this.cakeIds = cakeIds;
    }

    public CartDTO(Long userId) {
        this.userId = userId;
    }
}
