package com.example.mongocrud.vehicle.application;

import java.time.Instant;
import java.util.List;

import com.example.mongocrud.common.ResourceNotFoundException;
import com.example.mongocrud.vehicle.Vehicle;
import com.example.mongocrud.vehicle.port.inbound.VehicleUseCase;
import com.example.mongocrud.vehicle.port.outbound.VehiclePersistencePort;

public class VehicleService implements VehicleUseCase {

    private final VehiclePersistencePort repository;

    public VehicleService(VehiclePersistencePort repository) {
        this.repository = repository;
    }

    public List<Vehicle> findAll() {
        return repository.findAll();
    }

    public Vehicle findById(String id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Vehicle not found: " + id));
    }

    public List<Vehicle> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return findAll();
        }

        return repository.search(keyword);
    }

    public long count(String keyword) {
        return repository.count(normalizeKeyword(keyword));
    }

    public Vehicle create(VehicleUpsertCommand request) {
        var now = Instant.now();
        var vehicle = new Vehicle(
                null,
                request.vin(),
                request.make(),
                request.model(),
                request.year(),
                now,
                now
        );
        return repository.save(vehicle);
    }

    public Vehicle update(String id, VehicleUpsertCommand request) {
        var existing = findById(id);
        var updated = new Vehicle(
                existing.id(),
                request.vin(),
                request.make(),
                request.model(),
                request.year(),
                existing.createdAt(),
                Instant.now()
        );
        return repository.save(updated);
    }

    public void delete(String id) {
        var vehicle = findById(id);
        repository.delete(vehicle);
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null) {
            return null;
        }
        var trimmedKeyword = keyword.trim();
        return trimmedKeyword.isEmpty() ? null : trimmedKeyword;
    }
}
