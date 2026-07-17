package com.example.mongodb.runner;

/**
 * Implement this interface to create a runnable scenario for a domain repository.
 * Each implementation is auto-discovered and executed by ManualRunnerApplication.
 * Comment out @Component on any runner you don't want to execute.
 */
public interface DomainRunner {

    void run();
}
