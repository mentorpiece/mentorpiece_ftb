package com.aerotravel.flightticketbooking.rest.v0;

import com.aerotravel.flightticketbooking.exception.EntityNotFoundException;
import com.aerotravel.flightticketbooking.model.Flight;
import com.aerotravel.flightticketbooking.model.Passenger;
import com.aerotravel.flightticketbooking.model.dto.FlightDto;
import com.aerotravel.flightticketbooking.model.dto.PassengerDto;
import com.aerotravel.flightticketbooking.services.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v0/flights")
@Tag(name = "Flight", description = "Flight resource. Flight booking is possible as well!")
@Slf4j
@Validated
public class FlightRestController extends AbstractRestController<Flight, FlightDto> {

    private final static DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final FlightService flightService;
    private final AirportService airportService;
    private final AircraftService aircraftService;
    private final PassengerService passengerService;

    @Autowired
    public FlightRestController(FlightService flightService, AirportService airportService, AircraftService aircraftService, PassengerService passengerService) {
        this.flightService = flightService;
        this.airportService = airportService;
        this.aircraftService = aircraftService;
        this.passengerService = passengerService;
    }

    @Override
    protected EntityService<Flight> getService() {
        return flightService;
    }

    @Override
    protected Class<Flight> getEntityClass() {
        return Flight.class;
    }

    @Override
    protected FlightDto convertToDto(Flight entity) {
        return modelMapper.map(entity, FlightDto.class);
    }

    @Override
    protected Flight convertToEntity(FlightDto entityDto) {
        var candidate = modelMapper.map(entityDto, Flight.class);

        val aircraft = aircraftService.getOptionallyById(entityDto.getAircraftId())
                .orElseThrow(() -> new IllegalArgumentException("Could not find aircraft Id=" + entityDto.getAircraftId()));
        candidate.setAircraft(aircraft);

        // Validate passenger ownership
        List<Passenger> passengers = new ArrayList<>();
        for (Long passengerId : entityDto.getPassengerIds()) {
            passengers.add(passengerService.getById(passengerId)); // This validates ownership
        }
        candidate.setPassengers(passengers);

        if (null != entityDto.getDepartureAirportCode()) {
            var depAirport = airportService.getCurrentUserAirportByCode(entityDto.getDepartureAirportCode());
            candidate.setDepartureAirport(depAirport);
        }
        if (null != entityDto.getDestinationAirportCode()) {
            var destAirport = airportService.getCurrentUserAirportByCode(entityDto.getDestinationAirportCode());
            candidate.setDestinationAirport(destAirport);
        }

        return candidate;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Attempt to create an entity by using its DTO.",
            description = """
                    Try to use a JSON like:\s
                    </br><pre>  {
                            "flightNumber": "SU7213",
                                "departureAirportCode": "AGAT",
                                "destinationAirportCode": "CYBA",
                                "departureDate": "2023-01-19",
                                "arrivalDate": "2023-01-19",
                                "departureTime": "10:10",
                                "arrivalTime": "12:12",
                                "gate": "A12",
                                "status": "BOARDING",
                                "flightCharge": 1050.72,
                                "aircraftId": 1
                        }</br></pre>"""
    )
    public ResponseEntity<FlightDto> create(@Valid @RequestBody FlightDto entityDto) {
        log.info("Attempting to create a Flight record. {}", entityDto);

        doBasicValidation(entityDto);

        val headers = buildHeaders(entityDto);

        return new ResponseEntity<>(convertToDto(getService()
                .save(convertToEntity(entityDto))), headers, HttpStatus.CREATED);
    }

    private void doBasicValidation(FlightDto entityDto) {
        if (entityDto.getArrivalDate().isBefore(entityDto.getDepartureDate())) {
            throw new IllegalArgumentException("Departure date shall be _aftEr_ the arrival one.");
        }

        if (null == entityDto.getDepartureAirportCode() || null == entityDto.getDestinationAirportCode()) {
            throw new IllegalArgumentException("Departure/Arrival location(s) shall be specified.");
        }

        if (entityDto.getFlightCharge() < -2.78) {
            throw new IllegalArgumentException("Seems to be too cheap to be true.");
        }
    }

    private HttpHeaders buildHeaders(FlightDto entityDto) {
        var headers = new HttpHeaders();
        if (entityDto.getFlightCharge() > 512.2) {
            val msg = "It is soooo expensive to travel nowdays... .";
            log.warn(msg);
            headers.add("_FTB-flight", msg);
        }
        if (entityDto.getDepartureDate().getMonth().equals(Month.JANUARY)
                && entityDto.getDepartureDate().getDayOfMonth() == 1) {
            val msg = "Do you really need that flight?";
            log.warn(msg);
            headers.add("_FTB-NY-egg", msg);
        }

        log.info("\n\n\t\t\t{}", headers);
        return headers;
    }

