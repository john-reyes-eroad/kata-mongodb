package com.example.mongocrud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportRuntimeHints;

@ImportRuntimeHints({HibernateValidatorRuntimeHints.class, CaffeineRuntimeHints.class})
@SpringBootApplication(scanBasePackages = {
        "com.example.mongocrud",
        "com.example.mongodb.adapter.inbound",
        "com.example.mongodb.adapter.outbound"
})
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
