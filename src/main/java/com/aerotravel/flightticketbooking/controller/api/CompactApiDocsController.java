package com.aerotravel.flightticketbooking.controller.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
@Hidden
public class CompactApiDocsController {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final YAMLMapper yamlMapper = new YAMLMapper();

    @GetMapping(value = "/v3/api-docs-compact.yaml", produces = "application/x-yaml")
    public ResponseEntity<String> getCompactApiDocsYaml() {
        try {
            JsonNode compactApiDocs = generateCompactApiDocs();
            String yamlContent = yamlMapper.writeValueAsString(compactApiDocs);
            return ResponseEntity.ok()
                    .contentType(MediaType.valueOf("application/x-yaml"))
                    .body(yamlContent);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error generating compact API docs: " + e.getMessage());
        }
    }

    @GetMapping(value = "/v3/api-docs-compact", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getCompactApiDocsJson() {
        try {
            JsonNode compactApiDocs = generateCompactApiDocs();
            String jsonContent = objectMapper.writeValueAsString(compactApiDocs);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(jsonContent);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error generating compact API docs: " + e.getMessage());
        }
    }

    private JsonNode generateCompactApiDocs() throws Exception {
        // Fetch the full API documentation
        RestTemplate restTemplate = new RestTemplate();
        String fullApiDocs = restTemplate.getForObject("http://localhost:8080/v3/api-docs", String.class);

        // Parse the JSON
        JsonNode rootNode = objectMapper.readTree(fullApiDocs);

        // Remove descriptions recursively to create compact version
        removeDescriptions((ObjectNode) rootNode);

        // Update the title to indicate it's compact
        if (rootNode.has("info") && rootNode.get("info").isObject()) {
            ObjectNode infoNode = (ObjectNode) rootNode.get("info");
            infoNode.put("title", infoNode.get("title").asText() + " (Compact)");
            infoNode.put("description", "Minimal API documentation for AI model consumption");
        }

        return rootNode;
    }

    private void removeDescriptions(ObjectNode node) {
        // Remove description field if it exists
        node.remove("description");

        // Recursively process all object properties
        node.fieldNames().forEachRemaining(fieldName -> {
            JsonNode fieldValue = node.get(fieldName);
            if (fieldValue.isObject()) {
                removeDescriptions((ObjectNode) fieldValue);
            } else if (fieldValue.isArray()) {
                for (JsonNode arrayElement : fieldValue) {
                    if (arrayElement.isObject()) {
                        removeDescriptions((ObjectNode) arrayElement);
                    }
                }
            }
        });
    }


    @GetMapping(value = "/database/ftb.sql", produces = "text/plain")
    public ResponseEntity<String> getDatabaseSchema() {
        try {
            // Try to read from file system first (for development)
            Resource resource = new ClassPathResource("../../../database/ftb.sql");
            if (!resource.exists()) {
                // Fallback: read from a copied resource in classpath
                resource = new ClassPathResource("database/ftb.sql");
            }

            if (resource.exists()) {
                String sqlContent = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
                return ResponseEntity.ok()
                        .contentType(MediaType.TEXT_PLAIN)
                        .header("Content-Disposition", "inline; filename=\"ftb.sql\"")
                        .body(sqlContent);
            } else {
                return ResponseEntity.ok()
                        .contentType(MediaType.TEXT_PLAIN)
                        .body("-- FTB Database Schema not found\n-- This is an educational application for QA testing\n-- Database schema should be available in /database/ftb.sql");
            }
        } catch (IOException e) {
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("-- Error reading database schema: " + e.getMessage());
        }
    }
}