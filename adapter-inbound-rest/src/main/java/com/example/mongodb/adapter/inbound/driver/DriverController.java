package com.example.mongodb.adapter.inbound.driver;

import java.util.List;

import com.example.mongodb.adapter.inbound.common.CountResponse;
import com.example.mongocrud.driver.Driver;
import com.example.mongocrud.driver.application.DriverUpsertCommand;
import com.example.mongocrud.driver.port.inbound.DriverUseCase;
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
@RequestMapping("/api/drivers")
public class DriverController {

    private final DriverUseCase service;

    public DriverController(DriverUseCase service) {
        this.service = service;
    }

    @GetMapping
    public List<Driver> findAll() {
        return service.findAll();
    }

    @GetMapping("/search")
    public List<Driver> search(@RequestParam("keyword") String keyword) {
        return service.search(keyword);
    }

    @GetMapping("/count")
    public CountResponse count(@RequestParam(value = "keyword", required = false) String keyword) {
        return new CountResponse(service.count(keyword));
    }

    @GetMapping("/{id}")
    public Driver findById(@PathVariable String id) {
        return service.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Driver create(@Valid @RequestBody DriverRequest request) {
        return service.create(new DriverUpsertCommand(request.name(), request.licenseNumber()));
    }

    @PutMapping("/{id}")
    public Driver update(@PathVariable String id, @Valid @RequestBody DriverRequest request) {
        return service.update(id, new DriverUpsertCommand(request.name(), request.licenseNumber()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        service.delete(id);
    }
}
