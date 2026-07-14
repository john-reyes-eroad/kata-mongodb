package com.example.mongocrud.vehicle;

import java.time.Instant;
import java.util.List;

import com.example.mongocrud.common.ResourceNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class VehicleService {

    private final VehicleRepository repository;

    public VehicleService(VehicleRepository repository) {
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

    public Vehicle create(VehicleRequest request) {
        Instant now = Instant.now();
        Vehicle vehicle = new Vehicle(
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

    public Vehicle update(String id, VehicleRequest request) {
        Vehicle existing = findById(id);
        Vehicle updated = new Vehicle(
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
        Vehicle vehicle = findById(id);
        repository.delete(vehicle);
    }
}
