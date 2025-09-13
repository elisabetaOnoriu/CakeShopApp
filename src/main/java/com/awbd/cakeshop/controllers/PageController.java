package com.awbd.cakeshop.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class PageController {

    @GetMapping({"/", "/home"})
    public String home() {
        return "home";
    }

    @GetMapping("/cakes/{id}")
    public String cake(@PathVariable Long id, Model model) {
        model.addAttribute("cakeId", id);
        return "cake";
    }

    // (opțional) categorie: /categories/{id} → tot home.html, filtrat din JS
    @GetMapping("/categories/{id}")
    public String category(@PathVariable Long id, Model model) {
        model.addAttribute("categoryId", id);
        return "home";
    }

    // (opțional) profil chef
    @GetMapping("/chefs/{id}")
    public String chef(@PathVariable Long id, Model model) {
        model.addAttribute("chefId", id);
        return "chef"; // creezi chef.html când ai timp
    }

    // (opțional) pagina cart
    @GetMapping("/cart")
    public String cart() {
        return "cart"; // creezi cart.html dacă nu există
    }
}
