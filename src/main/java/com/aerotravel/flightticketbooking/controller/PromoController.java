package com.aerotravel.flightticketbooking.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.HashMap;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/promo")
public class PromoController {

    @GetMapping("/ping")
    public ResponseEntity<Map<String, Object>> ping() {
        return ResponseEntity.ok(Map.of(
            "status", "ok",
            "message", "pong"
        ));
    }

    @GetMapping("/slow")
    public ResponseEntity<Map<String, Object>> slow() throws InterruptedException {
        // Sleep for 5 seconds like the Python version
        TimeUnit.SECONDS.sleep(5);
        
        // Build response matching Python's jsonify(ok=True, kind='slow', ts=time.time())
        Map<String, Object> response = Map.of(
            "ok", true,
            "kind", "slow",
            "ts", System.currentTimeMillis() / 1000.0
        );

        // Add Cache-Control header matching Python version
        return ResponseEntity.ok()
            .header("Cache-Control", "no-cache, no-store, must-revalidate")
            .body(response);
    }

    @GetMapping("/redirect")
    public ResponseEntity<Void> redirect() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", "/api/promo/ping");
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    @GetMapping("/set_cookie")
    public ResponseEntity<Map<String, Object>> setCookie() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Set-Cookie", "server_cookie=srv123; Path=/; HttpOnly");
        
        return ResponseEntity.ok()
            .headers(headers)
            .body(Map.of(
                "ok", true,
                "cookie_set", true
            ));
    }

    @PostMapping("/echo")
    public ResponseEntity<Map<String, Object>> echo(@RequestBody(required = false) String body, HttpServletRequest request) {
        return ResponseEntity.ok(Map.of(
            "ok", true,
            "method", request.getMethod(),
            "headers", Collections.list(request.getHeaderNames()).stream()
                .collect(Collectors.toMap(
                    headerName -> headerName,
                    request::getHeader
                )),
            "body", body != null ? body : "",
            "magic_number", body != null && body.contains("action=modified") ? "938570103" : null
        ));
    }

    @PostMapping("/set_special_cookie")
    public ResponseEntity<Map<String, Object>> setSpecialCookie(@RequestBody(required = false) String body, HttpServletRequest request) {
        HttpHeaders headers = new HttpHeaders();
        if (body != null && body.contains("special_cookie_activated=true")) {
            headers.add("Set-Cookie", "server_special_cookie=029381721; Path=/; HttpOnly");

            return ResponseEntity.ok()
                .headers(headers)
                .body(Map.of(
                    "ok", true,
                    "special_cookie_activated", true
                ));
        } else {
            return ResponseEntity.ok()
                .body(Map.of(
                    "ok", true,
                    "special_cookie_activated", false
                ));
        }
    }

    @GetMapping("/static/{filename:.+}")
    public ResponseEntity<?> serveStaticFiles(@PathVariable String filename) {
        // Return 404
        return ResponseEntity.notFound().build();
    }

    @PostMapping(value = "/submit", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> submit(@RequestBody(required = false) Map<String, String> data) {
        // Handle null body case
        if (data == null) {
            data = Collections.emptyMap();
        }

        // Extract and clean input fields
        String name = (data.get("name") != null ? data.get("name").trim() : "");
        String email = (data.get("email") != null ? data.get("email").trim() : "");
        
        // Validate inputs
        Map<String, String> errors = new HashMap<>();
        if (name.isEmpty()) {
            errors.put("name", "Name is required");
        }
        if (email.isEmpty() || !email.contains("@")) {
            errors.put("email", "Please provide a valid email");
        }
        
        // Return errors if validation failed
        if (!errors.isEmpty()) {
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "ok", false,
                    "errors", errors
                ));
        }
        
        // Return success response
        return ResponseEntity.ok(Map.of(
            "ok", true,
            "echo", Map.of(
                "name", name,
                "email", email
            )
        ));
    }
}