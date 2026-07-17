package com.example.mongocrud.trip.application;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import com.example.mongocrud.common.ResourceNotFoundException;
import com.example.mongocrud.driver.port.inbound.DriverUseCase;
import com.example.mongocrud.driver.port.outbound.DriverPersistencePort;
import com.example.mongocrud.trip.Trip;
import com.example.mongocrud.trip.port.inbound.TripUseCase;
import com.example.mongocrud.trip.port.outbound.TripPersistencePort;
import com.example.mongocrud.vehicle.port.inbound.VehicleUseCase;
import com.example.mongocrud.vehicle.port.outbound.VehiclePersistencePort;

public class TripService implements TripUseCase {

    private final TripPersistencePort repository;
    private final VehicleUseCase vehicleService;
    private final DriverUseCase driverService;
    private final VehiclePersistencePort vehicleRepository;
    private final DriverPersistencePort driverRepository;

    public TripService(
            TripPersistencePort repository,
            VehicleUseCase vehicleService,
            DriverUseCase driverService,
            VehiclePersistencePort vehicleRepository,
            DriverPersistencePort driverRepository) {
        this.repository = repository;
        this.vehicleService = vehicleService;
        this.driverService = driverService;
        this.vehicleRepository = vehicleRepository;
        this.driverRepository = driverRepository;
    }

    public List<Trip> findAll() {
        return hydrate(repository.findAll());
    }

    public Trip findById(String id) {
        return hydrate(repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found: " + id)));
    }

    public Map<String, Trip> findByIds(Collection<String> ids) {
        var trips = hydrate(repository.findByIds(ids).values().stream().toList());
        var tripsById = new LinkedHashMap<String, Trip>();
        for (var trip : trips) {
            if (trip.id() != null) {
                tripsById.put(trip.id(), trip);
            }
        }
        return tripsById;
    }

    public long count(String keyword) {
        return repository.count(normalizeKeyword(keyword));
    }

    public Trip create(TripUpsertCommand command) {
        var now = Instant.now();
        var trip = new Trip(
                null,
                vehicleService.findById(command.vehicleId()),
                driverService.findById(command.driverId()),
                command.startTime(),
                command.endTime(),
                command.distanceKm(),
                now,
                now
        );
        return repository.save(trip);
    }

    public Trip update(String id, TripUpsertCommand command) {
        var existing = findById(id);
        var updated = new Trip(
                existing.id(),
                vehicleService.findById(command.vehicleId()),
                driverService.findById(command.driverId()),
                command.startTime(),
                command.endTime(),
                command.distanceKm(),
                existing.createdAt(),
                Instant.now()
        );
        return repository.save(updated);
    }

    public void delete(String id) {
        var trip = findById(id);
        repository.delete(trip);
    }

    private List<Trip> hydrate(List<Trip> trips) {
        var vehicleIds = new LinkedHashSet<String>();
        var driverIds = new LinkedHashSet<String>();
        for (var trip : trips) {
            if (trip.vehicle() != null && trip.vehicle().id() != null) {
                vehicleIds.add(trip.vehicle().id());
            }
            if (trip.driver() != null && trip.driver().id() != null) {
                driverIds.add(trip.driver().id());
            }
        }

        var vehiclesById = vehicleRepository.findByIds(vehicleIds);
        var driversById = driverRepository.findByIds(driverIds);
        return trips.stream()
                .map(trip -> new Trip(
                        trip.id(),
                        trip.vehicle() == null ? null : vehiclesById.get(trip.vehicle().id()),
                        trip.driver() == null ? null : driversById.get(trip.driver().id()),
                        trip.startTime(),
                        trip.endTime(),
                        trip.distanceKm(),
                        trip.createdAt(),
                        trip.updatedAt()))
                .toList();
    }

    private Trip hydrate(Trip trip) {
        return hydrate(List.of(trip)).getFirst();
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null) {
            return null;
        }
        var trimmedKeyword = keyword.trim();
        return trimmedKeyword.isEmpty() ? null : trimmedKeyword;
    }
}
