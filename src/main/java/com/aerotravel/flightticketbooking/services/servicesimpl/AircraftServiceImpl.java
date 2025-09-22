package com.aerotravel.flightticketbooking.services.servicesimpl;

import com.aerotravel.flightticketbooking.exception.EntityNotFoundException;
import com.aerotravel.flightticketbooking.model.Aircraft;
import com.aerotravel.flightticketbooking.model.User;
import com.aerotravel.flightticketbooking.repository.AircraftRepository;
import com.aerotravel.flightticketbooking.services.AircraftService;
import com.aerotravel.flightticketbooking.services.UserService;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Optional;

import java.util.List;
import java.util.Random;

@Slf4j
@Service
@Validated
public class AircraftServiceImpl extends AbstractEntityServiceImpl<Aircraft> implements AircraftService {
    private static final int PAGE_SIZE = 10;
    private final AircraftRepository aircraftRepository;
    private final UserService userService;
    private final String[] sortBy = new String[]{"model"};

    @Autowired
    public AircraftServiceImpl(AircraftRepository aircraftRepository, UserService userService) {
        this.aircraftRepository = aircraftRepository;
        this.userService = userService;
    }

    @Override
    protected JpaRepository<Aircraft, Long> getRepository() {
        return aircraftRepository;
    }

    @Override
    public List<Aircraft> getAll() {
        // Override to return only current user's aircraft by default
        return getCurrentUserAircraft();
    }

    @Override
    public Aircraft save(Aircraft aircraft) {
        // Override to automatically assign to current user
        return saveAircraftForCurrentUser(aircraft);
    }

    @Override
    protected String[] getSortByProperties() {
        return sortBy;
    }

    @Override
    public List<Aircraft> getByModel(String modelName) {
        return aircraftRepository.findByModel(modelName);
    }

    @Override
    public List<Aircraft> getByManufacturer(String manufacturerName) {
        return aircraftRepository.findByManufacturer(manufacturerName);
    }

    @Async
    @Override
    public Aircraft saveAsync(Aircraft data) throws InterruptedException {
        log.info("About to insert an aircraft like {}", data.describe());
        // For educational purpose only! Let's mimic delays.
        val rnd = new Random();
        Thread.sleep(rnd.nextInt(2187) + rnd.nextInt(3456));
        val saved = aircraftRepository.save(data);

        log.info("Just inserted an aircraft like {}", saved.describe());
        return saved;
    }

    @Override
    public List<Aircraft> saveAll(List<Aircraft> entities) {
        return aircraftRepository.saveAll(entities);
    }

    @Override
    public List<Aircraft> getCurrentUserAircraft() {
        User currentUser = userService.getCurrentUser();
        return aircraftRepository.findAllByOwner(currentUser);
    }

    @Override
    public List<Aircraft> getCurrentUserAircraftByModel(String modelName) {
        User currentUser = userService.getCurrentUser();
        return aircraftRepository.findByOwnerAndModel(currentUser, modelName);
    }

    @Override
    public List<Aircraft> getCurrentUserAircraftByManufacturer(String manufacturerName) {
        User currentUser = userService.getCurrentUser();
        return aircraftRepository.findByOwnerAndManufacturer(currentUser, manufacturerName);
    }

    @Override
    public Aircraft saveAircraftForCurrentUser(Aircraft aircraft) {
        User currentUser = userService.getCurrentUser();
        aircraft.setOwner(currentUser);
        return aircraftRepository.save(aircraft);
    }

    // SECURITY OVERRIDES - Ensure user ownership validation

    @Override
    public Aircraft getById(Long entityId) {
        if (null == entityId) throw new IllegalArgumentException("Entity ID shall not be null.");
        Aircraft entity = aircraftRepository.findById(entityId)
                .orElseThrow(() -> new EntityNotFoundException("Aircraft not found with id: " + entityId));
        User currentUser = userService.getCurrentUser();
        if (!entity.getOwner().getId().equals(currentUser.getId())) {
            throw new EntityNotFoundException("Aircraft not found with id: " + entityId);
        }
        return entity;
    }

    @Override
    public Optional<Aircraft> getOptionallyById(Long entityId) {
        if (null == entityId) return Optional.empty();
        Optional<Aircraft> entity = aircraftRepository.findById(entityId);
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
        aircraftRepository.deleteById(entityId);
    }

    @Override
    public Page<Aircraft> getAllPaged(int pageNum) {
        User currentUser = userService.getCurrentUser();
        return aircraftRepository.findAllByOwner(currentUser,
                PageRequest.of(pageNum, PAGE_SIZE, Sort.by(getSortByProperties())));
    }

    @Override
    public Page<Aircraft> getPaged(Pageable pageable) {
        User currentUser = userService.getCurrentUser();
        return aircraftRepository.findAllByOwner(currentUser, pageable);
    }
}
