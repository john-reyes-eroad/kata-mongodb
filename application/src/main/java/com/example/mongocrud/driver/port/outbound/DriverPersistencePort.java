package com.example.mongocrud.driver.port.outbound;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.example.mongocrud.driver.Driver;

public interface DriverPersistencePort {

    List<Driver> findAll();

    Optional<Driver> findById(String id);

    Map<String, Driver> findByIds(Collection<String> ids);

    List<Driver> search(String keyword);

    long count(String keyword);

    Driver save(Driver driver);

    void delete(Driver driver);
}
