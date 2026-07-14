package com.example.mongocrud.location;

import java.time.Instant;
import java.util.List;

import com.example.mongocrud.common.ResourceNotFoundException;
import com.example.mongocrud.trip.TripService;
import org.springframework.stereotype.Service;

@Service
public class LocationService {

    private final LocationRepository repository;
    private final TripService tripService;

    public LocationService(LocationRepository repository, TripService tripService) {
        this.repository = repository;
        this.tripService = tripService;
    }

    public List<Location> findAll() {
        return repository.findAll();
    }

    public Location findById(String id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Location not found: " + id));
    }

    public Location create(LocationRequest request) {
        Instant now = Instant.now();
        Location location = new Location(
                null,
                tripService.findById(request.tripId()),
                request.latitude(),
                request.longitude(),
                request.recordedAt(),
                now,
                now
        );
        return repository.save(location);
    }

    public Location update(String id, LocationRequest request) {
        Location existing = findById(id);
        Location updated = new Location(
                existing.id(),
                tripService.findById(request.tripId()),
                request.latitude(),
                request.longitude(),
                request.recordedAt(),
                existing.createdAt(),
                Instant.now()
        );
        return repository.save(updated);
    }

    public void delete(String id) {
        Location location = findById(id);
        repository.delete(location);
    }
}
