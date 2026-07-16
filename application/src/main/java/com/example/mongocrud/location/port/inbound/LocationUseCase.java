package com.example.mongocrud.location.port.inbound;

import java.util.List;

import com.example.mongocrud.location.Location;
import com.example.mongocrud.location.application.LocationUpsertCommand;

public interface LocationUseCase {

    List<Location> findAll();

    Location findById(String id);

    long count(String keyword);

    Location create(LocationUpsertCommand command);

    Location update(String id, LocationUpsertCommand command);

    void delete(String id);
}
