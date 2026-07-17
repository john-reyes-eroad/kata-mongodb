package com.example.mongodb.runner.location;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import com.example.mongocrud.location.Location;
import com.example.mongocrud.location.port.outbound.LocationPersistencePort;
import com.example.mongocrud.trip.Trip;
import com.example.mongocrud.trip.port.outbound.TripPersistencePort;
import com.example.mongodb.runner.DomainRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("location")
public class LocationRunner implements DomainRunner {

    private final LocationPersistencePort locationRepository;
    private final TripPersistencePort tripRepository;

    public LocationRunner(LocationPersistencePort locationRepository, TripPersistencePort tripRepository) {
        this.locationRepository = locationRepository;
        this.tripRepository = tripRepository;
    }

    @Override
    public void run() {
        Instant now = Instant.now();

        // seed a trip to reference
        Trip trip = tripRepository.save(new Trip(null, null, now, now.plusSeconds(1800), new BigDecimal("10.0"), now, now));

        // save
        Location saved = locationRepository.save(new Location(
                trip,
                new BigDecimal("-36.8485"),
                new BigDecimal("174.7633"),
                now, now, now));
        System.out.println("saved:   " + saved);

        // findById
        locationRepository.findById(saved.id()).ifPresent(l -> System.out.println("findById: " + l));

        // findAll
        List<Location> all = locationRepository.findAll();
        System.out.println("findAll count: " + all.size());

        // count
        System.out.println("count (no keyword):  " + locationRepository.count(null));
        System.out.println("count (by tripId):   " + locationRepository.count(trip.id()));

        // update
        Location updated = locationRepository.save(new Location(
                saved.id(), trip,
                new BigDecimal("-36.8600"),
                new BigDecimal("174.7700"),
                now.plusSeconds(60), saved.createdAt(), Instant.now()));
        System.out.println("updated: " + updated);

        // delete
        locationRepository.delete(updated);
        System.out.println("deleted: " + updated.id());
        System.out.println("findById after delete: " + locationRepository.findById(updated.id()));

        // clean up seeded trip
        tripRepository.delete(trip);
    }
}
