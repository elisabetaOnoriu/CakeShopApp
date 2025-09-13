package com.awbd.cakeshop.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "orders")  // "order" e cuvânt rezervat în SQL
@Getter
@Setter
@NoArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToMany
    @JoinTable(
            name = "order_cake",
            joinColumns = @JoinColumn(name = "order_id"),
            inverseJoinColumns = @JoinColumn(name = "cake_id")
    )
    private Set<Cake> cakes = new HashSet<>();

    private LocalDateTime orderDate = LocalDateTime.now();

    private Double totalPrice;

    // doar o reducere per comandă
    @OneToOne
    @JoinColumn(name = "sale_id", unique = true)
    private Sale sale;

    public Order(User user) {
        this.user = user;
        this.orderDate = LocalDateTime.now();
    }

    public void addCake(Cake cake) {
        this.cakes.add(cake);
    }

    public void removeCake(Cake cake) {
        this.cakes.remove(cake);
    }

    public void setUserId(long userId) {
        if (this.user == null) {
            this.user = new User();
        }
        this.user.setId(userId);
    }
}
