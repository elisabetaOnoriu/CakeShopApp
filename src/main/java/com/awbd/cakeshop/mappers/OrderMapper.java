package com.awbd.cakeshop.mappers;

import com.awbd.cakeshop.DTOs.OrderDTO;
import com.awbd.cakeshop.models.Cake;
import com.awbd.cakeshop.models.Order;
import com.awbd.cakeshop.models.User;
import com.awbd.cakeshop.repositories.CakeRepository;
import com.awbd.cakeshop.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class OrderMapper {

    private final UserRepository userRepository;
    private final CakeRepository cakeRepository;

    @Autowired
    public OrderMapper(UserRepository userRepository, CakeRepository cakeRepository) {
        this.userRepository = userRepository;
        this.cakeRepository = cakeRepository;
    }

    public OrderDTO toDto(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setUserId(order.getUser().getId());
        dto.setCakeIds(order.getCakes().stream()
                .map(Cake::getId)
                .collect(Collectors.toSet()));
        dto.setOrderDate(order.getOrderDate());
        dto.setTotalPrice(order.getTotalPrice());
        return dto;
    }

    public Order toEntity(OrderDTO orderDTO) {
        Order order = new Order();

        User user = userRepository.findById(orderDTO.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + orderDTO.getUserId()));
        order.setUser(user);

        Set<Cake> cakes = orderDTO.getCakeIds().stream()
                .map(cakeId -> cakeRepository.findById(cakeId)
                        .orElseThrow(() -> new RuntimeException("Cake not found with id: " + cakeId)))
                .collect(Collectors.toSet());
        order.setCakes(cakes);

        order.setOrderDate(orderDTO.getOrderDate());
        order.setTotalPrice(orderDTO.getTotalPrice());

        return order;
    }

    public List<OrderDTO> toDtoList(List<Order> orders) {
        return orders.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}
