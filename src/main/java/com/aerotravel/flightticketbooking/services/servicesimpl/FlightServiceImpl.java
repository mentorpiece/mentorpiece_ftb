package com.aerotravel.flightticketbooking.services.servicesimpl;

import com.aerotravel.flightticketbooking.exception.EntityNotFoundException;
import com.aerotravel.flightticketbooking.model.Airport;
import com.aerotravel.flightticketbooking.model.Flight;
import com.aerotravel.flightticketbooking.model.User;
import com.aerotravel.flightticketbooking.repository.FlightRepository;
import com.aerotravel.flightticketbooking.services.FlightService;
import com.aerotravel.flightticketbooking.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Optional;

import java.time.LocalDate;
import java.util.List;

@Service
@Validated
public class FlightServiceImpl extends AbstractEntityServiceImpl<Flight> implements FlightService {

    private static final int PAGE_SIZE = 10;
    private final FlightRepository flightRepository;
    private final UserService userService;
    private final String[] sortBy = new String[]{"departureDate"};

    @Autowired
    public FlightServiceImpl(FlightRepository flightRepository, UserService userService) {
        this.flightRepository = flightRepository;
        this.userService = userService;
    }

    @Override
    protected JpaRepository<Flight, Long> getRepository() {
        return flightRepository;
    }

    @Override
    public List<Flight> getAll() {
        // Override to return only current user's flights by default
        return getCurrentUserFlights();
    }

    @Override
    public Flight save(Flight flight) {
        // Override to automatically assign to current user
        return saveFlightForCurrentUser(flight);
    }

    @Override
    protected String[] getSortByProperties() {
        return sortBy;
    }

    @Override
    public List<Flight> getAllByAirportAndDepartureTime(Airport depAirport, Airport destAirport, LocalDate depDate) {
        return flightRepository.findAllByDepartureAirportEqualsAndDestinationAirportEqualsAndDepartureDateEquals(depAirport, destAirport, depDate);
    }

    @Override
    public List<Flight> getAllByAirports(Airport depAirport, Airport destAirport) {
        return flightRepository.findAllByDepartureAirportEqualsAndDestinationAirportEquals(depAirport, destAirport);
    }

    @Override
    public List<Flight> getAllByByFlightNumber(String flightNumber) {
        return flightRepository.findByFlightNumber(flightNumber);
    }

    @Override
    public List<Flight> getCurrentUserFlights() {
        User currentUser = userService.getCurrentUser();
        return flightRepository.findAllByOwner(currentUser);
    }

    @Override
    public List<Flight> getCurrentUserFlightsByAirportAndDepartureTime(Airport depAirport, Airport destAirport, LocalDate depDate) {
        User currentUser = userService.getCurrentUser();
        return flightRepository.findAllByOwnerAndDepartureAirportEqualsAndDestinationAirportEqualsAndDepartureDateEquals(
                currentUser, depAirport, destAirport, depDate);
    }

    @Override
    public List<Flight> getCurrentUserFlightsByAirports(Airport depAirport, Airport destAirport) {
        User currentUser = userService.getCurrentUser();
        return flightRepository.findAllByOwnerAndDepartureAirportEqualsAndDestinationAirportEquals(
                currentUser, depAirport, destAirport);
    }

    @Override
    public List<Flight> getCurrentUserFlightsByFlightNumber(String flightNumber) {
        User currentUser = userService.getCurrentUser();
        return flightRepository.findByOwnerAndFlightNumber(currentUser, flightNumber);
    }

    @Override
    public Flight saveFlightForCurrentUser(Flight flight) {
        User currentUser = userService.getCurrentUser();
        flight.setOwner(currentUser);
        return flightRepository.save(flight);
    }

    // SECURITY OVERRIDES - Ensure user ownership validation

    @Override
    public Flight getById(Long entityId) {
        if (null == entityId) throw new IllegalArgumentException("Entity ID shall not be null.");
        Flight entity = flightRepository.findById(entityId)
                .orElseThrow(() -> new EntityNotFoundException("Flight not found with id: " + entityId));
        User currentUser = userService.getCurrentUser();
        if (!entity.getOwner().getId().equals(currentUser.getId())) {
            throw new EntityNotFoundException("Flight not found with id: " + entityId);
        }
        return entity;
    }

    @Override
    public Optional<Flight> getOptionallyById(Long entityId) {
        if (null == entityId) return Optional.empty();
        Optional<Flight> entity = flightRepository.findById(entityId);
        if (entity.isPresent()) {
            User currentUser = userService.getCurrentUser();
            if (!entity.get().getOwner().getId().equals(currentUser.getId())) {
                return Optional.empty();
            }
        }
        return entity;
    }

    @Override
    public void deleteById(Long entityId) {
        getById(entityId); // This validates ownership
        flightRepository.deleteById(entityId);
    }

    @Override
    public Page<Flight> getAllPaged(int pageNum) {
        User currentUser = userService.getCurrentUser();
        return flightRepository.findAllByOwner(currentUser,
                PageRequest.of(pageNum, PAGE_SIZE, Sort.by(getSortByProperties())));
    }

    @Override
    public Page<Flight> getPaged(Pageable pageable) {
        User currentUser = userService.getCurrentUser();
        return flightRepository.findAllByOwner(currentUser, pageable);
    }
}
