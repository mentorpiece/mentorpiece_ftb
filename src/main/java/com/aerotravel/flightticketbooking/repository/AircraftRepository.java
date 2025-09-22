package com.aerotravel.flightticketbooking.repository;

import com.aerotravel.flightticketbooking.model.Aircraft;
import com.aerotravel.flightticketbooking.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AircraftRepository extends JpaRepository<Aircraft, Long> {
    List<Aircraft> findByModel(String model);
    List<Aircraft> findByManufacturer(String manufacturer);

    // User-filtered methods
    List<Aircraft> findAllByOwner(User owner);
    Page<Aircraft> findAllByOwner(User owner, Pageable pageable);
    List<Aircraft> findByOwnerAndModel(User owner, String model);
    List<Aircraft> findByOwnerAndManufacturer(User owner, String manufacturer);
}
