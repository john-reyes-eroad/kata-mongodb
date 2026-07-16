package com.example.mongocrud.trip.port.outbound;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.example.mongocrud.trip.Trip;

public interface TripPersistencePort {

    List<Trip> findAll();

    Optional<Trip> findById(String id);

    Map<String, Trip> findByIds(Collection<String> ids);

    long count(String keyword);

    Trip save(Trip trip);

    void delete(Trip trip);
}
