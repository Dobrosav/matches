package com.dobrosav.matches.service;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;

public abstract class AbstractIntegrationTest {

    static final MySQLContainer<?> mysql;

    static {
        mysql = new MySQLContainer<>("mysql:8.0.33")
                .withDatabaseName("testdb")
                .withUsername("test")
                .withPassword("test");
        mysql.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        System.out.println("DEBUG: DOCKER_API_VERSION=" + System.getenv("DOCKER_API_VERSION"));
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
    }
}
