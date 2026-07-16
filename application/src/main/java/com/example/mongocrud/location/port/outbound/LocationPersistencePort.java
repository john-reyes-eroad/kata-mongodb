package com.example.mongocrud.location.port.outbound;

import java.util.List;
import java.util.Optional;

import com.example.mongocrud.location.Location;

public interface LocationPersistencePort {

    List<Location> findAll();

    Optional<Location> findById(String id);

    long count(String keyword);

    Location save(Location location);

    void delete(Location location);
}
