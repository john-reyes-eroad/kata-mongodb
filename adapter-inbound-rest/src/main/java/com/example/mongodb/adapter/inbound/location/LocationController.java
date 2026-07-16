package com.example.mongodb.adapter.inbound.location;

import java.util.List;

import com.example.mongodb.adapter.inbound.common.CountResponse;
import com.example.mongocrud.location.Location;
import com.example.mongocrud.location.application.LocationUpsertCommand;
import com.example.mongocrud.location.port.inbound.LocationUseCase;
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
@RequestMapping("/api/locations")
public class LocationController {

    private final LocationUseCase service;

    public LocationController(LocationUseCase service) {
        this.service = service;
    }

    @GetMapping
    public List<Location> findAll() {
        return service.findAll();
    }

    @GetMapping("/count")
    public CountResponse count(@RequestParam(value = "keyword", required = false) String keyword) {
        return new CountResponse(service.count(keyword));
    }

    @GetMapping("/{id}")
    public Location findById(@PathVariable String id) {
        return service.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Location create(@Valid @RequestBody LocationRequest request) {
        return service.create(toCommand(request));
    }

    @PutMapping("/{id}")
    public Location update(@PathVariable String id, @Valid @RequestBody LocationRequest request) {
        return service.update(id, toCommand(request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        service.delete(id);
    }

    private LocationUpsertCommand toCommand(LocationRequest request) {
        return new LocationUpsertCommand(
                request.tripId(),
                request.latitude(),
                request.longitude(),
                request.recordedAt()
        );
    }
}
