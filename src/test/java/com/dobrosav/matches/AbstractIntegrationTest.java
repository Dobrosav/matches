package com.dobrosav.matches;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;

@SpringBootTest
public abstract class AbstractIntegrationTest {

    static final MySQLContainer<?> mysql;
    static final GenericContainer<?> redis;

    static {
        mysql = new MySQLContainer<>("mysql:8.0.33")
                .withDatabaseName("testdb")
                .withUsername("test")
                .withPassword("test");
        mysql.start();

        redis = new GenericContainer<>("redis:7.2")
                .withExposedPorts(6379);
        redis.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");

        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> String.valueOf(redis.getMappedPort(6379)));
    }
}
