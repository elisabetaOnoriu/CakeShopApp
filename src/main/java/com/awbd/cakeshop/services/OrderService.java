package com.awbd.cakeshop.services;

import com.awbd.cakeshop.exceptions.cake.CakeNotFoundException;
import com.awbd.cakeshop.exceptions.cart.EmptyCartException;
import com.awbd.cakeshop.exceptions.order.OrderNotFoundException;
import com.awbd.cakeshop.exceptions.order.SaleNotFoundException;
import com.awbd.cakeshop.exceptions.user.UserNotFoundException;
import com.awbd.cakeshop.models.*;
import com.awbd.cakeshop.repositories.CakeRepository;
import com.awbd.cakeshop.repositories.OrderRepository;
import com.awbd.cakeshop.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final CakeRepository cakeRepository;
    private final SaleService saleService;

    public OrderService(OrderRepository orderRepository, UserRepository userRepository,
                        CakeRepository cakeRepository, SaleService saleService) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.cakeRepository = cakeRepository;
        this.saleService = saleService;
    }

    public Order createOrder(Long userId, List<Long> cakeIds, Long saleId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with ID " + userId + " not found"));

        if (cakeIds.isEmpty()) {
            throw new EmptyCartException("Cannot create order with empty cake list");
        }

        Order order = new Order(user);

        cakeIds.forEach(cakeId -> {
            Cake cake = cakeRepository.findById(cakeId)
                    .orElseThrow(() -> new CakeNotFoundException("Cake with ID " + cakeId + " not found"));
            order.addCake(cake);
        });

        double totalPrice = 0.0;

        if (saleId == null) {
            totalPrice = order.getCakes().stream()
                    .mapToDouble(Cake::getPrice)
                    .sum();
        } else {
            Sale sale = saleService.getById(saleId);
            if (sale == null) {
                throw new SaleNotFoundException("Sale with ID " + saleId + " not found");
            }
            order.setSale(sale);

            double percentage = sale.getDiscountPercentage();
            List<Category> saleCategories = sale.getCategories();

            for (Cake cake : order.getCakes()) {
                if (saleCategories.contains(cake.getCategory())) {
                    totalPrice += cake.getPrice() * (1 - percentage / 100);
                } else {
                    totalPrice += cake.getPrice();
                }
            }
        }

        order.setTotalPrice(totalPrice);
        return orderRepository.save(order);
    }

    public List<Order> getUserOrderHistory(User user) {
        if (user == null) {
            throw new UserNotFoundException("User cannot be null");
        }
        return orderRepository.findByUserOrderByOrderDateDesc(user);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAllByOrderByOrderDateDesc();
    }

    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order with ID " + orderId + " not found"));
    }

    public Order updateOrder(Long orderId, Long userId, Set<Long> cakeIds) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order with ID " + orderId + " not found"));

        if (userId != null && !userId.equals(order.getUser().getId())) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User with ID " + userId + " not found"));
            order.setUser(user);
        }

        if (cakeIds == null || cakeIds.isEmpty()) {
            throw new EmptyCartException("Cake IDs cannot be empty");
        }

        Set<Cake> cakes = new HashSet<>();
        for (Long cakeId : cakeIds) {
            Cake cake = cakeRepository.findById(cakeId)
                    .orElseThrow(() -> new CakeNotFoundException("Cake with ID " + cakeId + " not found"));
            cakes.add(cake);
        }
        order.setCakes(cakes);

        return orderRepository.save(order);
    }
}
