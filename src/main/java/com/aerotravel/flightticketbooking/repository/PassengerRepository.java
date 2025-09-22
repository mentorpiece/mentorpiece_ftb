package com.aerotravel.flightticketbooking.repository;

import com.aerotravel.flightticketbooking.model.Passenger;
import com.aerotravel.flightticketbooking.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PassengerRepository extends JpaRepository<Passenger, Long> {

    List<Passenger> findAllByPassportNumber(String number);

    // User-filtered methods
    List<Passenger> findAllByOwner(User owner);
    Page<Passenger> findAllByOwner(User owner, Pageable pageable);
    List<Passenger> findAllByOwnerAndPassportNumber(User owner, String number);
}
