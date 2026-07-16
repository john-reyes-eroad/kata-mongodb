package com.example.mongocrud.diagnostic.port.outbound;

import java.util.List;
import java.util.Optional;

import com.example.mongocrud.diagnostic.DiagnosticEvent;

public interface DiagnosticEventPersistencePort {

    List<DiagnosticEvent> findAll();

    Optional<DiagnosticEvent> findById(String id);

    long count(String keyword);

    DiagnosticEvent save(DiagnosticEvent event);

    void delete(DiagnosticEvent event);
}
