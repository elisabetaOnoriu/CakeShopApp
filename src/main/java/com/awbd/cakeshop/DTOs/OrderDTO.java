package com.awbd.cakeshop.DTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {

    private Long id;
    private Long userId;
    private Set<Long> cakeIds;
    private LocalDateTime orderDate;
    private Double totalPrice;

    public OrderDTO(Long userId, Set<Long> cakeIds, Double totalPrice) {
        this.userId = userId;
        this.cakeIds = cakeIds;
        this.orderDate = LocalDateTime.now();
        this.totalPrice = totalPrice;
    }
}
