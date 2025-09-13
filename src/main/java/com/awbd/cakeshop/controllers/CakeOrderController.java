package com.awbd.cakeshop.controllers;

import com.awbd.cakeshop.DTOs.OrderRequestDTO;
import com.awbd.cakeshop.DTOs.OrderDTO;
import com.awbd.cakeshop.exceptions.user.UserNotFoundException;
import com.awbd.cakeshop.mappers.OrderMapper;
import com.awbd.cakeshop.models.Cake;
import com.awbd.cakeshop.models.Cart;
import com.awbd.cakeshop.models.Order;
import com.awbd.cakeshop.models.User;
import com.awbd.cakeshop.services.CartService;
import com.awbd.cakeshop.services.OrderService;
import com.awbd.cakeshop.services.UserService;
import com.awbd.cakeshop.utils.JwtUtil;
import com.awbd.cakeshop.annotations.RequireAdmin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
public class CakeOrderController {

    private final OrderService orderService;
    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final CartService cakeCartService;
    private final OrderMapper orderMapper;

    private static final Logger logger = LoggerFactory.getLogger(CakeOrderController.class);

    @Autowired
    public CakeOrderController(OrderService orderService,
                               JwtUtil jwtUtil,
                               UserService userService,
                               CartService cakeCartService,
                               OrderMapper orderMapper) {
        this.orderService = orderService;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
        this.cakeCartService = cakeCartService;
        this.orderMapper = orderMapper;
    }

    @PostMapping
    public ResponseEntity<OrderDTO> createOrder(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody(required = false) OrderRequestDTO request) {

        if (!authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String token = authHeader.substring(7);
        String username = jwtUtil.getUsernameFromToken(token);

        User user = userService.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));

        Cart cart = cakeCartService.getCartByUserId(user.getId())
                .filter(c -> !c.getCakes().isEmpty())
                .orElseThrow(() -> new IllegalStateException("Cake cart not found or empty for user: " + user.getId()));


        List<Long> cakeIds = cart.getCakes().stream()
                .map(Cake::getId)
                .collect(Collectors.toList());

        Long discountId = (request != null) ? request.getDiscountId() : null;

        Order order = orderService.createOrder(user.getId(), cakeIds, discountId);
        cakeCartService.clearCart(cart.getId());

        logger.info("Order created successfully for user: {}", username);
        return ResponseEntity.ok(orderMapper.toDto(order));
    }

    @GetMapping("/history")
    public ResponseEntity<List<OrderDTO>> getUserOrderHistory(
            @RequestHeader("Authorization") String authHeader) {
        if (!authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String token = authHeader.substring(7);
        String username = jwtUtil.getUsernameFromToken(token);

        User user = userService.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));

        List<Order> orders = orderService.getUserOrderHistory(user);
        if (orders.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }

        logger.info("Order history retrieved for user: {}", username);
        return ResponseEntity.ok(orderMapper.toDtoList(orders));
    }

    @GetMapping
    @RequireAdmin
    public ResponseEntity<List<OrderDTO>> getAllOrders() {
        List<Order> orders = orderService.getAllOrders();
        if (orders.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        logger.info("All cake orders retrieved");
        return ResponseEntity.ok(orderMapper.toDtoList(orders));
    }

    @PutMapping("/{id}")
    @RequireAdmin
    public ResponseEntity<OrderDTO> updateOrder(@PathVariable Long id, @RequestBody OrderDTO orderDTO) {
        Order updatedOrder = orderService.updateOrder(id, orderDTO.getUserId(), orderDTO.getCakeIds());
        OrderDTO updatedDTO = orderMapper.toDto(updatedOrder);
        logger.info("Order with ID {} updated successfully", id);
        return ResponseEntity.ok(updatedDTO);
    }
}
