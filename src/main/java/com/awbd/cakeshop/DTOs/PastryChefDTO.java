package com.awbd.cakeshop.DTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PastryChefDTO {

    private Long id;
    private String name;
    private String biography;
    private LocalDate birthDate;
    private List<Long> cakeIds;

    public PastryChefDTO(String name, String biography, LocalDate birthDate) {
        this.name = name;
        this.biography = biography;
        this.birthDate = birthDate;
    }

    public PastryChefDTO(Long id, String name, String biography, LocalDate birthDate) {
        this.id = id;
        this.name = name;
        this.biography = biography;
        this.birthDate = birthDate;
    }
}
