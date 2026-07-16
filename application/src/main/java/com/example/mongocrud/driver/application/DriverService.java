package com.example.mongocrud.driver.application;

import java.time.Instant;
import java.util.List;

import com.example.mongocrud.common.ResourceNotFoundException;
import com.example.mongocrud.driver.Driver;
import com.example.mongocrud.driver.port.inbound.DriverUseCase;
import com.example.mongocrud.driver.port.outbound.DriverPersistencePort;

public class DriverService implements DriverUseCase {

    private final DriverPersistencePort repository;

    public DriverService(DriverPersistencePort repository) {
        this.repository = repository;
    }

    public List<Driver> findAll() {
        return repository.findAll();
    }

    public List<Driver> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return findAll();
        }

        return repository.search(keyword);
    }

    public long count(String keyword) {
        return repository.count(normalizeKeyword(keyword));
    }

    public Driver findById(String id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Driver not found: " + id));
    }

    public Driver create(DriverUpsertCommand command) {
        Instant now = Instant.now();
        Driver driver = new Driver(
                null,
                command.name(),
                command.licenseNumber(),
                now,
                now
        );
        return repository.save(driver);
    }

    public Driver update(String id, DriverUpsertCommand command) {
        Driver existing = findById(id);
        Driver updated = new Driver(
                existing.id(),
                command.name(),
                command.licenseNumber(),
                existing.createdAt(),
                Instant.now()
        );
        return repository.save(updated);
    }

    public void delete(String id) {
        Driver driver = findById(id);
        repository.delete(driver);
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null) {
            return null;
        }
        String trimmedKeyword = keyword.trim();
        return trimmedKeyword.isEmpty() ? null : trimmedKeyword;
    }
}
