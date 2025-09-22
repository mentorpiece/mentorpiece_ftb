package com.aerotravel.flightticketbooking.services.servicesimpl;

import com.aerotravel.flightticketbooking.exception.EntityNotFoundException;
import com.aerotravel.flightticketbooking.model.Airport;
import com.aerotravel.flightticketbooking.model.User;
import com.aerotravel.flightticketbooking.repository.AirportRepository;
import com.aerotravel.flightticketbooking.services.AirportService;
import com.aerotravel.flightticketbooking.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Optional;

@Service
@Validated
public class AirportServiceImpl extends AbstractEntityServiceImpl<Airport> implements AirportService {

    private static final int PAGE_SIZE = 10;
    private final AirportRepository airportRepository;
    private final UserService userService;
    private final String[] sortBy = new String[]{"airportName"};

    @Autowired
    public AirportServiceImpl(AirportRepository airportRepository, UserService userService) {
        this.airportRepository = airportRepository;
        this.userService = userService;
    }

    @Override
    protected JpaRepository<Airport, Long> getRepository() {
        return airportRepository;
    }

    @Override
    public List<Airport> getAll() {
        // Override to return only current user's airports by default
        return getCurrentUserAirports();
    }

    @Override
    public Airport save(Airport airport) {
        // Override to automatically assign to current user
        return saveAirportForCurrentUser(airport);
    }

    @Override
    protected String[] getSortByProperties() {
        return sortBy;
    }

    @Override
    public Airport getByCode(String airportCode) {
        if (null == airportCode) throw new IllegalArgumentException("Airport code shall not be null.");

        return airportRepository.findByAirportCode(airportCode)
                .orElseThrow(() -> new EntityNotFoundException("Could not find airport by code=" + airportCode));
    }

    @Override
    public List<Airport> getCurrentUserAirports() {
        User currentUser = userService.getCurrentUser();
        return airportRepository.findAllByOwner(currentUser);
    }

    @Override
    public Airport getCurrentUserAirportByCode(String airportCode) {
        if (null == airportCode) throw new IllegalArgumentException("Airport code shall not be null.");

        User currentUser = userService.getCurrentUser();
        return airportRepository.findByOwnerAndAirportCode(currentUser, airportCode)
                .orElseThrow(() -> new EntityNotFoundException("Could not find airport by code=" + airportCode + " for current user"));
    }

    @Override
    public Airport saveAirportForCurrentUser(Airport airport) {
        User currentUser = userService.getCurrentUser();
        airport.setOwner(currentUser);
        return airportRepository.save(airport);
    }

    // SECURITY OVERRIDES - Ensure user ownership validation

    @Override
    public Airport getById(Long entityId) {
        if (null == entityId) throw new IllegalArgumentException("Entity ID shall not be null.");
        Airport entity = airportRepository.findById(entityId)
                .orElseThrow(() -> new EntityNotFoundException("Airport not found with id: " + entityId));
        User currentUser = userService.getCurrentUser();
        if (!entity.getOwner().getId().equals(currentUser.getId())) {
            throw new EntityNotFoundException("Airport not found with id: " + entityId);
        }
        return entity;
    }

    @Override
    public Optional<Airport> getOptionallyById(Long entityId) {
        if (null == entityId) return Optional.empty();
        Optional<Airport> entity = airportRepository.findById(entityId);
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
        airportRepository.deleteById(entityId);
    }

    @Override
    public Page<Airport> getAllPaged(int pageNum) {
        User currentUser = userService.getCurrentUser();
        return airportRepository.findAllByOwner(currentUser,
                PageRequest.of(pageNum, PAGE_SIZE, Sort.by(getSortByProperties())));
    }

    @Override
    public Page<Airport> getPaged(Pageable pageable) {
        User currentUser = userService.getCurrentUser();
        return airportRepository.findAllByOwner(currentUser, pageable);
    }
}
