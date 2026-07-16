package com.example.mongocrud.driver.port.inbound;

import java.util.List;

import com.example.mongocrud.driver.Driver;
import com.example.mongocrud.driver.application.DriverUpsertCommand;

public interface DriverUseCase {

    List<Driver> findAll();

    List<Driver> search(String keyword);

    long count(String keyword);

    Driver findById(String id);

    Driver create(DriverUpsertCommand command);

    Driver update(String id, DriverUpsertCommand command);

    void delete(String id);
}
