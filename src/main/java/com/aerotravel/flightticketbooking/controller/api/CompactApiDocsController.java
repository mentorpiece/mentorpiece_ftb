package com.aerotravel.flightticketbooking.controller.api;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
@Hidden
public class CompactApiDocsController {

    @GetMapping(value = "/v3/api-docs-compact.yaml", produces = "application/x-yaml")
    public ResponseEntity<String> getCompactApiDocsYaml() {
        String compactApiDocs = generateCompactApiDocs();
        return ResponseEntity.ok()
                .contentType(MediaType.valueOf("application/x-yaml"))
                .body(compactApiDocs);
    }

    @GetMapping(value = "/v3/api-docs-compact", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getCompactApiDocsJson() {
        String compactApiDocs = generateCompactApiDocsJson();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(compactApiDocs);
    }

    private String generateCompactApiDocs() {
        return """
                openapi: 3.0.1
                info:
                  title: Flight Ticket Booking API (Compact)
                  version: "1.0"
                  description: Minimal API documentation for AI model consumption
                servers:
                  - url: http://localhost:8080
                    description: Local development server
                paths:
                  # Authentication
                  /api/auth/login:
                    post:
                      tags: [Auth]
                      summary: User login
                      requestBody:
                        content:
                          application/json:
                            schema:
                              type: object
                              properties:
                                username: { type: string }
                                password: { type: string }
                      responses:
                        '200': { description: Login successful }

                  /api/auth/register:
                    post:
                      tags: [Auth]
                      summary: User registration
                      requestBody:
                        content:
                          application/json:
                            schema:
                              type: object
                              properties:
                                username: { type: string }
                                password: { type: string }
                                email: { type: string }
                                firstname: { type: string }
                                lastname: { type: string }
                      responses:
                        '201': { description: Registration successful }

                  # Flights
                  /api/v0/flights:
                    get:
                      tags: [Flights]
                      summary: Get all flights
                      responses:
                        '200': { description: List of flights }
                    post:
                      tags: [Flights]
                      summary: Create new flight
                      requestBody:
                        content:
                          application/json:
                            schema:
                              type: object
                              properties:
                                flightNumber: { type: string }
                                departureAirportCode: { type: string }
                                destinationAirportCode: { type: string }
                                departureDate: { type: string, format: date }
                                arrivalDate: { type: string, format: date }
                                departureTime: { type: string }
                                arrivalTime: { type: string }
                                gate: { type: string }
                                status: { type: string }
                                flightCharge: { type: number }
                                aircraftId: { type: integer }
                      responses:
                        '201': { description: Flight created }

                  /api/v0/flights/{id}:
                    get:
                      tags: [Flights]
                      summary: Get flight by ID
                      parameters:
                        - name: id
                          in: path
                          required: true
                          schema: { type: integer }
                      responses:
                        '200': { description: Flight details }
                    put:
                      tags: [Flights]
                      summary: Update flight
                      parameters:
                        - name: id
                          in: path
                          required: true
                          schema: { type: integer }
                      responses:
                        '200': { description: Flight updated }
                    delete:
                      tags: [Flights]
                      summary: Delete flight
                      parameters:
                        - name: id
                          in: path
                          required: true
                          schema: { type: integer }
                      responses:
                        '200': { description: Flight deleted }

                  /api/v0/flights/search:
                    get:
                      tags: [Flights]
                      summary: Search flights
                      parameters:
                        - name: departureAirportCode
                          in: query
                          required: true
                          schema: { type: string }
                        - name: destinationAirportCode
                          in: query
                          required: true
                          schema: { type: string }
                        - name: departureDate
                          in: query
                          schema: { type: string, format: date }
                      responses:
                        '200': { description: Search results }

                  /api/v0/flights/book/{flightId}:
                    post:
                      tags: [Booking]
                      summary: Book flight ticket
                      parameters:
                        - name: flightId
                          in: path
                          required: true
                          schema: { type: integer }
                      requestBody:
                        content:
                          application/json:
                            schema:
                              type: object
                              properties:
                                firstName: { type: string }
                                lastName: { type: string }
                                phoneNumber: { type: string }
                                passportNumber: { type: string }
                                email: { type: string }
                                address: { type: string }
                      responses:
                        '200': { description: Booking successful }

                  # Aircraft
                  /api/v0/aircrafts:
                    get:
                      tags: [Aircraft]
                      summary: Get all aircraft
                      responses:
                        '200': { description: List of aircraft }
                    post:
                      tags: [Aircraft]
                      summary: Create aircraft
                      requestBody:
                        content:
                          application/json:
                            schema:
                              type: object
                              properties:
                                manufacturer: { type: string }
                                model: { type: string }
                                numberOfSeats: { type: integer }
                      responses:
                        '201': { description: Aircraft created }

                  /api/v0/aircrafts/{id}:
                    get:
                      tags: [Aircraft]
                      summary: Get aircraft by ID
                      parameters:
                        - name: id
                          in: path
                          required: true
                          schema: { type: integer }
                      responses:
                        '200': { description: Aircraft details }

                  # Airports
                  /api/v0/airports:
                    get:
                      tags: [Airports]
                      summary: Get all airports
                      responses:
                        '200': { description: List of airports }
                    post:
                      tags: [Airports]
                      summary: Create airport
                      requestBody:
                        content:
                          application/json:
                            schema:
                              type: object
                              properties:
                                airportCode: { type: string }
                                airportName: { type: string }
                                city: { type: string }
                                state: { type: string }
                                country: { type: string }
                      responses:
                        '201': { description: Airport created }

                  /api/v0/airports/{id}:
                    get:
                      tags: [Airports]
                      summary: Get airport by ID
                      parameters:
                        - name: id
                          in: path
                          required: true
                          schema: { type: integer }
                      responses:
                        '200': { description: Airport details }

                  # Passengers
                  /api/v0/passengers:
                    get:
                      tags: [Passengers]
                      summary: Get all passengers
                      responses:
                        '200': { description: List of passengers }

                  # Users
                  /api/v0/users:
                    get:
                      tags: [Users]
                      summary: Get all users
                      responses:
                        '200': { description: List of users }

                  # Version
                  /api/version:
                    get:
                      tags: [System]
                      summary: Get API version
                      responses:
                        '200': { description: Version information }

                components:
                  securitySchemes:
                    bearerAuth:
                      type: http
                      scheme: bearer
                      bearerFormat: JWT
                      description: JWT Bearer token authentication
                    basicAuth:
                      type: http
                      scheme: basic
                      description: Basic authentication using username and password
                    sessionCookie:
                      type: apiKey
                      name: sessionCookie
                      in: cookie
                      description: Session cookie authentication (login via /login form first)

                security:
                  - bearerAuth: []
                  - basicAuth: []
                  - sessionCookie: []
                """;
    }

    private String generateCompactApiDocsJson() {
        return """
                {
                  "openapi": "3.0.1",
                  "info": {
                    "title": "Flight Ticket Booking API (Compact)",
                    "version": "1.0",
                    "description": "Minimal API documentation for AI model consumption"
                  },
                  "servers": [
                    {
                      "url": "http://localhost:8080",
                      "description": "Local development server"
                    }
                  ],
                  "paths": {
                    "/api/auth/login": {
                      "post": {
                        "tags": ["Auth"],
                        "summary": "User login",
                        "requestBody": {
                          "content": {
                            "application/json": {
                              "schema": {
                                "type": "object",
                                "properties": {
                                  "username": { "type": "string" },
                                  "password": { "type": "string" }
                                }
                              }
                            }
                          }
                        },
                        "responses": {
                          "200": { "description": "Login successful" }
                        }
                      }
                    },
                    "/api/v0/flights": {
                      "get": {
                        "tags": ["Flights"],
                        "summary": "Get all flights",
                        "responses": {
                          "200": { "description": "List of flights" }
                        }
                      },
                      "post": {
                        "tags": ["Flights"],
                        "summary": "Create new flight",
                        "responses": {
                          "201": { "description": "Flight created" }
                        }
                      }
                    },
                    "/api/v0/flights/search": {
                      "get": {
                        "tags": ["Flights"],
                        "summary": "Search flights",
                        "parameters": [
                          {
                            "name": "departureAirportCode",
                            "in": "query",
                            "required": true,
                            "schema": { "type": "string" }
                          },
                          {
                            "name": "destinationAirportCode",
                            "in": "query",
                            "required": true,
                            "schema": { "type": "string" }
                          }
                        ],
                        "responses": {
                          "200": { "description": "Search results" }
                        }
                      }
                    },
                    "/api/v0/flights/book/{flightId}": {
                      "post": {
                        "tags": ["Booking"],
                        "summary": "Book flight ticket",
                        "parameters": [
                          {
                            "name": "flightId",
                            "in": "path",
                            "required": true,
                            "schema": { "type": "integer" }
                          }
                        ],
                        "responses": {
                          "200": { "description": "Booking successful" }
                        }
                      }
                    }
                  },
                  "components": {
                    "securitySchemes": {
                      "bearerAuth": {
                        "type": "http",
                        "scheme": "bearer",
                        "bearerFormat": "JWT",
                        "description": "JWT Bearer token authentication"
                      },
                      "basicAuth": {
                        "type": "http",
                        "scheme": "basic",
                        "description": "Basic authentication using username and password"
                      },
                      "sessionCookie": {
                        "type": "apiKey",
                        "name": "sessionCookie",
                        "in": "cookie",
                        "description": "Session cookie authentication (login via /login form first)"
                      }
                    }
                  },
                  "security": [
                    { "bearerAuth": [] },
                    { "basicAuth": [] },
                    { "sessionCookie": [] }
                  ]
                }
                """;
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