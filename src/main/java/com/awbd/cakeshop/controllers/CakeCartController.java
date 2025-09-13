package com.awbd.cakeshop.controllers;

import com.awbd.cakeshop.DTOs.CakeDTO;
import com.awbd.cakeshop.DTOs.CartDTO;
import com.awbd.cakeshop.DTOs.CartDTO;
import com.awbd.cakeshop.exceptions.token.InvalidTokenException;
import com.awbd.cakeshop.exceptions.user.UserNotFoundException;
import com.awbd.cakeshop.mappers.CakeMapper;
import com.awbd.cakeshop.mappers.CartMapper;
import com.awbd.cakeshop.mappers.CartMapper;
import com.awbd.cakeshop.models.Cake;
import com.awbd.cakeshop.models.Cart;
import com.awbd.cakeshop.models.Cart;
import com.awbd.cakeshop.models.User;
import com.awbd.cakeshop.services.CartService;
import com.awbd.cakeshop.services.CartService;
import com.awbd.cakeshop.services.UserService;
import com.awbd.cakeshop.utils.JwtUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class CakeCartController {

    private final CartService cakeCartService;
    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final CartMapper cakeCartMapper;
    private final CakeMapper cakeMapper;

    private static final Logger logger = LoggerFactory.getLogger(CakeCartController.class);

    public CakeCartController(CartService cakeCartService,
                              JwtUtil jwtUtil,
                              UserService userService,
                              CartMapper cakeCartMapper,
                              CakeMapper cakeMapper) {
        this.cakeCartService = cakeCartService;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
        this.cakeCartMapper = cakeCartMapper;
        this.cakeMapper = cakeMapper;
    }

    @PostMapping("/{cakeId}")
    public ResponseEntity<CartDTO> addCakeToCart(
            @PathVariable Long cakeId,
            @RequestHeader("Authorization") String authHeader) {

        validateAuthHeader(authHeader);
        String token = authHeader.substring(7);
        String username = jwtUtil.getUsernameFromToken(token);

        User user = getUserByUsername(username);
        Cart cart = cakeCartService.getCartByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Cart not found for user: " + user.getId()));

        cakeCartService.addCakeToCart(cart.getId(), cakeId);
        logger.info("Cake with ID {} added to cart with ID {}", cakeId, cart.getId());

        return ResponseEntity.ok(cakeCartMapper.toDto(cart));
    }

    @GetMapping("/cakes")
    public ResponseEntity<List<CakeDTO>> getCartCakes(@RequestHeader("Authorization") String authHeader) {
        validateAuthHeader(authHeader);
        String token = authHeader.substring(7);
        String username = jwtUtil.getUsernameFromToken(token);

        User user = getUserByUsername(username);
        Cart cart = cakeCartService.getCartByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Cart not found for user: " + user.getId()));

        List<Cake> cakesInCart = cakeCartService.getCakesInCart(cart.getId());
        logger.info("Retrieved cakes for cart with ID {}", cart.getId());

        return ResponseEntity.ok(cakeMapper.toDtoList(cakesInCart));
    }

    @GetMapping("/total")
    public ResponseEntity<Double> getTotalPrice(@RequestHeader("Authorization") String authHeader) {
        validateAuthHeader(authHeader);
        String token = authHeader.substring(7);
        String username = jwtUtil.getUsernameFromToken(token);

        User user = getUserByUsername(username);
        Cart cart = cakeCartService.getCartByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Cart not found for user: " + user.getId()));

        double totalPrice = cakeCartService.calculateTotalPrice(cart.getId());
        logger.info("Total price for cart with ID {}: {}", cart.getId(), totalPrice);

        return ResponseEntity.ok(totalPrice);
    }

    // Helpers

    private void validateAuthHeader(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.error("Missing or invalid authorization header");
            throw new InvalidTokenException("Missing or invalid authorization header");
        }
    }

    private User getUserByUsername(String username) {
        if (username == null) {
            logger.error("Invalid token (username is null)");
            throw new InvalidTokenException("Invalid token");
        }

        return userService.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));
    }
}
