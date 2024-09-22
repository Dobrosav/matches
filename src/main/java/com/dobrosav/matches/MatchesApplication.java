package com.dobrosav.matches;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class MatchesApplication {

    public static void main(String[] args) {
        SpringApplication.run(MatchesApplication.class, args);
    }

}
