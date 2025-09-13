package com.awbd.cakeshop.repositories;

import com.awbd.cakeshop.models.PastryChef;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PastryChefRepository extends JpaRepository<PastryChef, Long> {

    List<PastryChef> findByName(String name);

    List<PastryChef> findByNameContaining(String nameFragment);

    boolean existsByName(String name);

    boolean existsByNameAndBirthDate(String name, LocalDate birthDate);

    Optional<PastryChef> findByNameIgnoreCase(String name);
}