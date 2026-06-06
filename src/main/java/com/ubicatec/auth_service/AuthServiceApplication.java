package com.ubicatec.auth_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(
        basePackages = "com.ubicatec.auth_service.infrastructure.adapter.out.persistence.repository"
)
@EntityScan(
        basePackages = "com.ubicatec.auth_service.infrastructure.adapter.out.persistence.entity"
)
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }

}