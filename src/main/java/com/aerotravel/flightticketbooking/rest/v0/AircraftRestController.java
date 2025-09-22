package com.aerotravel.flightticketbooking.rest.v0;

import com.aerotravel.flightticketbooking.model.Aircraft;
import com.aerotravel.flightticketbooking.model.dto.AircraftDto;
import com.aerotravel.flightticketbooking.services.AircraftService;
import com.aerotravel.flightticketbooking.services.EntityService;
import com.aerotravel.flightticketbooking.services.FlightService;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.HeaderColumnNameMappingStrategyBuilder;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static org.apache.commons.collections.IteratorUtils.toList;

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
    @Operation(summary = "Attempt to get current user's aircraft by model name.")
    @ApiResponse(responseCode = "200", description = "Found aircraft(s).",
            content = {@Content(mediaType = "application/json",
                    schema = @Schema(implementation = AircraftDto.class))})
    public List<AircraftDto> findByModel(@PathVariable String modelName) {
        log.info("Searching for current user's aircrafts by model={}", modelName);
        return aircraftService.getCurrentUserAircraftByModel(modelName)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/manufacturer/{manufacturerName}")
    @Operation(summary = "Attempt to get current user's aircraft by manufacturer name.")
    @ApiResponse(responseCode = "200", description = "Found aircraft(s).",
            content = {@Content(mediaType = "application/json",
                    schema = @Schema(implementation = AircraftDto.class))})
    public List<AircraftDto> findByManufacturer(@PathVariable String manufacturerName) {
        log.info("Searching for current user's aircrafts by manufacturer={}", manufacturerName);
        return aircraftService.getCurrentUserAircraftByManufacturer(manufacturerName)
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
        var map = new LinkedHashMap<String, String>();

        if (null != file) {
            log.info("About to import some data from '{}'", file.getOriginalFilename());
            putFileInfo(file, map);
            val records = parseRecords(file);
            val savedEntities = aircraftService.saveAll(records.stream()
                            .map(this::convertToEntity).collect(Collectors.toList()));
            savedEntities.forEach(e -> map.put(String.valueOf(e.getAircraftId()), e.getModel()));

            map.put("Records processed", String.valueOf(records.size()));
        } else {
            map.put("ERROR", "Nothing to import.");
        }

        map.put("Message", "File upload done");

        return ResponseEntity.ok(map);
    }
    @Operation(summary = "Attempt to import aircraft data from CSV file asynchronously.",
            description =
                    "</br>CSV file content sample:</br>" +
                            "<pre>" +
                            "manufacturer, model, numberOfSeats\n" +
                            "\"E\", \"E-155\", 1\n" +
                            "\"E\", \"E-156\", 2" +
                            "</br> </pre>")
    @PostMapping(value = "/import/async", consumes = "multipart/form-data")
    @Async
    public CompletableFuture<ResponseEntity<String>> handleImportViaFileAsync(
            @RequestPart("file") MultipartFile file) throws IOException, InterruptedException {
        // Handle empty file error
        if (null == file || file.isEmpty()) {
            return CompletableFuture
                    .completedFuture(ResponseEntity.badRequest().body("No file submitted"));
        }
        // File upload process is submitted
        else {
            log.info("About to start Aircraft data import from '{}'", file.getOriginalFilename());
            val records = parseRecords(file);

            for (AircraftDto dto : records) {
                val saved = aircraftService.saveAsync(convertToEntity(dto));
                if (null != saved) {
                    log.info("Saved {}", saved.getAircraftId());
                }
            }

            return CompletableFuture.completedFuture(
                    ResponseEntity.ok("File upload started. Keep calm and wait for delivery."));
        }
    }

    private void putFileInfo(MultipartFile file, Map<String, String> map) {
        map.put("File name", file.getOriginalFilename());
        map.put("File size", String.valueOf(file.getSize()));
        map.put("File content type", file.getContentType());
    }

    private List<AircraftDto> parseRecords(MultipartFile file) throws IOException {
        return new CsvToBeanBuilder<AircraftDto>(new InputStreamReader(file.getInputStream()))
                .withType(AircraftDto.class)
                .build()
                .parse();
    }

    @Operation(summary = "Attempt to export all aircraft records to CSV file.")
    @GetMapping("/export")
    public StreamingResponseBody exportAllAsCsvFile(HttpServletResponse response) throws CsvRequiredFieldEmptyException, CsvDataTypeMismatchException, IOException {
        val allItems = findAll().getBody();

        val writer = new StringWriter();
        var strategy = new HeaderColumnNameMappingStrategyBuilder<AircraftDto>().build();
        strategy.setType(AircraftDto.class);
        val beanToCsv = new StatefulBeanToCsvBuilder<AircraftDto>(writer)
                .withMappingStrategy(strategy)
                .build();
        assert allItems != null;
        beanToCsv.write(toList(allItems.iterator()));

        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.setHeader(
                HttpHeaders.CONTENT_DISPOSITION,
                "attachment;filename=\"" + "all_records_" + LocalDateTime.now() + ".csv\"");

        return outputStream -> {
            outputStream.write(writer.toString().getBytes(StandardCharsets.UTF_8));
        };
    }
}
