package com.example.mongocrud.diagnostic;

import java.time.Instant;
import java.util.List;

import com.example.mongocrud.common.ResourceNotFoundException;
import com.example.mongocrud.vehicle.VehicleService;
import org.springframework.stereotype.Service;

@Service
public class DiagnosticEventService {

    private final DiagnosticEventRepository repository;
    private final VehicleService vehicleService;

    public DiagnosticEventService(DiagnosticEventRepository repository, VehicleService vehicleService) {
        this.repository = repository;
        this.vehicleService = vehicleService;
    }

    public List<DiagnosticEvent> findAll() {
        return repository.findAll();
    }

    public DiagnosticEvent findById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Diagnostic event not found: " + id));
    }

    public DiagnosticEvent create(DiagnosticEventRequest request) {
        Instant now = Instant.now();
        DiagnosticEvent event = new DiagnosticEvent(
                null,
                vehicleService.findById(request.vehicleId()),
                request.code(),
                request.severity(),
                request.description(),
                request.occurredAt(),
                now,
                now
        );
        return repository.save(event);
    }

    public DiagnosticEvent update(String id, DiagnosticEventRequest request) {
        DiagnosticEvent existing = findById(id);
        DiagnosticEvent updated = new DiagnosticEvent(
                existing.id(),
                vehicleService.findById(request.vehicleId()),
                request.code(),
                request.severity(),
                request.description(),
                request.occurredAt(),
                existing.createdAt(),
                Instant.now()
        );
        return repository.save(updated);
    }

    public void delete(String id) {
        DiagnosticEvent event = findById(id);
        repository.delete(event);
    }
}
