package com.awbd.cakeshop.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Cake {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String name;
    private double price;
    private int stock;
    private double weight;
    private String description;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToMany(mappedBy = "cakes")
    private Set<Cart> carts = new HashSet<>();

    @ManyToMany
    private Set<Order> orders = new HashSet<>();

    @OneToMany(mappedBy = "cake", cascade = CascadeType.ALL)
    private List<Review> reviews = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "chef_id")
    private PastryChef pastryChef;

    public Cake(String name, double price, int stock, double weight, String description, Category category, PastryChef pastryChef) {
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.weight = weight;
        this.description = description;
        this.category = category;
        this.pastryChef = pastryChef;
    }
}
