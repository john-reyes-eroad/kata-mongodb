package com.example.mongodb.adapter.inbound.diagnostic;

import java.util.List;

import com.example.mongodb.adapter.inbound.common.CountResponse;
import com.example.mongocrud.diagnostic.DiagnosticEvent;
import com.example.mongocrud.diagnostic.application.DiagnosticEventUpsertCommand;
import com.example.mongocrud.diagnostic.port.inbound.DiagnosticEventUseCase;
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
@RequestMapping("/api/diagnostic-events")
public class DiagnosticEventController {

    private final DiagnosticEventUseCase service;

    public DiagnosticEventController(DiagnosticEventUseCase service) {
        this.service = service;
    }

    @GetMapping
    public List<DiagnosticEvent> findAll() {
        return service.findAll();
    }

    @GetMapping("/count")
    public CountResponse count(@RequestParam(value = "keyword", required = false) String keyword) {
        return new CountResponse(service.count(keyword));
    }

    @GetMapping("/{id}")
    public DiagnosticEvent findById(@PathVariable String id) {
        return service.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DiagnosticEvent create(@Valid @RequestBody DiagnosticEventRequest request) {
        return service.create(toCommand(request));
    }

    @PutMapping("/{id}")
    public DiagnosticEvent update(@PathVariable String id, @Valid @RequestBody DiagnosticEventRequest request) {
        return service.update(id, toCommand(request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        service.delete(id);
    }

    private DiagnosticEventUpsertCommand toCommand(DiagnosticEventRequest request) {
        return new DiagnosticEventUpsertCommand(
                request.vehicleId(),
                request.code(),
                request.severity(),
                request.description(),
                request.occurredAt()
        );
    }
}
