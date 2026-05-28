package com.ycyw.chat.chat_poc_backend.controller;

import com.ycyw.chat.chat_poc_backend.domain.Customer;
import com.ycyw.chat.chat_poc_backend.dto.AdvisorAuthRequest;
import com.ycyw.chat.chat_poc_backend.dto.AuthTokenResponse;
import com.ycyw.chat.chat_poc_backend.dto.ClientAuthRequest;
import com.ycyw.chat.chat_poc_backend.repository.CustomerRepository;
import com.ycyw.chat.chat_poc_backend.security.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentification", description = "JWT CLIENT / ADVISOR")
public class AuthController {

    private static final Map<String, AdvisorCredential> ADVISORS = Map.of(
            "agent-001", new AdvisorCredential("Sophie Martin", "ycyw2024"),
            "agent-002", new AdvisorCredential("Thomas Leclerc", "ycyw2024")
    );

    private final JwtService jwtService;
    private final CustomerRepository customerRepository;

    public AuthController(JwtService jwtService, CustomerRepository customerRepository) {
        this.jwtService = jwtService;
        this.customerRepository = customerRepository;
    }

    @PostMapping("/client")
    @Operation(summary = "Token JWT pour un client (email)")
    public AuthTokenResponse clientToken(@Valid @RequestBody ClientAuthRequest request) {
        Customer customer = customerRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Client inconnu"));

        AuthTokenResponse response = new AuthTokenResponse();
        response.setAccessToken(jwtService.createClientToken(customer.getId(), customer.getEmail()));
        response.setRole("CLIENT");
        response.setCustomerId(customer.getId());
        return response;
    }

    @PostMapping("/advisor")
    @Operation(summary = "Token JWT pour un conseiller")
    public AuthTokenResponse advisorToken(@Valid @RequestBody AdvisorAuthRequest request) {
        AdvisorCredential cred = ADVISORS.get(request.getAgentId());
        if (cred == null || !cred.password().equals(request.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Identifiants invalides");
        }

        AuthTokenResponse response = new AuthTokenResponse();
        response.setAccessToken(jwtService.createAdvisorToken(request.getAgentId(), cred.name()));
        response.setRole("ADVISOR");
        response.setAgentId(request.getAgentId());
        return response;
    }

    private record AdvisorCredential(String name, String password) {}
}
