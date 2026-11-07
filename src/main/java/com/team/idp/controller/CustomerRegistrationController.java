
package com.team.idp.controller;

import com.team.idp.dto.CustomerRegisterRequest;
import com.team.idp.entity.UserAccount;
import com.team.idp.service.CustomerRegistrationService;

import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class CustomerRegistrationController {

    private final CustomerRegistrationService registrationService;

    public CustomerRegistrationController(
            CustomerRegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(
            @RequestBody CustomerRegisterRequest request) {

        UserAccount account = registrationService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                Map.of(
                        "message", "Account created successfully! Please log in.",
                        "username", account.getUsername()
                )
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleInvalidInput(
            IllegalArgumentException exception) {

        return ResponseEntity.badRequest()
                .body(Map.of("message", exception.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleRegistrationUnavailable(
            IllegalStateException exception) {

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("message", exception.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDuplicateAccount() {

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("message", "Username or email is already registered"));
    }
}
