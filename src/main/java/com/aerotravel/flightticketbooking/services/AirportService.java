package com.aerotravel.flightticketbooking.services;

import com.aerotravel.flightticketbooking.exception.EntityNotFoundException;
import com.aerotravel.flightticketbooking.model.Airport;

import java.util.List;

public interface AirportService extends EntityService<Airport> {
    Airport getByCode(String airportCode);

    List<Airport> getCurrentUserAirports();
    Airport getCurrentUserAirportByCode(String airportCode);
    Airport saveAirportForCurrentUser(Airport airport);

    default EntityNotFoundException buildEntityNotFoundException(long id) {
        return buildEntityNotFoundException("Airport", id);
    }
}
