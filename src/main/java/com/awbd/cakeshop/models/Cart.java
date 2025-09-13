package com.awbd.cakeshop.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @ManyToMany
    @JoinTable(
            name = "cart_cake",
            joinColumns = @JoinColumn(name = "cart_id"),
            inverseJoinColumns = @JoinColumn(name = "cake_id")
    )
    private Set<Cake> cakes = new HashSet<>();

    public Cart(User user) {
        this.user = user;
    }

    public void addCake(Cake cake) {
        cakes.add(cake);
    }

    public void removeCake(Cake cake) {
        cakes.remove(cake);
    }

    public void setUserId(long userId) {
        if (this.user == null) {
            this.user = new User();
        }
        this.user.setId(userId);
    }
}