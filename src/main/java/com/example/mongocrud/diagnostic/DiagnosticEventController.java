package com.example.mongocrud.diagnostic;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/diagnostic-events")
public class DiagnosticEventController {

    private final DiagnosticEventService service;

    public DiagnosticEventController(DiagnosticEventService service) {
        this.service = service;
    }

    @GetMapping
    public List<DiagnosticEvent> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public DiagnosticEvent findById(@PathVariable String id) {
        return service.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DiagnosticEvent create(@Valid @RequestBody DiagnosticEventRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public DiagnosticEvent update(@PathVariable String id, @Valid @RequestBody DiagnosticEventRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        service.delete(id);
    }
}

