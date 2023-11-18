package com.aerotravel.flightticketbooking.controller;

import com.aerotravel.flightticketbooking.model.Airport;
import com.aerotravel.flightticketbooking.model.Flight;
import com.aerotravel.flightticketbooking.model.Passenger;
import com.aerotravel.flightticketbooking.services.AircraftService;
import com.aerotravel.flightticketbooking.services.AirportService;
import com.aerotravel.flightticketbooking.services.FlightService;
import com.aerotravel.flightticketbooking.services.PassengerService;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import javax.websocket.server.PathParam;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import static java.lang.String.format;

// Migrated from the MainController.
@Slf4j
@Controller
public class FlightsGuiController {

    private static final String ATTR_CURRENT_PAGE = "currentPage";
    private static final String ATTR_FLIGHTS = "flights";
    private static final String ATTR_FLIGHT = "flight";
    private static final String ATTR_MESSAGE = "message";
    private static final String ATTR_AIRPORTS = "airports";
    private static final String ATTR_NOT_FOUND = "notFound";
    private static final String ATTR_AIRCRAFTS = "aircrafts";
    private static final String PAGE_FLIGHTS = "flights";
    private static final String PAGE_NEW_FLIGHT = "newFlight";
    private static final String PAGE_SEARCH_FLIGHT = "searchFlight";
    private static final String PAGE_BOOK_FLIGHT = "bookFlight";
    private static final String DATE_PATTERN = "yyyy-MM-dd";
    private static final List<String> STATUSES = List.of(
            "PLANNED", "DETAINED", "CANCELED", "WAIT FOR CHECK-IN", "CHECK-IN OPEN", "CHECK-IN CLOSED", "GO TO GATE", "GATE OPEN", "BOARDING", "BOARDING COMPLETED", "GATE CLOSED", "DEPARTED", "DEPARTED LATE", "EN ROUTE", "ON-SCHEDULE", "EXPECTED", "ARRIVED", "ARRIVED LATE", "LANDED", "BAGGAGE CLAIM", "BAGGAGE CLAIM DELAYED", "BAGS DELIVERED", "UNKNOWN");
    @Autowired
    AirportService airportService;
    @Autowired
    AircraftService aircraftService;
    @Autowired
    FlightService flightService;
    @Autowired
    PassengerService passengerService;

    @GetMapping("/flight/new")
    public String showNewFlightPage(Model model) {
        log.info("Opening the New flight page");
        return newFlightPage(model, new Flight());
    }

    private String newFlightPage(Model model, Flight flight) {
        var statuses = new ArrayList<>(STATUSES);
        Collections.shuffle(statuses);
        model.addAttribute(ATTR_FLIGHT, null != flight ? flight : new Flight());
        model.addAttribute(ATTR_AIRCRAFTS, aircraftService.getAll().stream().sorted().toList());
        model.addAttribute(ATTR_AIRPORTS, airportService.getAll().stream().sorted().toList());
        model.addAttribute("statuses", statuses);
        return PAGE_NEW_FLIGHT;
    }

    @PostMapping("/flight/new")
    public String saveFlight(@Valid @ModelAttribute(ATTR_FLIGHT) Flight flight, BindingResult bindingResult,
                             @RequestParam("departureAirport") long departureAirport,
                             @RequestParam("destinationAirport") long destinationAirport,
                             @RequestParam("aircraft") long aircraftId,
                             @RequestParam("arrivalTime") String arrivalTime,
                             @RequestParam("departureTime") String departureTime,
                             @RequestParam("status") String status,
                             Model model) {

        log.info("About to save flight from {}/{} to {}/{}",
                departureAirport, departureTime, destinationAirport, arrivalTime);
        // This is intentional - a built-in issue.
        flight.setDepartureTime(departureTime);
        flight.setArrivalTime(arrivalTime);
        flight.setStatus(status);

        if (bindingResult.hasErrors()) {
            model.addAttribute("errors", bindingResult.getAllErrors());
            log.info("Failed to save the flight because of the binding errors:\n{}\n{}", flight, bindingResult.getAllErrors());
            return newFlightPage(model, flight);
        }
        if (departureAirport == destinationAirport) {
            model.addAttribute("sameAirportError", "Departure and destination airport can't be same");
            return newFlightPage(model, flight);
        }

        flight.setAircraft(aircraftService.getById(aircraftId));
        flight.setDepartureAirport(airportService.getById(departureAirport));
        flight.setDestinationAirport(airportService.getById(destinationAirport));

        log.info("About to save the flight:\n{}", flight);
        execute(() -> {
                    flightService.save(flight);
                    return null;
                }, model,
                "Something went wrong upon saving a flight. Data:\n" + flight.describe());

        model.addAttribute(ATTR_FLIGHTS, flightService.getAllPaged(0));
        model.addAttribute(ATTR_CURRENT_PAGE, 0);
        return PAGE_FLIGHTS;
    }

