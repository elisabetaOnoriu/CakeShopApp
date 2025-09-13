package com.awbd.cakeshop.unitTests;

import com.awbd.cakeshop.DTOs.OrderDTO;
import com.awbd.cakeshop.DTOs.OrderRequestDTO;
import com.awbd.cakeshop.controllers.CakeOrderController;
import com.awbd.cakeshop.mappers.OrderMapper;
import com.awbd.cakeshop.models.Cake;
import com.awbd.cakeshop.models.Cart;
import com.awbd.cakeshop.models.Order;
import com.awbd.cakeshop.models.User;
import com.awbd.cakeshop.services.CartService;
import com.awbd.cakeshop.services.OrderService;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CakeOrderControllerTest {

    @Mock
    private OrderService orderService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserService userService;

    @Mock
    private CartService cartService;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private CakeOrderController orderController;

    private User user;
    private Cart cart;
    private Order order;
    private OrderDTO orderDTO;
    private OrderRequestDTO orderRequestDTO;
    private Cake cake;
    private String validAuthHeader;
    private String token;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        cake = new Cake();
        cake.setId(1L);
        cake.setName("Test Cake");

        cart = new Cart();
        cart.setId(1L);
        cart.setCakes(Set.of(cake));

        order = new Order();
        order.setId(1L);
        order.setUserId(1L);
        order.setOrderDate(LocalDateTime.now());
        order.setTotalPrice(29.99);

        orderDTO = new OrderDTO();
        orderDTO.setId(1L);
        orderDTO.setUserId(1L);
        orderDTO.setCakeIds(Set.of(1L));
        orderDTO.setOrderDate(LocalDateTime.now());
        orderDTO.setTotalPrice(29.99);

        orderRequestDTO = new OrderRequestDTO();
        orderRequestDTO.setSaleId(1L);

        token = "validToken123";
        validAuthHeader = "Bearer " + token;
    }

    @Test
    void createOrder_Success() {
        when(jwtUtil.getUsernameFromToken(token)).thenReturn("testuser");
        when(userService.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(cartService.getCartByUserId(1L)).thenReturn(Optional.of(cart));
        when(orderService.createOrder(1L, List.of(1L), 1L)).thenReturn(order);
        when(orderMapper.toDto(order)).thenReturn(orderDTO);
        doNothing().when(cartService).clearCart(1L);

        ResponseEntity<OrderDTO> result = orderController.createOrder(validAuthHeader, orderRequestDTO);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(orderDTO, result.getBody());
        verify(jwtUtil).getUsernameFromToken(token);
        verify(userService).findByUsername("testuser");
        verify(cartService).getCartByUserId(1L);
        verify(orderService).createOrder(1L, List.of(1L), 1L);
        verify(cartService).clearCart(1L);
        verify(orderMapper).toDto(order);
    }

    @Test
    void getUserOrderHistory_Success() {
        List<Order> orders = List.of(order);
        List<OrderDTO> orderDTOs = List.of(orderDTO);

        when(jwtUtil.getUsernameFromToken(token)).thenReturn("testuser");
        when(userService.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(orderService.getUserOrderHistory(user)).thenReturn(orders);
        when(orderMapper.toDtoList(orders)).thenReturn(orderDTOs);

        ResponseEntity<List<OrderDTO>> result = orderController.getUserOrderHistory(validAuthHeader);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1, result.getBody().size());
        assertEquals(orderDTO, result.getBody().get(0));
        verify(jwtUtil).getUsernameFromToken(token);
        verify(userService).findByUsername("testuser");
        verify(orderService).getUserOrderHistory(user);
        verify(orderMapper).toDtoList(orders);
    }

    @Test
    void getAllOrders_Success() {
        List<Order> orders = List.of(order);
        List<OrderDTO> orderDTOs = List.of(orderDTO);

        when(orderService.getAllOrders()).thenReturn(orders);
        when(orderMapper.toDtoList(orders)).thenReturn(orderDTOs);

        ResponseEntity<List<OrderDTO>> result = orderController.getAllOrders();

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1, result.getBody().size());
        assertEquals(orderDTO, result.getBody().get(0));
        verify(orderService).getAllOrders();
        verify(orderMapper).toDtoList(orders);
    }

    @Test
    void updateOrder_Success() {
        Order updatedOrder = new Order();
        updatedOrder.setId(1L);
        updatedOrder.setUserId(1L);
        updatedOrder.setTotalPrice(35.99);

        OrderDTO updatedDTO = new OrderDTO();
        updatedDTO.setId(1L);
        updatedDTO.setUserId(1L);
        updatedDTO.setTotalPrice(35.99);

        when(orderService.updateOrder(1L, 1L, orderDTO.getCakeIds())).thenReturn(updatedOrder);
        when(orderMapper.toDto(updatedOrder)).thenReturn(updatedDTO);

        ResponseEntity<OrderDTO> result = orderController.updateOrder(1L, orderDTO);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(updatedDTO, result.getBody());
        verify(orderService).updateOrder(1L, 1L, orderDTO.getCakeIds());
        verify(orderMapper).toDto(updatedOrder);
    }
}
