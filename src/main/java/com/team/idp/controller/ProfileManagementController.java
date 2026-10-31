package com.team.idp.controller;

import com.team.idp.dto.ProfileStatusRequest;
import com.team.idp.dto.UserProfileRequest;
import com.team.idp.service.ConflictException;
import com.team.idp.service.ProfileManagementService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.Map;
import java.util.NoSuchElementException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/profiles")
public class ProfileManagementController {

    private static final String ADMIN_ROLE = "User Admin";

    private final ProfileManagementService service;

    public ProfileManagementController(ProfileManagementService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<?> list(@RequestParam(name = "keyword", required = false) String keyword,
                                  HttpServletRequest request) {
        ResponseEntity<?> denied = checkAdmin(request);
        if (denied != null) return denied;
        return ResponseEntity.ok(service.search(keyword));
    }

    @GetMapping("/{profileId}")
    public ResponseEntity<?> view(@PathVariable("profileId") Long profileId,
                                  HttpServletRequest request) {
        ResponseEntity<?> denied = checkAdmin(request);
        if (denied != null) return denied;
        return ResponseEntity.ok(service.view(profileId));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody UserProfileRequest body,
                                    HttpServletRequest request) {
        ResponseEntity<?> denied = checkAdmin(request);
        if (denied != null) return denied;
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(body));
    }

    @PutMapping("/{profileId}")
    public ResponseEntity<?> update(@PathVariable("profileId") Long profileId,
                                    @RequestBody UserProfileRequest body,
                                    HttpServletRequest request) {
        ResponseEntity<?> denied = checkAdmin(request);
        if (denied != null) return denied;
        return ResponseEntity.ok(service.update(profileId, body));
    }

    @PutMapping("/{profileId}/status")
    public ResponseEntity<?> changeStatus(@PathVariable("profileId") Long profileId,
                                          @RequestBody ProfileStatusRequest body,
                                          HttpServletRequest request) {
        ResponseEntity<?> denied = checkAdmin(request);
        if (denied != null) return denied;
        return ResponseEntity.ok(service.changeStatus(profileId, body));
    }

    /** Returns an error response if the caller is not a logged-in User Admin, otherwise null. */
    private ResponseEntity<?> checkAdmin(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || !(session.getAttribute("userId") instanceof Long)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Please log in first"));
        }
        if (!ADMIN_ROLE.equals(session.getAttribute("profileName"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "You do not have permission to do this"));
        }
        return null;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> invalidInput(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<?> notFound(NoSuchElementException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<?> conflict(ConflictException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", e.getMessage()));
    }
}