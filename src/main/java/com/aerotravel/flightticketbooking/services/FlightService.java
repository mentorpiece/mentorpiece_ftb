package com.aerotravel.flightticketbooking.services;

import com.aerotravel.flightticketbooking.exception.EntityNotFoundException;
import com.aerotravel.flightticketbooking.model.Airport;
import com.aerotravel.flightticketbooking.model.Flight;

import java.time.LocalDate;
import java.util.List;

public interface FlightService extends EntityService<Flight> {
    List<Flight> getAllByAirportAndDepartureTime(Airport depAirport, Airport destAirport, LocalDate depDate);
    List<Flight> getAllByAirports(Airport depAirport, Airport destAirport);
    List<Flight> getAllByByFlightNumber(String flightNumber);

    // User-aware methods
    List<Flight> getCurrentUserFlights();
    List<Flight> getCurrentUserFlightsByAirportAndDepartureTime(Airport depAirport, Airport destAirport, LocalDate depDate);
    List<Flight> getCurrentUserFlightsByAirports(Airport depAirport, Airport destAirport);
    List<Flight> getCurrentUserFlightsByFlightNumber(String flightNumber);
    Flight saveFlightForCurrentUser(Flight flight);

    default EntityNotFoundException buildEntityNotFoundException(long id) {
        return buildEntityNotFoundException("Flight", id);
    }
}
