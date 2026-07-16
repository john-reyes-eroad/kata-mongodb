package com.example.mongocrud.vehicle.port.outbound;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.example.mongocrud.vehicle.Vehicle;

public interface VehiclePersistencePort {

    List<Vehicle> findAll();

    Optional<Vehicle> findById(String id);

    Map<String, Vehicle> findByIds(Collection<String> ids);

    List<Vehicle> search(String keyword);

    long count(String keyword);

    Vehicle save(Vehicle vehicle);

    void delete(Vehicle vehicle);
}
