package com.team.idp.controller;

import com.team.idp.dto.ProfileUpdateRequest;
import com.team.idp.service.ProfileService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.Map;
import java.util.NoSuchElementException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {
    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public ResponseEntity<?> view(HttpServletRequest request) {
        Long userId = currentUserId(request);
        if (userId == null) return unauthorised();
        return ResponseEntity.ok(profileService.view(userId));
    }

    @PutMapping
    public ResponseEntity<?> update(@Valid @RequestBody ProfileUpdateRequest body,
                                    HttpServletRequest request) {
        Long userId = currentUserId(request);
        if (userId == null) return unauthorised();
        return ResponseEntity.ok(profileService.update(userId, body));
    }

    private Long currentUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return null;
        Object id = session.getAttribute("userId");
        return id instanceof Long ? (Long) id : null;
    }

    private ResponseEntity<?> unauthorised() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Please log in first"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> invalidFields() {
        return ResponseEntity.badRequest().body(Map.of("message", "Enter a valid username and email address"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> invalidUpdate(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(Map.of("message", exception.getMessage()));
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<?> notFound(NoSuchElementException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", exception.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<?> inactive(IllegalStateException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", exception.getMessage()));
    }
}
