package com.prison;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PrisonApplication {
    public static void main(String[] args) {
        SpringApplication.run(PrisonApplication.class, args);
    }
}
