package com.aerotravel.flightticketbooking.repository;

import com.aerotravel.flightticketbooking.model.Airport;
import com.aerotravel.flightticketbooking.model.Flight;
import com.aerotravel.flightticketbooking.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface FlightRepository extends JpaRepository<Flight, Long> {
    List<Flight> findAllByDepartureAirportEqualsAndDestinationAirportEqualsAndDepartureDateEquals(Airport depAirport, Airport destAirport, LocalDate depDate);
    List<Flight> findAllByDepartureAirportEqualsAndDestinationAirportEquals(Airport depAirport, Airport destAirport);

    List<Flight> findByFlightNumber(String flightNumber);

    // User-filtered methods
    List<Flight> findAllByOwner(User owner);
    Page<Flight> findAllByOwner(User owner, Pageable pageable);
    List<Flight> findAllByOwnerAndDepartureAirportEqualsAndDestinationAirportEqualsAndDepartureDateEquals(User owner, Airport depAirport, Airport destAirport, LocalDate depDate);
    List<Flight> findAllByOwnerAndDepartureAirportEqualsAndDestinationAirportEquals(User owner, Airport depAirport, Airport destAirport);
    List<Flight> findByOwnerAndFlightNumber(User owner, String flightNumber);
}
