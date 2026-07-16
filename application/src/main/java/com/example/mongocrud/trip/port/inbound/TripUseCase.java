package com.example.mongocrud.trip.port.inbound;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.example.mongocrud.trip.Trip;
import com.example.mongocrud.trip.application.TripUpsertCommand;

public interface TripUseCase {

    List<Trip> findAll();

    Trip findById(String id);

    Map<String, Trip> findByIds(Collection<String> ids);

    long count(String keyword);

    Trip create(TripUpsertCommand command);

    Trip update(String id, TripUpsertCommand command);

    void delete(String id);
}
