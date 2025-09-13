package com.awbd.cakeshop.services;

import com.awbd.cakeshop.exceptions.cake.CakeNotFoundException;
import com.awbd.cakeshop.exceptions.cake.OutOfStockException;
import com.awbd.cakeshop.exceptions.cart.CakeAlreadyInCartException;
import com.awbd.cakeshop.exceptions.cart.CartNotFoundException;
import com.awbd.cakeshop.exceptions.user.UserNotFoundException;
import com.awbd.cakeshop.models.Cake;
import com.awbd.cakeshop.models.Cart;
import com.awbd.cakeshop.models.User;
import com.awbd.cakeshop.repositories.CakeRepository;
import com.awbd.cakeshop.repositories.CartRepository;
import com.awbd.cakeshop.repositories.UserRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CakeRepository cakeRepository;
    private final UserRepository userRepository;
    private final SaleService saleService;
    private final EntityManager entityManager;

    @Transactional
    public Cart createCartForUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("User with ID " + userId + " not found");
        }
        User userRef = entityManager.getReference(User.class, userId);
        Cart cart = new Cart(userRef);
        return cartRepository.save(cart);
    }

    public Optional<Cart> getCartByUserId(Long userId) {
        return Optional.ofNullable(cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException("Cart not found for user with ID " + userId)));
    }

    @Transactional
    public void addCakeToCart(Long cartId, Long cakeId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new CartNotFoundException("Cart with ID " + cartId + " not found"));

        if (cartRepository.existsCakeInCart(cartId, cakeId)) {
            throw new CakeAlreadyInCartException("Cake already in Cart");
        }

        Cake cake = cakeRepository.findById(cakeId)
                .orElseThrow(() -> new CakeNotFoundException("Cake with ID " + cakeId + " not found"));

        if (cake.getStock() <= 0) {
            throw new OutOfStockException("Cake is out of stock.");
        }

        cart.addCake(cake);
        cartRepository.save(cart);
    }

    @Transactional
    public void clearCart(Long cartId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new CartNotFoundException("Cart with ID " + cartId + " not found"));

        cart.getCakes().clear();
        cartRepository.save(cart);
    }

    public double calculateTotalPrice(Long cartId) {
        if (!cartRepository.existsById(cartId)) {
            throw new CartNotFoundException("Cart with ID " + cartId + " not found");
        }
        return cartRepository.calculateTotalPrice(cartId);
    }

    public List<Cake> getCakesInCart(Long cartId) {
        if (!cartRepository.existsById(cartId)) {
            throw new CartNotFoundException("Cart with ID " + cartId + " not found");
        }
        return cartRepository.findCakesInCart(cartId);
    }
}
