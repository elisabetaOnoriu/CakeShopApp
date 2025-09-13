package com.awbd.cakeshop.mappers;

import com.awbd.cakeshop.DTOs.CartDTO;
import com.awbd.cakeshop.models.Cart;
import com.awbd.cakeshop.models.Cake;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class CartMapper {

    public CartDTO toDto(Cart cart) {
        CartDTO dto = new CartDTO();
        dto.setId(cart.getId());
        dto.setUserId(cart.getUser().getId());

        if (cart.getCakes() != null && !cart.getCakes().isEmpty()) {
            Set<Long> cakeIds = cart.getCakes().stream()
                    .map(Cake::getId)
                    .collect(Collectors.toSet());
            dto.setCakeIds(cakeIds);
        }

        return dto;
    }
}
