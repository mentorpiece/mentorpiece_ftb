package com.aerotravel.flightticketbooking.services.aux;

import com.aerotravel.flightticketbooking.model.Aircraft;
import com.aerotravel.flightticketbooking.model.Airport;
import com.aerotravel.flightticketbooking.model.Flight;
import com.aerotravel.flightticketbooking.model.Passenger;
import com.aerotravel.flightticketbooking.repository.AircraftRepository;
import com.aerotravel.flightticketbooking.repository.AirportRepository;
import com.aerotravel.flightticketbooking.repository.FlightRepository;
import com.aerotravel.flightticketbooking.repository.PassengerRepository;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.datafaker.Faker;
import net.datafaker.providers.base.Address;
import net.datafaker.providers.base.Aviation;
import net.datafaker.providers.base.DateAndTime;
import net.datafaker.providers.base.Name;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class DataGenService {
    public static final int ALMOST_UPPER_BOUND = 12;
    private final AircraftRepository aircraftRepository;
    private final AirportRepository airportRepository;
    private final FlightRepository flightRepository;
    private final PassengerRepository passengerRepository;

    private final Faker faker = new Faker(Locale.ENGLISH);
    private final Name nameFaker = faker.name();
    private final Aviation aviaFaker = faker.aviation();
    private final DateAndTime dateFaker = faker.date();
    private final Address addressFaker = faker.address();

    @Autowired
    public DataGenService(AircraftRepository aircraftRepository, AirportRepository airportRepository, FlightRepository flightRepository, PassengerRepository passengerRepository) {
        this.aircraftRepository = aircraftRepository;
        this.airportRepository = airportRepository;
        this.flightRepository = flightRepository;
        this.passengerRepository = passengerRepository;
    }

    public Map<String, List<String>> generateAll() {
        log.info("About to generate and persist fake data.");
        val rnd = new Random();

        // Use both existing and freshly generated data.
        createAircrafts(rnd);
        val aircrafts = aircraftRepository.findAll();

        createAirports();
        val airports = airportRepository.findAll();

        createFlights(rnd, aircrafts, airports);
        val flights = flightRepository.findAll();

        val passengers = createPassengers(rnd, flights);

        val data = new TreeMap<String, List<String>>();
        data.put("Aircrafts", aircrafts.stream().map(Aircraft::describe).collect(Collectors.toList()));
        data.put("Airports", airports.stream().map(Airport::describe).collect(Collectors.toList()));
        data.put("Flights", flights.stream().map(Flight::describe).collect(Collectors.toList()));
        data.put("Passengers", passengers.stream().map(Passenger::toString).collect(Collectors.toList()));

        return data;
    }

    public List<Aircraft> createAircrafts(Random rnd) {
        log.info("\n\tAbout to create fake aircrafts.\n");
        var aircrafts = new ArrayList<Aircraft>();
        for (int i = 0; i < ALMOST_UPPER_BOUND; i++) {
            val model = aviaFaker.aircraft();
            val aircraft = Aircraft.builder()
                    .model(model)
                    .manufacturer(model.split("-|\\s")[0])
                    .numberOfSeats(1 + Math.abs(rnd.nextInt(850)))
                    .build();

            aircrafts.add(aircraft);
        }

        addPredefinedAircraftsIfNeeded(aircrafts);
        val existingData = aircraftRepository.findAll();
        val existingModels = existingData.stream()
                .map(Aircraft::getModel)
                .toList();
        val newModels = aircrafts.stream()
                .filter(a-> !existingModels.contains(a.getModel())).toList();

        return aircraftRepository.saveAll(newModels);
    }

    private void addPredefinedAircraftsIfNeeded(List<Aircraft> aircrafts) {
        val tupolev = "Tupolev";
        val il = "Ilushin";
        val cyxou = "Cyxou";
        val yakovlev = "Yakovlev";
        val airbus = "Airbus";
        val boeing = "Boeing";

        aircrafts.add(new Aircraft(tupolev, "Tu-214", 150));
        aircrafts.add(new Aircraft(tupolev, "Tu-154", 140));
        aircrafts.add(new Aircraft(tupolev, "Tu-154M", 140));
        aircrafts.add(new Aircraft(tupolev, "Tu-134", 110));
        aircrafts.add(new Aircraft(tupolev, "Tu-114", 100));
        aircrafts.add(new Aircraft(tupolev, "Tu-554", 729));
        aircrafts.add(new Aircraft(il, "Il-86", 230));
        aircrafts.add(new Aircraft(il, "Il-96", 240));
        aircrafts.add(new Aircraft(il, "Il-76", 60));
        aircrafts.add(new Aircraft(il, "Il-18", 40));
        aircrafts.add(new Aircraft(il, "Il-103", 3));
        aircrafts.add(new Aircraft(il, "Il-2", 1));
        aircrafts.add(new Aircraft(cyxou, "SSJ", 99));
        aircrafts.add(new Aircraft(cyxou, "SSJ-N", 100));
        aircrafts.add(new Aircraft(yakovlev, "Yak-242", 150));
        aircrafts.add(new Aircraft(yakovlev, "Yak-42", 50));
        aircrafts.add(new Aircraft(airbus, "A-319", 99));
        aircrafts.add(new Aircraft(airbus, "A-320", 110));
        aircrafts.add(new Aircraft(airbus, "A-321", 120));
        aircrafts.add(new Aircraft(airbus, "A-340", 220));
        aircrafts.add(new Aircraft(airbus, "A-350", 220));
        aircrafts.add(new Aircraft(airbus, "A-380", 820));
        aircrafts.add(new Aircraft(boeing, "B-737", 150));
        aircrafts.add(new Aircraft(boeing, "B-747", 300));
        aircrafts.add(new Aircraft(boeing, "B-757", 300));
        aircrafts.add(new Aircraft(boeing, "B-777", 250));
        aircrafts.add(new Aircraft(boeing, "B-787", 350));
    }

    public List<Airport> createAirports() {
        log.info("\n\tAbout to create fake airports.\n");
        var data = new ArrayList<Airport>();
        var codes = new ArrayList<String>();
        for (int i = 0; i < ALMOST_UPPER_BOUND; i++) {
            var code = aviaFaker.airport();
            if (codes.contains(code)) {
                code = faker.bothify("????-?");
            }
            if (codes.contains(code)) { // Double-check.
                code += code + "-M";
            }

            if (!codes.contains(code)) {
                codes.add(code);

                var entry = Airport.builder()
                        .airportCode(code)
                        .airportName(aviaFaker.airportName())
                        .city(addressFaker.city())
                        .state(addressFaker.state())
                        .country(addressFaker.country())
                        .build();

                data.add(entry);
            }
        }

        data.add(new Airport("DAL", "Dallas Love Field", "Dallas", "Dallas", "United States"));
        data.add(new Airport("DCG", "Dubai Creek SPB", "Dubai", "Dubai", "United Arab Emirates"));
        data.add(new Airport("CID", "Cedar Rapid Airport", "IOWA", "United States", "Iowa"));
        data.add(new Airport("CHI", "Chicago Airport", "Chicago", "Illinois", "United States"));
        data.add(new Airport("CLN", "California Airport", "California", "California", "United States"));
        data.add(new Airport("TEX", "Texas Airport", "Texas", "Texas", "United States"));

        var existingData = airportRepository.findAll();
        var existingCodes = existingData.stream()
                .map(Airport::getAirportCode)
                .toList();
        var filteredData = data.stream()
                .filter(a-> !existingCodes.contains(a.getAirportCode())).toList();
        try {
            return airportRepository.saveAll(filteredData);
        } catch (Throwable e) {
            if (null != e.getCause()
                    && null != e.getCause().getCause()
                    &&
                    e.getCause()
                            .getCause()
                            .getMessage().contains("Duplicate entry")) {
                log.error("Seems there are duplicate airport code upon fake data generating. " +
                        "That is fine, let's just ignore that.", e);
                return List.of();
            }
            throw new RuntimeException(e);
        }
    }

    public List<Flight> createFlights(Random rnd, List<Aircraft> aircrafts, List<Airport> airports) {
        log.info("\n\tAbout to create fake flights.\n");
        var data = new ArrayList<Flight>();
        for (int i = 0; i < ALMOST_UPPER_BOUND; i++) {
            val depDate = LocalDate.ofInstant(dateFaker.future(1 + rnd.nextInt(144), TimeUnit.DAYS).toInstant(),
                    ZoneId.systemDefault());

            val entry = Flight.builder()
                    .flightNumber(aviaFaker.flight())
                    .flightCharge(Double.MAX_EXPONENT * rnd.nextDouble())
                    .aircraft(getAny(rnd, aircrafts))
                    .departureDate(depDate)
                    .arrivalDate(depDate.plus(rnd.nextInt(2), ChronoUnit.DAYS))
                    .departureTime(dateFaker.future(1 + rnd.nextInt(36), TimeUnit.HOURS).toString())
                    .arrivalTime(dateFaker.future(2 + rnd.nextInt(36), TimeUnit.HOURS).toString())
                    .departureAirport(getAny(rnd, airports))
                    .destinationAirport(getAny(rnd, airports))
                    .gate(aviaFaker.gate())
                    .status(aviaFaker.flightStatus())
                    .build();

            data.add(entry);
        }

        return flightRepository.saveAll(data);
    }

    public List<Passenger> createPassengers(Random rnd, List<Flight> flights) {
        log.info("\n\tAbout to create fake passengers.\n");
        List<Passenger> data = new ArrayList<>();
        for (int i = 0; i < ALMOST_UPPER_BOUND; i++) {
            var entry = Passenger.builder()
                    .flight(getAny(rnd, flights))
                    .firstName(nameFaker.firstName())
                    .lastName(nameFaker.lastName())
                    .phoneNumber(faker.phoneNumber().phoneNumber())
                    .email(faker.internet().safeEmailAddress())
                    .passportNumber(faker.regexify("[0-9]{4} \\d{6}"))
                    .address(addressFaker.fullAddress())
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
