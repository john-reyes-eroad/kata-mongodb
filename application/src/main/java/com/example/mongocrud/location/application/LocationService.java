package com.example.mongocrud.location.application;

import java.util.LinkedHashSet;
import java.time.Instant;
import java.util.List;

import com.example.mongocrud.common.ResourceNotFoundException;
import com.example.mongocrud.location.Location;
import com.example.mongocrud.location.port.inbound.LocationUseCase;
import com.example.mongocrud.location.port.outbound.LocationPersistencePort;
import com.example.mongocrud.trip.port.inbound.TripUseCase;

public class LocationService implements LocationUseCase {

    private final LocationPersistencePort repository;
    private final TripUseCase tripService;

    public LocationService(LocationPersistencePort repository, TripUseCase tripService) {
        this.repository = repository;
        this.tripService = tripService;
    }

    public List<Location> findAll() {
        return hydrate(repository.findAll());
    }

    public Location findById(String id) {
        return hydrate(repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found: " + id)));
    }

    public long count(String keyword) {
        return repository.count(normalizeKeyword(keyword));
    }

    public Location create(LocationUpsertCommand command) {
        var now = Instant.now();
        var location = new Location(
                null,
                tripService.findById(command.tripId()),
                command.latitude(),
                command.longitude(),
                command.recordedAt(),
                now,
                now
        );
        return repository.save(location);
    }

    public Location update(String id, LocationUpsertCommand command) {
        var existing = findById(id);
        var updated = new Location(
                existing.id(),
                tripService.findById(command.tripId()),
                command.latitude(),
                command.longitude(),
                command.recordedAt(),
                existing.createdAt(),
                Instant.now()
        );
        return repository.save(updated);
    }

    public void delete(String id) {
        var location = findById(id);
        repository.delete(location);
    }

    private List<Location> hydrate(List<Location> locations) {
        var tripIds = new LinkedHashSet<String>();
        for (var location : locations) {
            if (location.trip() != null && location.trip().id() != null) {
                tripIds.add(location.trip().id());
            }
        }

        var tripsById = tripService.findByIds(tripIds);
        return locations.stream()
                .map(location -> new Location(
                        location.id(),
                        location.trip() == null ? null : tripsById.get(location.trip().id()),
                        location.latitude(),
                        location.longitude(),
                        location.recordedAt(),
                        location.createdAt(),
                        location.updatedAt()))
                .toList();
    }

    private Location hydrate(Location location) {
        return hydrate(List.of(location)).getFirst();
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null) {
            return null;
        }
        var trimmedKeyword = keyword.trim();
        return trimmedKeyword.isEmpty() ? null : trimmedKeyword;
    }
}
