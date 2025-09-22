package com.aerotravel.flightticketbooking.services.servicesimpl;

import com.aerotravel.flightticketbooking.exception.EntityNotFoundException;
import com.aerotravel.flightticketbooking.model.Passenger;
import com.aerotravel.flightticketbooking.model.User;
import com.aerotravel.flightticketbooking.repository.PassengerRepository;
import com.aerotravel.flightticketbooking.services.PassengerService;
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

import java.util.List;

@Service
@Validated
public class PassengerServiceImpl extends AbstractEntityServiceImpl<Passenger> implements PassengerService {

    private static final int PAGE_SIZE = 10;
    private final PassengerRepository passengerRepository;
    private final UserService userService;
    private final String[] sortBy = new String[]{"lastName"};

    @Autowired
    public PassengerServiceImpl(PassengerRepository passengerRepository, UserService userService) {
        this.passengerRepository = passengerRepository;
        this.userService = userService;
    }

    @Override
    protected JpaRepository<Passenger, Long> getRepository() {
        return passengerRepository;
    }

    @Override
    public List<Passenger> getAll() {
        // Override to return only current user's passengers by default
        return getCurrentUserPassengers();
    }

    @Override
    public Passenger save(Passenger passenger) {
        // Override to automatically assign to current user
        return savePassengerForCurrentUser(passenger);
    }

    @Override
    protected String[] getSortByProperties() {
        return sortBy;
    }

    @Override
    public List<Passenger> getAllByByPassportNumber(String number) {
        return passengerRepository.findAllByPassportNumber(number);
    }

    @Override
    public List<Passenger> getCurrentUserPassengers() {
        User currentUser = userService.getCurrentUser();
        return passengerRepository.findAllByOwner(currentUser);
    }

    @Override
    public List<Passenger> getCurrentUserPassengersByPassportNumber(String number) {
        User currentUser = userService.getCurrentUser();
        return passengerRepository.findAllByOwnerAndPassportNumber(currentUser, number);
    }

    @Override
    public Passenger savePassengerForCurrentUser(Passenger passenger) {
        User currentUser = userService.getCurrentUser();
        passenger.setOwner(currentUser);
        return passengerRepository.save(passenger);
    }

    // SECURITY OVERRIDES - Ensure user ownership validation

    @Override
    public Passenger getById(Long entityId) {
        if (null == entityId) throw new IllegalArgumentException("Entity ID shall not be null.");
        Passenger entity = passengerRepository.findById(entityId)
                .orElseThrow(() -> new EntityNotFoundException("Passenger not found with id: " + entityId));
        User currentUser = userService.getCurrentUser();
        if (!entity.getOwner().getId().equals(currentUser.getId())) {
            throw new EntityNotFoundException("Passenger not found with id: " + entityId);
        }
        return entity;
    }

    @Override
    public Optional<Passenger> getOptionallyById(Long entityId) {
        if (null == entityId) return Optional.empty();
        Optional<Passenger> entity = passengerRepository.findById(entityId);
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
        passengerRepository.deleteById(entityId);
    }

    @Override
    public Page<Passenger> getAllPaged(int pageNum) {
        User currentUser = userService.getCurrentUser();
        return passengerRepository.findAllByOwner(currentUser,
                PageRequest.of(pageNum, PAGE_SIZE, Sort.by(getSortByProperties())));
    }

    @Override
    public Page<Passenger> getPaged(Pageable pageable) {
        User currentUser = userService.getCurrentUser();
        return passengerRepository.findAllByOwner(currentUser, pageable);
    }
}
