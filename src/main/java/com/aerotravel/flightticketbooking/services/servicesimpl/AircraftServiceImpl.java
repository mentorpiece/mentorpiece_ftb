package com.aerotravel.flightticketbooking.services.servicesimpl;

import com.aerotravel.flightticketbooking.model.Aircraft;
import com.aerotravel.flightticketbooking.repository.AircraftRepository;
import com.aerotravel.flightticketbooking.services.AircraftService;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Random;

@Slf4j
@Service
@Validated
public class AircraftServiceImpl extends AbstractEntityServiceImpl<Aircraft> implements AircraftService {
    private final AircraftRepository aircraftRepository;
    private final String[] sortBy = new String[]{"model"};

    @Autowired
    public AircraftServiceImpl(AircraftRepository aircraftRepository) {
        this.aircraftRepository = aircraftRepository;
    }

    @Override
    protected JpaRepository<Aircraft, Long> getRepository() {
        return aircraftRepository;
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
        log.info("About to insert an aircraft like {}", data.toShortString());
        // For educational purpose only! Let's mimic delays.
        val rnd = new Random();
        Thread.sleep(rnd.nextInt(2187) + rnd.nextInt(3456));
        val saved = aircraftRepository.save(data);

        log.info("Just inserted an aircraft like {}", saved.toShortString());
        return saved;
    }

    @Override
    public List<Aircraft> saveAll(List<Aircraft> entities) {
        return aircraftRepository.saveAll(entities);
    }
}
