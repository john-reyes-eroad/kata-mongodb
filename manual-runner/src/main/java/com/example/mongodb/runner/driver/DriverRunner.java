package com.example.mongodb.runner.driver;

import java.time.Instant;
import java.util.List;

import com.example.mongocrud.driver.Driver;
import com.example.mongocrud.driver.port.outbound.DriverPersistencePort;
import com.example.mongodb.runner.DomainRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("driver")
public class DriverRunner implements DomainRunner {

    private final DriverPersistencePort repository;

    public DriverRunner(DriverPersistencePort repository) {
        this.repository = repository;
    }

    @Override
    public void run() {
        Instant now = Instant.now();

        // save
        Driver saved = repository.save(new Driver("Jane Smith", "DL-99887766", now, now));
        System.out.println("saved:   " + saved);

        // findById
        repository.findById(saved.id()).ifPresent(d -> System.out.println("findById: " + d));

        // findAll
        List<Driver> all = repository.findAll();
        System.out.println("findAll count: " + all.size());

        // count
        System.out.println("count (no keyword): " + repository.count(null));
        System.out.println("count (Smith):      " + repository.count("Smith"));

        // search
        List<Driver> results = repository.search("Smith");
        System.out.println("search (Smith): " + results.size() + " result(s)");

        // update
        Driver updated = repository.save(new Driver(saved.id(), "Jane M. Smith", saved.licenseNumber(), saved.createdAt(), Instant.now()));
        System.out.println("updated: " + updated);

        // delete
        repository.delete(updated);
        System.out.println("deleted: " + updated.id());
        System.out.println("findById after delete: " + repository.findById(updated.id()));
    }
}
