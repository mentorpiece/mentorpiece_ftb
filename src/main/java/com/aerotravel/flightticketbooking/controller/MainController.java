package com.aerotravel.flightticketbooking.controller;

import com.aerotravel.flightticketbooking.model.Aircraft;
import com.aerotravel.flightticketbooking.model.Airport;
import com.aerotravel.flightticketbooking.rest.v0.AircraftRestController;
import com.aerotravel.flightticketbooking.services.AircraftService;
import com.aerotravel.flightticketbooking.services.AirportService;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import jakarta.websocket.server.PathParam;
import java.io.IOException;

import static java.util.Objects.requireNonNull;

@Slf4j
@Controller
public class MainController {

    public static final String ATTR_CURRENT_PAGE = "currentPage";
    public static final String ATTR_MESSAGE = "message";
    @Autowired
    AirportService airportService;
    @Autowired
    AircraftService aircraftService;
    @Autowired
    AircraftRestController aircraftRestController;

    @GetMapping("/")
    public String showHomePage() {
        return "index";
    }

    @GetMapping("/airport/new")
    @PreAuthorize("hasRole('ADMIN')")
    public String showAddAirportPage(Model model) {
        log.info("About to open the New Airport page");
        model.addAttribute("airport", new Airport());
        return "newAirport";
    }

    @GetMapping("/airport/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String showEditAirport(@PathParam("airportId") long airportId, Model model) {
        log.info("Opening airport={} to edit", airportId);
        var record = airportService.getById(airportId);

        model.addAttribute("airport", record);
        return "editAirport";
    }

    @PostMapping("/airport/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String editAirport(@PathParam("airportId") long airportId, @Valid Airport airport,
                               BindingResult result, Model model) {
        if (result.hasErrors()) {
            airport.setAirportId(airportId);
            log.info("There were errors upon  editing Airport entity");
            return "editAirport";
        }

        log.info("About to update airport={}", airport.getAirportName());
        airportService.save(airport);
        return "redirect:/airports";
    }

    @PostMapping("/airport/new")
    @PreAuthorize("hasRole('ADMIN')")
    public String saveAirport(@Valid @ModelAttribute("airport") Airport airport, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("errors", bindingResult.getAllErrors());
            model.addAttribute("airport", new Airport());
            log.info("There were errors upon saving Airport entity");

            return "newAirport";
        }
        log.info("About to save airport={}", airport.getAirportName());
        airportService.save(airport);
        model.addAttribute("airports", airportService.getAllPaged(0));
        model.addAttribute(ATTR_CURRENT_PAGE, 0);
        return "airports";
    }

    @GetMapping("/airport/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteAirport(@PathParam("airportId") long airportId, Model model) {
        log.info("About to delete Airport Id={}", airportId);
        airportService.deleteById(airportId);
        model.addAttribute("airports", airportService.getAllPaged(0));
        model.addAttribute(ATTR_CURRENT_PAGE, 0);
        return "airports";
    }

    @GetMapping("/airports")
    public String showAirportsList(@RequestParam(defaultValue = "0") int pageNo, Model model) {
        log.info("About to show airports");
        model.addAttribute("airports", airportService.getAllPaged(pageNo));
        model.addAttribute(ATTR_CURRENT_PAGE, pageNo);
        return "airports";
    }

    @GetMapping("/aircraft/new")
    @PreAuthorize("hasRole('ADMIN')")
    public String showAddAircraftPage(Model model) {
        log.info("About to show aircrafts");
        model.addAttribute("aircraft", new Aircraft());
        return "newAircraft";
    }

    @PostMapping("/aircraft/new")
    @PreAuthorize("hasRole('ADMIN')")
    public String saveAircraft(@Valid @ModelAttribute("aircraft") Aircraft aircraft, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("errors", bindingResult.getAllErrors());
            model.addAttribute("aircraft", new Aircraft());
            log.info("There were errors upon saving Aircraft entity:\n{}", aircraft);
            return "newAircraft";
        }

        log.info("About to save aircraft={}", aircraft.getModel());
        aircraftService.save(aircraft);
        model.addAttribute("aircrafts", aircraftService.getAllPaged(0));
        model.addAttribute(ATTR_CURRENT_PAGE, 0);
        return "aircrafts";
    }

    @GetMapping("/aircraft/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String showEditAircraft(@PathParam("aircraftId") long aircraftId, Model model) {
        log.info("About to edit aircraft entity, id={}", aircraftId);
        var record = aircraftService.getById(aircraftId);

        model.addAttribute("aircraft", record);
        return "editAircraft";
    }

    @PostMapping("/aircraft/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String editAircraft(@PathParam("aircraftId") long aircraftId, @Valid Aircraft aircraft,
                             BindingResult result, Model model) {
        if (result.hasErrors()) {
            aircraft.setAircraftId(aircraftId);
            log.info("Failed to edit the aircraft, id={}", aircraftId);
            return "editAircraft";
        }

        log.info("About to update aircraft={}", aircraft.getModel());
        aircraftService.save(aircraft);
        return "redirect:/aircrafts";
    }

    @GetMapping("/aircraft/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteAircraft(@PathParam("aircraftId") long aircraftId, Model model) {
        log.info("About to delete aircraft, id=={}", aircraftId);
        aircraftService.deleteById(aircraftId);
        return showAircraftsList(0, model); //TODO(L.E.): fix this one day.
    }

    @GetMapping("/aircrafts")
    public String showAircraftsList(@RequestParam(defaultValue = "0") int pageNo, Model model) {
        log.info("About to show aircraft list");
        model.addAttribute("aircrafts", aircraftService.getAllPaged(pageNo));
        model.addAttribute(ATTR_CURRENT_PAGE, pageNo);
        return "aircrafts";
    }

    @PostMapping(value = "/aircrafts/upload")
    public String importAircrafts(Model model, @RequestParam(value = "file", required = false) MultipartFile data) throws IOException {
        log.info("About to import aircraft data from CSV file");
        val response = aircraftRestController.handleImportViaFile(data);

        if (null != response && response.hasBody()) {
            model.addAttribute("messages", requireNonNull(response.getBody()).entrySet());
        }

        return showAircraftsList(0, model);
    }


    @GetMapping("fancy")
    public String showLoginPage1() {
        log.info("Ola-ola-ola-ola, somebody is going to login somewhere!");
        return "index";
    }
}