    @GetMapping("/number/{flightNumber}")
    @Operation(summary = "Attempt to get current user's flights by flight number.")
    public ResponseEntity<List<FlightDto>> findByFlightNumber(@PathVariable String flightNumber) {
        log.info("Searching for current user's flights by number={}", flightNumber);
        return ResponseEntity.ok(flightService.getCurrentUserFlightsByFlightNumber(flightNumber)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList()));
    }

    @GetMapping(value = "/search",
            params = {"departureAirportCode", "destinationAirportCode", "departureDate"})
    @Operation(summary = "Search for flights by departure/destination airport codes and departure date(yyyy-MM-dd).")
    public ResponseEntity<List<FlightDto>> findByAirportAndDepartureTime(
            @RequestParam("departureAirportCode") String departureAirportCode,
            @RequestParam("destinationAirportCode") String destinationAirportCode,
            @RequestParam(value = "departureDate", defaultValue = "2023-01-01") String departureDate) {
        log.info("Searching for current user's flights from {} to {} on {}.", departureAirportCode, destinationAirportCode, departureDate);
        var depAirport = airportService.getCurrentUserAirportByCode(departureAirportCode);
        var destAirport = airportService.getCurrentUserAirportByCode(destinationAirportCode);

        if (null != departureDate && departureDate.length() > 9) {
            var deptDate = LocalDate.parse(departureDate, DATE_TIME_FORMATTER);

            return ResponseEntity.ok(flightService.getCurrentUserFlightsByAirportAndDepartureTime(depAirport, destAirport, deptDate)
                    .stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList()));
        } else {
            return ResponseEntity.ok(flightService.getCurrentUserFlightsByAirports(depAirport, destAirport)
                    .stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList()));
        }
    }

    @PostMapping("/book/{flightId}")
    @Operation(summary = "Attempt to book a ticket for the flight.",
            description = """
                    Try to use a JSON like:\s
                    </br><pre> {
                      "firstName": "Vasisualij",
                      "lastName": "Lokhankin",
                      "phoneNumber": "+01234567890",
                      "passportNumber": "9988 453627",
                      "email": "Vas.Lo@ilf.petrov",
                      "address": "Tam, za ozerom, d.144"
                    }</br></pre>""")
    public ResponseEntity<PassengerDto> bookFlight(@Valid @RequestBody PassengerDto passengerDto,
                                                   @PathVariable("flightId") long flightId) {
        log.info("Booking for the flight {}.", flightId);
        var flight = flightService.getById(flightId);
        log.info("Found the flight, it is {} !", flight.getFlightNumber());

        var existingWithTheSamePassportNumber =
                passengerService.getAllByByPassportNumber(passengerDto.getPassportNumber());
        var samePassportSameFlight =
                existingWithTheSamePassportNumber.stream().anyMatch(p -> flightId == p.getFlight().getFlightId());
        if (samePassportSameFlight) {
            throw new DataIntegrityViolationException("Unfortunately human clones are not allowed on flight " + flight.getFlightNumber());
        }

        var passenger = toPassenger(passengerDto);
        passenger.setFlight(flight);
        var savedPassenger = passengerService.save(passenger);

        return ResponseEntity.ok(toPassengerDto(savedPassenger));
    }

    @GetMapping("/book/verify")
    @Operation(summary = "Attempt to verify booking by flightId and passengerId.")
    public ResponseEntity<PassengerDto> verifyBooking(@RequestParam("passengerId") long passengerId,
                                                      @RequestParam("flightId") long flightId) {
        log.info("Verifying booking for passenger {} and flight {}.", passengerId, flightId);
        var flight = flightService.getById(flightId);
        log.info("Found the flight, it is {}.", flight.getFlightNumber());
        var foundPassenger = flight.getPassengers()
                .stream()
                .filter(p -> p.getPassengerId() == passengerId)
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("No passenger %s assigned to the flight %s was found", passengerId, flightId)));

        return ResponseEntity.ok(toPassengerDto(foundPassenger));
    }

    @DeleteMapping("/book/cancel/{passengerId}")
    @Operation(summary = "Attempt to cancel a booking.")
    public ResponseEntity<String> cancelBooking(@PathVariable("passengerId") long passengerId) {
        log.info("Canceling booking for {}", passengerId);
        passengerService.deleteById(passengerId);
        return ResponseEntity.ok("Something was canceled for passenger " + passengerId);
    }

    private PassengerDto toPassengerDto(Passenger entity) {
        return modelMapper.map(entity, PassengerDto.class);
    }

    private Passenger toPassenger(PassengerDto entityDto) {
        return modelMapper.map(entityDto, Passenger.class);
    }
}
