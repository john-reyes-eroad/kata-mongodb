package com.example.mongodb.runner.diagnostic;

import java.time.Instant;

import com.example.mongocrud.diagnostic.DiagnosticEvent;
import com.example.mongocrud.diagnostic.port.outbound.DiagnosticEventPersistencePort;
import com.example.mongocrud.vehicle.Vehicle;
import com.example.mongocrud.vehicle.port.outbound.VehiclePersistencePort;
import com.example.mongodb.runner.DomainRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("diagnostic")
public class DiagnosticEventRunner implements DomainRunner {

    private final DiagnosticEventPersistencePort diagnosticRepository;
    private final VehiclePersistencePort vehicleRepository;

    public DiagnosticEventRunner(
            DiagnosticEventPersistencePort diagnosticRepository,
            VehiclePersistencePort vehicleRepository) {
        this.diagnosticRepository = diagnosticRepository;
        this.vehicleRepository = vehicleRepository;
    }

    @Override
    public void run() {
        var now = Instant.now();

        // seed a vehicle to reference
        var vehicle = vehicleRepository.save(new Vehicle("DIAG-VIN-001", "Ford", "Transit", 2019, now, now));

        // save
        var saved = diagnosticRepository.save(new DiagnosticEvent(
                vehicle,
                "P0300", "ERROR",
                "Random/Multiple Cylinder Misfire Detected",
                now, now, now));
        System.out.println("saved:   " + saved);

        // findById
        diagnosticRepository.findById(saved.id()).ifPresent(e -> System.out.println("findById: " + e));

        // findAll
        var all = diagnosticRepository.findAll();
        System.out.println("findAll count: " + all.size());

        // count
        System.out.println("count (no keyword):   " + diagnosticRepository.count(null));
        System.out.println("count (P0300):        " + diagnosticRepository.count("P0300"));
        System.out.println("count (by vehicleId): " + diagnosticRepository.count(vehicle.id()));

        // update
        var updated = diagnosticRepository.save(new DiagnosticEvent(
                saved.id(), vehicle,
                saved.code(), "WARNING",
                "Cylinder misfire resolved after tune-up",
                saved.occurredAt(), saved.createdAt(), Instant.now()));
        System.out.println("updated: " + updated);

        // delete
        diagnosticRepository.delete(updated);
        System.out.println("deleted: " + updated.id());
        System.out.println("findById after delete: " + diagnosticRepository.findById(updated.id()));

        // clean up seeded vehicle
        vehicleRepository.delete(vehicle);
    }
}
