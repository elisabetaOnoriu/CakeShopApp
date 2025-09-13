package com.awbd.cakeshop.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pastry_chefs")
@Getter
@Setter
@NoArgsConstructor
public class PastryChef {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String biography;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @OneToMany(mappedBy = "pastryChef", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Cake> cakes = new ArrayList<>();

    public PastryChef(String name) {
        this.name = name;
    }

    public PastryChef(String name, String biography, LocalDate birthDate) {
        this.name = name;
        this.biography = biography;
        this.birthDate = birthDate;
    }

    public void addCake(Cake cake) {
        cakes.add(cake);
        cake.setPastryChef(this);
    }

    public void removeCake(Cake cake) {
        cakes.remove(cake);
        cake.setPastryChef(null);
    }
}
