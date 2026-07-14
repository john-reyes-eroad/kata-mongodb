package com.example.mongocrud.driver;

import java.time.Instant;
import java.util.List;

import com.example.mongocrud.common.ResourceNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class DriverService {

    private final DriverRepository repository;

    public DriverService(DriverRepository repository) {
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

    public Driver findById(String id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Driver not found: " + id));
    }

    public Driver create(DriverRequest request) {
        Instant now = Instant.now();
        Driver driver = new Driver(
                null,
                request.name(),
                request.licenseNumber(),
                now,
                now
        );
        return repository.save(driver);
    }

    public Driver update(String id, DriverRequest request) {
        Driver existing = findById(id);
        Driver updated = new Driver(
                existing.id(),
                request.name(),
                request.licenseNumber(),
                existing.createdAt(),
                Instant.now()
        );
        return repository.save(updated);
    }

    public void delete(String id) {
        Driver driver = findById(id);
        repository.delete(driver);
    }
}
