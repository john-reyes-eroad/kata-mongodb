package com.example.mongocrud.vehicle.port.inbound;

import java.util.List;

import com.example.mongocrud.vehicle.Vehicle;
import com.example.mongocrud.vehicle.application.VehicleUpsertCommand;

public interface VehicleUseCase {

    List<Vehicle> findAll();

    Vehicle findById(String id);

    List<Vehicle> search(String keyword);

    long count(String keyword);

    Vehicle create(VehicleUpsertCommand request);

    Vehicle update(String id, VehicleUpsertCommand request);

    void delete(String id);
}
