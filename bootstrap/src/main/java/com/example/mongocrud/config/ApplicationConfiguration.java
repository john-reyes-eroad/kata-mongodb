package com.example.mongocrud.config;

import com.example.mongocrud.diagnostic.application.DiagnosticEventService;
import com.example.mongocrud.diagnostic.port.inbound.DiagnosticEventUseCase;
import com.example.mongocrud.diagnostic.port.outbound.DiagnosticEventPersistencePort;
import com.example.mongocrud.driver.application.DriverService;
import com.example.mongocrud.driver.port.inbound.DriverUseCase;
import com.example.mongocrud.driver.port.outbound.DriverPersistencePort;
import com.example.mongocrud.location.application.LocationService;
import com.example.mongocrud.location.port.inbound.LocationUseCase;
import com.example.mongocrud.location.port.outbound.LocationPersistencePort;
import com.example.mongocrud.trip.application.TripService;
import com.example.mongocrud.trip.port.inbound.TripUseCase;
import com.example.mongocrud.trip.port.outbound.TripPersistencePort;
import com.example.mongocrud.vehicle.application.VehicleService;
import com.example.mongocrud.vehicle.port.inbound.VehicleUseCase;
import com.example.mongocrud.vehicle.port.outbound.VehiclePersistencePort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfiguration {

    @Bean
    VehicleUseCase vehicleUseCase(VehiclePersistencePort vehiclePersistencePort) {
        return new VehicleService(vehiclePersistencePort);
    }

    @Bean
    DriverUseCase driverUseCase(DriverPersistencePort driverPersistencePort) {
        return new DriverService(driverPersistencePort);
    }

    @Bean
    TripUseCase tripUseCase(
            TripPersistencePort tripPersistencePort,
            VehicleUseCase vehicleUseCase,
            DriverUseCase driverUseCase,
            VehiclePersistencePort vehiclePersistencePort,
            DriverPersistencePort driverPersistencePort) {
        return new TripService(
                tripPersistencePort,
                vehicleUseCase,
                driverUseCase,
                vehiclePersistencePort,
                driverPersistencePort);
    }

    @Bean
    LocationUseCase locationUseCase(LocationPersistencePort locationPersistencePort, TripUseCase tripUseCase) {
        return new LocationService(locationPersistencePort, tripUseCase);
    }

    @Bean
    DiagnosticEventUseCase diagnosticEventUseCase(
            DiagnosticEventPersistencePort diagnosticEventPersistencePort,
            VehicleUseCase vehicleUseCase,
            VehiclePersistencePort vehiclePersistencePort) {
        return new DiagnosticEventService(
                diagnosticEventPersistencePort,
                vehicleUseCase,
                vehiclePersistencePort);
    }
}