    @GetMapping("/flight/delete")
    public String deleteFlight(@PathParam("flightId") long flightId, Model model) {
        log.info("About to delete the flight, id={}", flightId);
        execute(() -> {
                    flightService.deleteById(flightId);
                    return null;
                }, model,
                "SOmething went wrong upon deleting flight " + flightId);
        model.addAttribute(ATTR_FLIGHTS, flightService.getAllPaged(0));
        model.addAttribute(ATTR_CURRENT_PAGE, 0);
        return PAGE_FLIGHTS;
    }

    @GetMapping("/flights")
    public String showFlightsList(@RequestParam(defaultValue = "0") int pageNo, Model model) {
        log.info("About to show the list of flights");
        model.addAttribute(ATTR_FLIGHTS, flightService.getAllPaged(pageNo));
        model.addAttribute(ATTR_CURRENT_PAGE, pageNo);
        return PAGE_FLIGHTS;
    }

    @GetMapping("/flights_list")
    public String showSimpleFlightsList(@RequestParam(defaultValue = "0") int pageNo, Model model) {
        log.info("About to show the simple list of flights");
        model.addAttribute(ATTR_FLIGHTS, flightService.getAllPaged(pageNo));
        model.addAttribute(ATTR_CURRENT_PAGE, pageNo);
        return "flights_list";
    }

    @GetMapping("/flight/search")
    public String showSearchFlightPage(Model model) {
        log.info("About to show the search flights page");
        model.addAttribute(ATTR_AIRPORTS, airportService.getAll());
        model.addAttribute(ATTR_FLIGHTS, null);
        return PAGE_SEARCH_FLIGHT;
    }

    @PostMapping("/flight/search")
    public String searchFlight(@RequestParam("departureAirport") long departureAirport,
                               @RequestParam("destinationAirport") long destinationAirport,
                               @RequestParam("departureTime") String departureTime,
                               Model model) {
        log.info("About to search for a flight from {} to {} on {}", departureAirport, destinationAirport, departureTime);
        return doSearch(departureAirport, destinationAirport, departureTime, model, PAGE_SEARCH_FLIGHT);
    }

    @GetMapping("/flight/book")
    public String showBookFlightPage(Model model) {
        log.info("About to show the flight page");
        model.addAttribute(ATTR_AIRPORTS, airportService.getAll());
        return PAGE_BOOK_FLIGHT;
    }

    @PostMapping("/flight/book")
    public String searchFlightToBook(@RequestParam("departureAirport") long departureAirport,
                                     @RequestParam("destinationAirport") long destinationAirport,
                                     @RequestParam("departureTime") String departureTime,
                                     Model model) {
        log.info("About to search for flight to book from {} to {} on {}", departureAirport, destinationAirport, departureTime);
        return doSearch(departureAirport, destinationAirport, departureTime, model, PAGE_BOOK_FLIGHT);
    }

    private String doSearch(long departureAirport, long destinationAirport, String departureTime,
                            Model model, String pageToReturnTo) {
        if (departureAirport == destinationAirport) {
            val msg = "Departure and destination airport cannot be the same!";
            log.warn(msg);
            model.addAttribute(ATTR_NOT_FOUND, msg);
            model.addAttribute(ATTR_AIRPORTS, airportService.getAll());
            return pageToReturnTo;
        }

        val dtf = DateTimeFormatter.ofPattern(DATE_PATTERN);
        val deptTime = LocalDate.parse(departureTime, dtf);
        val depAirport = airportService.getById(departureAirport);
        val destAirport = airportService.getById(destinationAirport);

        populateFlightsIfAny(departureAirport, destinationAirport, departureTime, model, deptTime, depAirport, destAirport);

        model.addAttribute(ATTR_AIRPORTS, airportService.getAll());
        return pageToReturnTo;
    }

