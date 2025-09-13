package com.awbd.cakeshop.unitTests;

import com.awbd.cakeshop.DTOs.CakeDTO;
import com.awbd.cakeshop.DTOs.CartDTO;
import com.awbd.cakeshop.controllers.CakeCartController;
import com.awbd.cakeshop.mappers.CakeMapper;
import com.awbd.cakeshop.mappers.CartMapper;
import com.awbd.cakeshop.models.Cake;
import com.awbd.cakeshop.models.Cart;
import com.awbd.cakeshop.models.User;
import com.awbd.cakeshop.services.CartService;
import com.awbd.cakeshop.services.UserService;
import com.awbd.cakeshop.utils.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CakeCartControllerTest {

    @Mock
    private CartService cartService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserService userService;

    @Mock
    private CartMapper cartMapper;

    @Mock
    private CakeMapper cakeMapper;

    @InjectMocks
    private CakeCartController cartController;

    private User user;
    private Cart cart;
    private CartDTO cartDTO;
    private Cake cake;
    private CakeDTO cakeDTO;
    private String validAuthHeader;
    private String token;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        cart = new Cart();
        cart.setId(1L);
        cart.setUserId(1L);

        cartDTO = new CartDTO();
        cartDTO.setId(1L);
        cartDTO.setUserId(1L);
        cartDTO.setCakeIds(Set.of(1L));

        cake = new Cake();
        cake.setId(1L);
        cake.setName("Test Cake");

        cakeDTO = new CakeDTO();
        cakeDTO.setId(1L);
        cakeDTO.setName("Test Cake");

        token = "validToken123";
        validAuthHeader = "Bearer " + token;
    }

    @Test
    void addCakeToCart_Success() {
        when(jwtUtil.getUsernameFromToken(token)).thenReturn("testuser");
        when(userService.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(cartService.getCartByUserId(1L)).thenReturn(Optional.ofNullable(cart));
        doNothing().when(cartService).addCakeToCart(1L, 1L);
        when(cartMapper.toDto(cart)).thenReturn(cartDTO);

        ResponseEntity<CartDTO> result = cartController.addCakeToCart(1L, validAuthHeader);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(cartDTO, result.getBody());
        verify(jwtUtil, times(1)).getUsernameFromToken(token);
        verify(userService, times(1)).findByUsername("testuser");
        verify(cartService, times(1)).getCartByUserId(1L);
        verify(cartService, times(1)).addCakeToCart(1L, 1L);
        verify(cartMapper, times(1)).toDto(cart);
    }

    @Test
    void getCartCakes_Success() {
        List<Cake> cakesInCart = Arrays.asList(cake);
        List<CakeDTO> cakeDTOs = Arrays.asList(cakeDTO);

        when(jwtUtil.getUsernameFromToken(token)).thenReturn("testuser");
        when(userService.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(cartService.getCartByUserId(1L)).thenReturn(Optional.ofNullable(cart));
        when(cartService.getCakesInCart(1L)).thenReturn(cakesInCart);
        when(cakeMapper.toDtoList(cakesInCart)).thenReturn(cakeDTOs);

        ResponseEntity<List<CakeDTO>> result = cartController.getCartCakes(validAuthHeader);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1, result.getBody().size());
        assertEquals("Test Cake", result.getBody().get(0).getName());
        verify(jwtUtil, times(1)).getUsernameFromToken(token);
        verify(userService, times(1)).findByUsername("testuser");
        verify(cartService, times(1)).getCartByUserId(1L);
        verify(cartService, times(1)).getCakesInCart(1L);
        verify(cakeMapper, times(1)).toDtoList(cakesInCart);
    }

    @Test
    void getTotalPrice_Success() {
        double totalPrice = 99.99;

        when(jwtUtil.getUsernameFromToken(token)).thenReturn("testuser");
        when(userService.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(cartService.getCartByUserId(1L)).thenReturn(Optional.ofNullable(cart));
        when(cartService.calculateTotalPrice(1L)).thenReturn(totalPrice);

        ResponseEntity<Double> result = cartController.getTotalPrice(validAuthHeader);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(totalPrice, result.getBody());
        verify(jwtUtil, times(1)).getUsernameFromToken(token);
        verify(userService, times(1)).findByUsername("testuser");
        verify(cartService, times(1)).getCartByUserId(1L);
        verify(cartService, times(1)).calculateTotalPrice(1L);
    }
}
