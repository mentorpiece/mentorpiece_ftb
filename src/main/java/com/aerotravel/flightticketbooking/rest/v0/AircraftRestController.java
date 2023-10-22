package com.aerotravel.flightticketbooking.rest.v0;

import com.aerotravel.flightticketbooking.model.Aircraft;
import com.aerotravel.flightticketbooking.model.dto.AircraftDto;
import com.aerotravel.flightticketbooking.services.AircraftService;
import com.aerotravel.flightticketbooking.services.EntityService;
import com.aerotravel.flightticketbooking.services.FlightService;
import com.opencsv.bean.CsvToBeanBuilder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v0/aircrafts")
@Tag(name = "Aircraft", description = "Aircraft resource")
@Slf4j
@Validated
public class AircraftRestController extends AbstractRestController<Aircraft, AircraftDto> {
    private final AircraftService aircraftService;
    private final FlightService flightService;

    @Autowired
    public AircraftRestController(AircraftService aircraftService, FlightService flightService) {
        this.aircraftService = aircraftService;
        this.flightService = flightService;
    }

    @Override
    protected EntityService<Aircraft> getService() {
        return aircraftService;
    }

    @Override
    protected Class<Aircraft> getEntityClass() {
        return Aircraft.class;
    }

    @Override
    protected AircraftDto convertToDto(Aircraft entity) {
        return modelMapper.map(entity, AircraftDto.class);
    }

    @Override
    protected Aircraft convertToEntity(AircraftDto entityDto) {
        var candidate = modelMapper.map(entityDto, Aircraft.class);
        candidate.setFlights(flightService.getAllById(entityDto.getFlightIds()));

        return candidate;
    }

    @GetMapping("/model/{modelName}")
    @Operation(summary = "Attempt to get an aircraft by its model name.")
    @ApiResponse(responseCode = "200", description = "Found aircraft(s).",
            content = {@Content(mediaType = "application/json",
                    schema = @Schema(implementation = AircraftDto.class))})
    public List<AircraftDto> findByModel(@PathVariable String modelName) {
        log.info("Searching for aircrafts by model={}", modelName);
        return aircraftService.getByModel(modelName)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/manufacturer/{manufacturerName}")
    @Operation(summary = "Attempt to get an aircraft by its manufacturer name.")
    @ApiResponse(responseCode = "200", description = "Found aircraft(s).",
            content = {@Content(mediaType = "application/json",
                    schema = @Schema(implementation = AircraftDto.class))})
    public List<AircraftDto> findByManufacturer(@PathVariable String manufacturerName) {
        log.info("Searching for aircrafts by manufacturer={}", manufacturerName);
        return aircraftService.getByManufacturer(manufacturerName)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Operation(summary = "Attempt to import aircraft data from CSV file.",
            description =
                    "</br>CSV file content sample:</br>" +
                            "<pre>" +
                            "manufacturer, model, numberOfSeats\n" +
                            "\"Delf\", \"D-11\", 2\n" +
                            "\"Delf\", \"D-12\", 8" +
                            "</br> </pre>")
    @PostMapping(value = "/import", consumes = "multipart/form-data")
    public ResponseEntity<Map<String, String>> handleImportViaFile(
            @RequestPart("file") MultipartFile file) throws IOException {
        var map = new TreeMap<String, String>();

        List<AircraftDto> beans = new CsvToBeanBuilder(new InputStreamReader(file.getInputStream()))
                .withType(AircraftDto.class)
                .build()
                .parse();

        for (AircraftDto dto : beans) {
            // TODO: Need a saveAll() + save async
            val createResult = create(dto);
            if (HttpStatus.CREATED.value() == createResult.getStatusCodeValue()
                    && null != createResult.getBody()) {
                map.put(String.valueOf(createResult.getBody().getAircraftId()), String.valueOf(HttpStatus.CREATED));
            }
        }

        map.put("File name", file.getOriginalFilename());
        map.put("File size", String.valueOf(file.getSize()));
        map.put("File content type", file.getContentType());
        map.put("Message", "File upload done");

        return ResponseEntity.ok(map);
    }
}
