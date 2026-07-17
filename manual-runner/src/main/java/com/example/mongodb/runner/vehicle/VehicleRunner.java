package com.example.mongodb.runner.vehicle;

import java.time.Instant;
import java.util.List;

import com.example.mongocrud.vehicle.Vehicle;
import com.example.mongocrud.vehicle.port.outbound.VehiclePersistencePort;
import com.example.mongodb.runner.DomainRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("vehicle")
public class VehicleRunner implements DomainRunner {

    private final VehiclePersistencePort repository;

    public VehicleRunner(VehiclePersistencePort repository) {
        this.repository = repository;
    }

    @Override
    public void run() {
        Instant now = Instant.now();

        // save
        Vehicle saved = repository.save(new Vehicle(null, "1HGBH41JXMN109186", "Honda", "Civic", 2021, now, now));
        System.out.println("saved:   " + saved);

        // findById
        repository.findById(saved.id()).ifPresent(v -> System.out.println("findById: " + v));

        // findAll
        List<Vehicle> all = repository.findAll();
        System.out.println("findAll count: " + all.size());

        // count
        System.out.println("count (no keyword): " + repository.count(null));
        System.out.println("count (Honda):      " + repository.count("Honda"));

        // search
        List<Vehicle> results = repository.search("Honda");
        System.out.println("search (Honda): " + results.size() + " result(s)");

        // update
        Vehicle updated = repository.save(new Vehicle(saved.id(), saved.vin(), "Honda", "Civic EX", 2022, saved.createdAt(), Instant.now()));
        System.out.println("updated: " + updated);

        // delete
        repository.delete(updated);
        System.out.println("deleted: " + updated.id());
        System.out.println("findById after delete: " + repository.findById(updated.id()));
    }
}
