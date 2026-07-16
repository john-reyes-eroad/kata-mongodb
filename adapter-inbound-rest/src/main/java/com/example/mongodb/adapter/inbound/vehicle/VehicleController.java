package com.example.mongodb.adapter.inbound.vehicle;

import java.util.List;

import com.example.mongodb.adapter.inbound.common.CountResponse;
import com.example.mongocrud.vehicle.Vehicle;
import com.example.mongocrud.vehicle.application.VehicleUpsertCommand;
import com.example.mongocrud.vehicle.port.inbound.VehicleUseCase;
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
@RequestMapping("/api/vehicles")
public class VehicleController {

    private final VehicleUseCase service;

    public VehicleController(VehicleUseCase service) {
        this.service = service;
    }

    @GetMapping
    public List<Vehicle> findAll() {
        return service.findAll();
    }

    @GetMapping("/search")
    public List<Vehicle> search(@RequestParam("keyword") String keyword) {
        return service.search(keyword);
    }

    @GetMapping("/count")
    public CountResponse count(@RequestParam(value = "keyword", required = false) String keyword) {
        return new CountResponse(service.count(keyword));
    }

    @GetMapping("/{id}")
    public Vehicle findById(@PathVariable String id) {
        return service.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Vehicle create(@Valid @RequestBody VehicleRequest request) {
        return service.create(new VehicleUpsertCommand(
                request.vin(),
                request.make(),
                request.model(),
                request.year()
        ));
    }

    @PutMapping("/{id}")
    public Vehicle update(@PathVariable String id, @Valid @RequestBody VehicleRequest request) {
        return service.update(id, new VehicleUpsertCommand(
                request.vin(),
                request.make(),
                request.model(),
                request.year()
        ));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        service.delete(id);
    }
}
