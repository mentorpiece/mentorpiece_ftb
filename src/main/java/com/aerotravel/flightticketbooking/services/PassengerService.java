package com.aerotravel.flightticketbooking.services;

import com.aerotravel.flightticketbooking.exception.EntityNotFoundException;
import com.aerotravel.flightticketbooking.model.Passenger;

import java.util.List;

public interface PassengerService extends EntityService<Passenger> {
    List<Passenger> getAllByByPassportNumber(String number);

    // User-aware methods
    List<Passenger> getCurrentUserPassengers();
    List<Passenger> getCurrentUserPassengersByPassportNumber(String number);
    Passenger savePassengerForCurrentUser(Passenger passenger);

    default EntityNotFoundException buildEntityNotFoundException(long id) {
        return buildEntityNotFoundException("Passenger", id);
    }
}