    private void populateFlightsIfAny(long departureAirport, long destinationAirport, String departureTime, Model model, LocalDate deptTime, Airport depAirport, Airport destAirport) {
        val flights = flightService.getAllByAirportAndDepartureTime(depAirport, destAirport, deptTime);
        if (flights.isEmpty()) {
            val msg = format("No flights were found from %s to %s on %s",
                    departureAirport, destinationAirport, departureTime);
            log.info(msg);
            model.addAttribute(ATTR_NOT_FOUND, msg);
        } else {
            model.addAttribute(ATTR_FLIGHTS, flights);
        }
    }

    @GetMapping("/flight/book/new")
    public String showCustomerInfoPage(@RequestParam long flightId, Model model) {
        log.info("About to show the new passenger page");
        model.addAttribute("flightId", flightId);
        model.addAttribute("passenger", new Passenger());
        return "newPassenger";
    }

    @PostMapping("/flight/book/new")
    public String bookFlight(@Valid @ModelAttribute("passenger") Passenger passenger,
                             BindingResult bindingResult,
                             @RequestParam("flightId") long flightId, Model model) {
        log.info("About to book a ticket for flight {}", flightId);
        Flight flight = flightService.getById(flightId);
        Passenger passenger1 = passenger;
        passenger1.setFlight(flight);
        passengerService.save(passenger1);
        model.addAttribute("passenger", passenger1);
        return "confirmationPage";
    }

    @GetMapping("/flight/book/verify")
    public String showVerifyBookingPage() {
        log.info("Navigating to the page to verify bookings");
        return "verifyBooking";
    }

    @PostMapping("/flight/book/verify")
    public String showVerifyBookingPageResult(@RequestParam("flightId") long flightId,
                                              @RequestParam("passengerId") long passengerId,
                                              Model model) {
        log.info("About to verify booking for flight={} and passenger={}", flightId, passengerId);
        var flightOptional = flightService.getOptionallyById(flightId);
        if (flightOptional.isPresent()) {
            var flight = flightOptional.get();
            model.addAttribute(ATTR_FLIGHT, flight);
            var passengers = flight.getPassengers();
            Passenger passenger = null;
            for (Passenger p : passengers) {
                if (p.getPassengerId() == passengerId) {
                    passenger = passengerService.getById(passengerId);
                    model.addAttribute("passenger", passenger);
                }
            }
            if (passenger == null) {
                log.info("No passengers were found for flight={} with Id={}", flightId, passengerId);
                model.addAttribute(ATTR_NOT_FOUND, "Passenger " + passengerId + " was Not Found");
            }
        } else {
            log.info("No bookings were found for flight={} with Id={}", flightId, passengerId);
            model.addAttribute(ATTR_NOT_FOUND, "Flight " + flightId + " was Not Found");
        }
        return "verifyBooking";

    }

    @PostMapping("/flight/book/cancel")
    public String cancelTicket(@RequestParam("passengerId") long passengerId, Model model) {
        log.info("About to cancel a booking for passenger {}", passengerId);
        passengerService.deleteById(passengerId);
        model.addAttribute(ATTR_FLIGHTS, flightService.getAllPaged(0));
        model.addAttribute(ATTR_CURRENT_PAGE, 0);
        return ATTR_FLIGHTS;
    }

    @GetMapping("passengers")
    public String showPassengerList(@RequestParam long flightId, Model model) {
        log.info("About to show the list of passengers for flights {}", flightId);
        List<Passenger> passengers = flightService.getById(flightId).getPassengers();
        model.addAttribute("passengers", passengers);
        model.addAttribute(ATTR_FLIGHT, flightService.getById(flightId));
        return "passengers";
    }

    private void execute(Supplier<Object> operation, Model model, String messageOnFail) {
        try {
            operation.get();
        } catch (Exception e) {
            log.warn(messageOnFail, e);
            val msg = format("%s\n%s\n%s\n%s", messageOnFail, e.getMessage(), e.getLocalizedMessage(), e.fillInStackTrace());
            model.addAttribute(ATTR_MESSAGE, msg);
        }
    }
}
