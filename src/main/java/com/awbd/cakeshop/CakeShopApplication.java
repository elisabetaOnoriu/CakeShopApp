package com.awbd.cakeshop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.awbd.cakeshop")
@EnableJpaRepositories(basePackages = "com.awbd.cakeshop.repositories")
@EntityScan(basePackages = "com.awbd.cakeshop.models")
public class CakeShopApplication {

    public static void main(String[] args) {
        SpringApplication.run(CakeShopApplication.class, args);
    }
}
