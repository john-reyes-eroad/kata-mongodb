package com.example.mongocrud.trip;

import java.time.Instant;
import java.util.List;

import com.example.mongocrud.common.ResourceNotFoundException;
import com.example.mongocrud.driver.DriverService;
import com.example.mongocrud.vehicle.VehicleService;
import org.springframework.stereotype.Service;

@Service
public class TripService {

    private final TripRepository repository;
    private final VehicleService vehicleService;
    private final DriverService driverService;

    public TripService(TripRepository repository, VehicleService vehicleService, DriverService driverService) {
        this.repository = repository;
        this.vehicleService = vehicleService;
        this.driverService = driverService;
    }

    public List<Trip> findAll() {
        return repository.findAll();
    }

    public Trip findById(String id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Trip not found: " + id));
    }

    public Trip create(TripRequest request) {
        Instant now = Instant.now();
        Trip trip = new Trip(
                null,
                vehicleService.findById(request.vehicleId()),
                driverService.findById(request.driverId()),
                request.startTime(),
                request.endTime(),
                request.distanceKm(),
                now,
                now
        );
        return repository.save(trip);
    }

    public Trip update(String id, TripRequest request) {
        Trip existing = findById(id);
        Trip updated = new Trip(
                existing.id(),
                vehicleService.findById(request.vehicleId()),
                driverService.findById(request.driverId()),
                request.startTime(),
                request.endTime(),
                request.distanceKm(),
                existing.createdAt(),
                Instant.now()
        );
        return repository.save(updated);
    }

    public void delete(String id) {
        Trip trip = findById(id);
        repository.delete(trip);
    }
}
