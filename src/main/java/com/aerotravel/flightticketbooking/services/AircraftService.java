package com.aerotravel.flightticketbooking.services;

import com.aerotravel.flightticketbooking.exception.EntityNotFoundException;
import com.aerotravel.flightticketbooking.model.Aircraft;

import java.util.List;

public interface AircraftService extends EntityService<Aircraft> {
    List<Aircraft> getByModel(String modelName);
    List<Aircraft> getByManufacturer(String manufacturerName);

    Aircraft saveAsync(Aircraft data) throws InterruptedException;
    List<Aircraft> saveAll(List<Aircraft> entities);

    // User-aware methods
    List<Aircraft> getCurrentUserAircraft();
    List<Aircraft> getCurrentUserAircraftByModel(String modelName);
    List<Aircraft> getCurrentUserAircraftByManufacturer(String manufacturerName);
    Aircraft saveAircraftForCurrentUser(Aircraft aircraft);

    default EntityNotFoundException buildEntityNotFoundException(long id) {
        return buildEntityNotFoundException("Aircraft", id);
    }
}
