package com.example.mongocrud.diagnostic.port.inbound;

import java.util.List;

import com.example.mongocrud.diagnostic.DiagnosticEvent;
import com.example.mongocrud.diagnostic.application.DiagnosticEventUpsertCommand;

public interface DiagnosticEventUseCase {

    List<DiagnosticEvent> findAll();

    DiagnosticEvent findById(String id);

    long count(String keyword);

    DiagnosticEvent create(DiagnosticEventUpsertCommand command);

    DiagnosticEvent update(String id, DiagnosticEventUpsertCommand command);

    void delete(String id);
}
