package com.aerotravel.flightticketbooking.services;

import com.aerotravel.flightticketbooking.model.*;
import com.aerotravel.flightticketbooking.repository.*;
import com.aerotravel.flightticketbooking.services.aux.DataGenService;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.datafaker.Faker;
import net.datafaker.providers.base.Address;
import net.datafaker.providers.base.Aviation;
import net.datafaker.providers.base.Name;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class UserDataInitializationService {

    private final AircraftRepository aircraftRepository;
    private final AirportRepository airportRepository;
    private final FlightRepository flightRepository;
    private final PassengerRepository passengerRepository;
    private final DataGenService dataGenService;

    private final Faker faker = new Faker(Locale.ENGLISH);
    private final Name nameFaker = faker.name();
    private final Aviation aviaFaker = faker.aviation();
    private final Address addressFaker = faker.address();

    @Autowired
    public UserDataInitializationService(AircraftRepository aircraftRepository,
                                       AirportRepository airportRepository,
                                       FlightRepository flightRepository,
                                       PassengerRepository passengerRepository,
                                       DataGenService dataGenService) {
        this.aircraftRepository = aircraftRepository;
        this.airportRepository = airportRepository;
        this.flightRepository = flightRepository;
        this.passengerRepository = passengerRepository;
        this.dataGenService = dataGenService;
    }

    /**
     * Creates sample data for a new user using DataGenService and assigns all data to the user
     */
    public void initializeUserData(User user) {
        log.info("Initializing complete sample data for user: {}", user.getUsername());

        try {
            Random rnd = new Random();

            // 1. Create user-specific airports using DataGenService patterns
            List<Airport> userAirports = createUserAirportsFromDataGen(user, rnd);
            log.info("Created {} airports for user {}", userAirports.size(), user.getUsername());

            // 2. Create aircraft for this user using DataGenService logic
            List<Aircraft> userAircraft = createUserAircraftFromDataGen(user, rnd);
            log.info("Created {} aircraft for user {}", userAircraft.size(), user.getUsername());

            // 3. Create flights for this user using DataGenService logic
            List<Flight> userFlights = createUserFlightsFromDataGen(user, rnd, userAircraft, userAirports);
            log.info("Created {} flights for user {}", userFlights.size(), user.getUsername());

            // 4. Create passengers for this user using DataGenService logic
            List<Passenger> userPassengers = createUserPassengersFromDataGen(user, rnd, userFlights);
            log.info("Created {} passengers for user {}", userPassengers.size(), user.getUsername());

            log.info("Complete sample data initialization completed for user: {}. Created {} airports, {} aircraft, {} flights, {} passengers",
                    user.getUsername(), userAirports.size(), userAircraft.size(), userFlights.size(), userPassengers.size());

        } catch (Exception e) {
            log.error("Failed to initialize sample data for user: {}", user.getUsername(), e);
        }
    }

    private List<Airport> createUserAirportsFromDataGen(User user, Random rnd) {
        var airports = new ArrayList<Airport>();

        // Use DataGenService logic - create random airports + predefined ones for this user
        for (int i = 0; i < 12; i++) {
            String code = aviaFaker.airport();

            // Check for duplicates and generate alternatives if needed
            Set<String> existingCodes = airports.stream()
                    .map(Airport::getAirportCode)
                    .collect(Collectors.toSet());

            if (existingCodes.contains(code)) {
                code = faker.bothify("????-?");
            }
            if (existingCodes.contains(code)) {
                code = code + "-M";
            }

            String finalCode = code + "-" + user.getUsername().toUpperCase();
            var entry = Airport.builder()
                    .airportCode(finalCode)
                    .airportName(aviaFaker.airportName())
                    .city(addressFaker.city())
                    .state(addressFaker.state())
                    .country(addressFaker.country())
                    .owner(user)  // Assign to user
                    .build();

            airports.add(entry);
        }

        // Add predefined airports for this user (from DataGenService)
        addPredefinedAirportsForUser(airports, user);

        return airportRepository.saveAll(airports);
    }

    private void addPredefinedAirportsForUser(List<Airport> airports, User user) {
        // Add all predefined airports with user ownership
        airports.add(Airport.builder().airportCode("DAL-" + user.getUsername()).airportName("Dallas Love Field").city("Dallas").state("Dallas").country("United States").owner(user).build());
        airports.add(Airport.builder().airportCode("DCG-" + user.getUsername()).airportName("Dubai Creek SPB").city("Dubai").state("Dubai").country("United Arab Emirates").owner(user).build());
        airports.add(Airport.builder().airportCode("CID-" + user.getUsername()).airportName("Cedar Rapid Airport").city("IOWA").state("United States").country("Iowa").owner(user).build());
        airports.add(Airport.builder().airportCode("CHI-" + user.getUsername()).airportName("Chicago Airport").city("Chicago").state("Illinois").country("United States").owner(user).build());
        airports.add(Airport.builder().airportCode("CLN-" + user.getUsername()).airportName("California Airport").city("California").state("California").country("United States").owner(user).build());
        airports.add(Airport.builder().airportCode("TEX-" + user.getUsername()).airportName("Texas Airport").city("Texas").state("Texas").country("United States").owner(user).build());
    }

    private List<Aircraft> createUserAircraftFromDataGen(User user, Random rnd) {
        var aircrafts = new ArrayList<Aircraft>();

        // Use DataGenService logic - create random aircraft + predefined ones for this user
        for (int i = 0; i < 12; i++) {
            val model = aviaFaker.aircraft();
            val aircraft = Aircraft.builder()
                    .model(model + "-" + user.getUsername().toUpperCase())
                    .manufacturer(model.split("-|\\s")[0])
                    .numberOfSeats(1 + Math.abs(rnd.nextInt(850)))
                    .owner(user)  // Assign to user
                    .build();

            aircrafts.add(aircraft);
        }

        // Add predefined aircraft for this user (from DataGenService)
        addPredefinedAircraftsForUser(aircrafts, user);

        return aircraftRepository.saveAll(aircrafts);
    }

    private void addPredefinedAircraftsForUser(List<Aircraft> aircrafts, User user) {
        val tupolev = "Tupolev";
        val il = "Ilushin";
        val cyxou = "Cyxou";
        val yakovlev = "Yakovlev";
        val airbus = "Airbus";
        val boeing = "Boeing";

        // Add all predefined aircraft with user ownership
        aircrafts.add(Aircraft.builder().manufacturer(tupolev).model("Tu-214-" + user.getUsername()).numberOfSeats(150).owner(user).build());
        aircrafts.add(Aircraft.builder().manufacturer(tupolev).model("Tu-154-" + user.getUsername()).numberOfSeats(140).owner(user).build());
        aircrafts.add(Aircraft.builder().manufacturer(tupolev).model("Tu-154M-" + user.getUsername()).numberOfSeats(140).owner(user).build());
        aircrafts.add(Aircraft.builder().manufacturer(tupolev).model("Tu-134-" + user.getUsername()).numberOfSeats(110).owner(user).build());
        aircrafts.add(Aircraft.builder().manufacturer(tupolev).model("Tu-114-" + user.getUsername()).numberOfSeats(100).owner(user).build());
        aircrafts.add(Aircraft.builder().manufacturer(tupolev).model("Tu-554-" + user.getUsername()).numberOfSeats(729).owner(user).build());
        aircrafts.add(Aircraft.builder().manufacturer(il).model("Il-86-" + user.getUsername()).numberOfSeats(230).owner(user).build());
        aircrafts.add(Aircraft.builder().manufacturer(il).model("Il-96-" + user.getUsername()).numberOfSeats(240).owner(user).build());
        aircrafts.add(Aircraft.builder().manufacturer(il).model("Il-76-" + user.getUsername()).numberOfSeats(60).owner(user).build());
        aircrafts.add(Aircraft.builder().manufacturer(il).model("Il-18-" + user.getUsername()).numberOfSeats(40).owner(user).build());
        aircrafts.add(Aircraft.builder().manufacturer(il).model("Il-103-" + user.getUsername()).numberOfSeats(3).owner(user).build());
        aircrafts.add(Aircraft.builder().manufacturer(il).model("Il-2-" + user.getUsername()).numberOfSeats(1).owner(user).build());
        aircrafts.add(Aircraft.builder().manufacturer(cyxou).model("SSJ-" + user.getUsername()).numberOfSeats(99).owner(user).build());
        aircrafts.add(Aircraft.builder().manufacturer(cyxou).model("SSJ-N-" + user.getUsername()).numberOfSeats(100).owner(user).build());
        aircrafts.add(Aircraft.builder().manufacturer(yakovlev).model("Yak-242-" + user.getUsername()).numberOfSeats(150).owner(user).build());
        aircrafts.add(Aircraft.builder().manufacturer(yakovlev).model("Yak-42-" + user.getUsername()).numberOfSeats(50).owner(user).build());
        aircrafts.add(Aircraft.builder().manufacturer(airbus).model("A-319-" + user.getUsername()).numberOfSeats(99).owner(user).build());
        aircrafts.add(Aircraft.builder().manufacturer(airbus).model("A-320-" + user.getUsername()).numberOfSeats(110).owner(user).build());
        aircrafts.add(Aircraft.builder().manufacturer(airbus).model("A-321-" + user.getUsername()).numberOfSeats(120).owner(user).build());
        aircrafts.add(Aircraft.builder().manufacturer(airbus).model("A-340-" + user.getUsername()).numberOfSeats(220).owner(user).build());
        aircrafts.add(Aircraft.builder().manufacturer(airbus).model("A-350-" + user.getUsername()).numberOfSeats(220).owner(user).build());
        aircrafts.add(Aircraft.builder().manufacturer(airbus).model("A-380-" + user.getUsername()).numberOfSeats(820).owner(user).build());
        aircrafts.add(Aircraft.builder().manufacturer(boeing).model("B-737-" + user.getUsername()).numberOfSeats(150).owner(user).build());
        aircrafts.add(Aircraft.builder().manufacturer(boeing).model("B-747-" + user.getUsername()).numberOfSeats(300).owner(user).build());
        aircrafts.add(Aircraft.builder().manufacturer(boeing).model("B-757-" + user.getUsername()).numberOfSeats(300).owner(user).build());
        aircrafts.add(Aircraft.builder().manufacturer(boeing).model("B-777-" + user.getUsername()).numberOfSeats(250).owner(user).build());
        aircrafts.add(Aircraft.builder().manufacturer(boeing).model("B-787-" + user.getUsername()).numberOfSeats(350).owner(user).build());
    }

    private List<Flight> createUserFlightsFromDataGen(User user, Random rnd, List<Aircraft> userAircraft, List<Airport> airports) {
        var data = new ArrayList<Flight>();

        // Use DataGenService logic - create 12 flights for this user
        for (int i = 0; i < 12; i++) {
            val depDate = LocalDate.ofInstant(faker.date().future(1 + rnd.nextInt(144), java.util.concurrent.TimeUnit.DAYS).toInstant(),
                    java.time.ZoneId.systemDefault());

            val entry = Flight.builder()
                    .flightNumber(aviaFaker.flight() + "-" + user.getUsername())
                    .flightCharge(Double.MAX_EXPONENT * rnd.nextDouble())
                    .aircraft(getAny(rnd, userAircraft))
                    .departureDate(depDate)
                    .arrivalDate(depDate.plus(rnd.nextInt(2), ChronoUnit.DAYS))
                    .departureTime(faker.date().future(1 + rnd.nextInt(36), java.util.concurrent.TimeUnit.HOURS).toString())
                    .arrivalTime(faker.date().future(2 + rnd.nextInt(36), java.util.concurrent.TimeUnit.HOURS).toString())
                    .departureAirport(getAny(rnd, airports))
                    .destinationAirport(getAny(rnd, airports))
                    .gate(aviaFaker.gate())
                    .status(aviaFaker.flightStatus())
                    .owner(user)  // Assign to user
                    .build();

            data.add(entry);
        }

        return flightRepository.saveAll(data);
    }

    private List<Passenger> createUserPassengersFromDataGen(User user, Random rnd, List<Flight> userFlights) {
        List<Passenger> data = new ArrayList<>();

        // Use DataGenService logic - create 12 passengers for this user
        for (int i = 0; i < 12; i++) {
            var entry = Passenger.builder()
                    .flight(getAny(rnd, userFlights))
                    .firstName(nameFaker.firstName())
                    .lastName(nameFaker.lastName())
                    .phoneNumber(faker.phoneNumber().phoneNumber())
                    .email(faker.internet().safeEmailAddress())
                    .passportNumber(faker.regexify("[0-9]{4} \\d{6}"))
                    .address(addressFaker.fullAddress())
                    .owner(user)  // Assign to user
                    .build();

            data.add(entry);
        }

        return passengerRepository.saveAll(data);
    }

    private <R> R getAny(Random rnd, List<R> entities) {
        val index = rnd.nextInt(entities.size());
        return entities.get(index);
    }
}