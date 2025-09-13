package com.awbd.cakeshop.DTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CakeDTO {

    private Long id;
    private String name;
    private double price;
    private int stock;
    private double weight;
    private Long categoryId;
    private Long pastryChefId;
    private String pastryChefName;
    private String description;
}
