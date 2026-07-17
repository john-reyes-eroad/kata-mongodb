package com.example.mongodb.runner;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
        "com.example.mongodb.runner",
        "com.example.mongodb.adapter.outbound"
})
public class ManualRunnerApplication implements CommandLineRunner {

    private final List<DomainRunner> runners;

    public ManualRunnerApplication(List<DomainRunner> runners) {
        this.runners = runners;
    }

    public static void main(String[] args) {
        SpringApplication.run(ManualRunnerApplication.class, args);
    }

    @Override
    public void run(String... args) {
        for (DomainRunner runner : runners) {
            System.out.println("\n=== " + runner.getClass().getSimpleName() + " ===");
            runner.run();
        }
    }
}
