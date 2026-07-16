package com.example.mongodb.adapter.inbound.trip;

import java.util.List;

import com.example.mongodb.adapter.inbound.common.CountResponse;
import com.example.mongocrud.trip.Trip;
import com.example.mongocrud.trip.application.TripUpsertCommand;
import com.example.mongocrud.trip.port.inbound.TripUseCase;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/trips")
public class TripController {

    private final TripUseCase service;

    public TripController(TripUseCase service) {
        this.service = service;
    }

    @GetMapping
    public List<Trip> findAll() {
        return service.findAll();
    }

    @GetMapping("/count")
    public CountResponse count(@RequestParam(value = "keyword", required = false) String keyword) {
        return new CountResponse(service.count(keyword));
    }

    @GetMapping("/{id}")
    public Trip findById(@PathVariable String id) {
        return service.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Trip create(@Valid @RequestBody TripRequest request) {
        return service.create(toCommand(request));
    }

    @PutMapping("/{id}")
    public Trip update(@PathVariable String id, @Valid @RequestBody TripRequest request) {
        return service.update(id, toCommand(request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        service.delete(id);
    }

    private TripUpsertCommand toCommand(TripRequest request) {
        return new TripUpsertCommand(
                request.vehicleId(),
                request.driverId(),
                request.startTime(),
                request.endTime(),
                request.distanceKm()
        );
    }
}
