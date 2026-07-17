package com.example.mongodb.runner.trip;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import com.example.mongocrud.driver.Driver;
import com.example.mongocrud.driver.port.outbound.DriverPersistencePort;
import com.example.mongocrud.trip.Trip;
import com.example.mongocrud.trip.port.outbound.TripPersistencePort;
import com.example.mongocrud.vehicle.Vehicle;
import com.example.mongocrud.vehicle.port.outbound.VehiclePersistencePort;
import com.example.mongodb.runner.DomainRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("trip")
public class TripRunner implements DomainRunner {

    private final TripPersistencePort tripRepository;
    private final VehiclePersistencePort vehicleRepository;
    private final DriverPersistencePort driverRepository;

    public TripRunner(
            TripPersistencePort tripRepository,
            VehiclePersistencePort vehicleRepository,
            DriverPersistencePort driverRepository) {
        this.tripRepository = tripRepository;
        this.vehicleRepository = vehicleRepository;
        this.driverRepository = driverRepository;
    }

    @Override
    public void run() {
        Instant now = Instant.now();

        // seed a vehicle and driver to reference
        Vehicle vehicle = vehicleRepository.save(new Vehicle("TRIP-VIN-001", "Toyota", "Camry", 2020, now, now));
        Driver driver = driverRepository.save(new Driver("Trip Runner Driver", "DL-TRIP-001", now, now));

        // save
        Trip saved = tripRepository.save(new Trip(
                vehicle, driver,
                now, now.plusSeconds(3600),
                new BigDecimal("42.5"),
                now, now));
        System.out.println("saved:   " + saved);

        // findById
        tripRepository.findById(saved.id()).ifPresent(t -> System.out.println("findById: " + t));

        // findAll
        List<Trip> all = tripRepository.findAll();
        System.out.println("findAll count: " + all.size());

        // count
        System.out.println("count (no keyword):  " + tripRepository.count(null));
        System.out.println("count (by vehicleId): " + tripRepository.count(vehicle.id()));

        // findByIds
        var byIds = tripRepository.findByIds(List.of(saved.id()));
        System.out.println("findByIds: " + byIds);

        // update
        Trip updated = tripRepository.save(new Trip(
                saved.id(), vehicle, driver,
                saved.startTime(), now.plusSeconds(7200),
                new BigDecimal("88.0"),
                saved.createdAt(), Instant.now()));
        System.out.println("updated: " + updated);

        // delete
        tripRepository.delete(updated);
        System.out.println("deleted: " + updated.id());
        System.out.println("findById after delete: " + tripRepository.findById(updated.id()));

        // clean up seeded vehicle and driver
        vehicleRepository.delete(vehicle);
        driverRepository.delete(driver);
    }
}
