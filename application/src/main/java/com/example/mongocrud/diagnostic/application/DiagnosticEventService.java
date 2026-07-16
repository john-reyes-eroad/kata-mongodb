package com.example.mongocrud.diagnostic.application;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import com.example.mongocrud.common.ResourceNotFoundException;
import com.example.mongocrud.diagnostic.DiagnosticEvent;
import com.example.mongocrud.diagnostic.port.inbound.DiagnosticEventUseCase;
import com.example.mongocrud.diagnostic.port.outbound.DiagnosticEventPersistencePort;
import com.example.mongocrud.vehicle.Vehicle;
import com.example.mongocrud.vehicle.port.inbound.VehicleUseCase;
import com.example.mongocrud.vehicle.port.outbound.VehiclePersistencePort;

public class DiagnosticEventService implements DiagnosticEventUseCase {

    private final DiagnosticEventPersistencePort repository;
    private final VehicleUseCase vehicleService;
    private final VehiclePersistencePort vehicleRepository;

    public DiagnosticEventService(
            DiagnosticEventPersistencePort repository,
            VehicleUseCase vehicleService,
            VehiclePersistencePort vehicleRepository) {
        this.repository = repository;
        this.vehicleService = vehicleService;
        this.vehicleRepository = vehicleRepository;
    }

    public List<DiagnosticEvent> findAll() {
        return hydrate(repository.findAll());
    }

    public DiagnosticEvent findById(String id) {
        return hydrate(repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Diagnostic event not found: " + id)));
    }

    public long count(String keyword) {
        return repository.count(normalizeKeyword(keyword));
    }

    public DiagnosticEvent create(DiagnosticEventUpsertCommand command) {
        Instant now = Instant.now();
        DiagnosticEvent event = new DiagnosticEvent(
                null,
                vehicleService.findById(command.vehicleId()),
                command.code(),
                command.severity(),
                command.description(),
                command.occurredAt(),
                now,
                now
        );
        return repository.save(event);
    }

    public DiagnosticEvent update(String id, DiagnosticEventUpsertCommand command) {
        DiagnosticEvent existing = findById(id);
        DiagnosticEvent updated = new DiagnosticEvent(
                existing.id(),
                vehicleService.findById(command.vehicleId()),
                command.code(),
                command.severity(),
                command.description(),
                command.occurredAt(),
                existing.createdAt(),
                Instant.now()
        );
        return repository.save(updated);
    }

    public void delete(String id) {
        DiagnosticEvent event = findById(id);
        repository.delete(event);
    }

    private List<DiagnosticEvent> hydrate(List<DiagnosticEvent> events) {
        LinkedHashSet<String> vehicleIds = new LinkedHashSet<>();
        for (DiagnosticEvent event : events) {
            if (event.vehicle() != null && event.vehicle().id() != null) {
                vehicleIds.add(event.vehicle().id());
            }
        }

        Map<String, Vehicle> vehiclesById = vehicleRepository.findByIds(vehicleIds);
        return events.stream()
                .map(event -> new DiagnosticEvent(
                        event.id(),
                        event.vehicle() == null ? null : vehiclesById.get(event.vehicle().id()),
                        event.code(),
                        event.severity(),
                        event.description(),
                        event.occurredAt(),
                        event.createdAt(),
                        event.updatedAt()))
                .toList();
    }

    private DiagnosticEvent hydrate(DiagnosticEvent event) {
        return hydrate(List.of(event)).getFirst();
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null) {
            return null;
        }
        String trimmedKeyword = keyword.trim();
        return trimmedKeyword.isEmpty() ? null : trimmedKeyword;
    }
}
