package com.aerotravel.flightticketbooking.repository;

import com.aerotravel.flightticketbooking.model.Airport;
import com.aerotravel.flightticketbooking.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AirportRepository extends JpaRepository<Airport, Long> {
    Optional<Airport> findByAirportCode(String airportCode);

    List<Airport> findAllByOwner(User owner);
    Page<Airport> findAllByOwner(User owner, Pageable pageable);
    Optional<Airport> findByOwnerAndAirportCode(User owner, String airportCode);
}
