package com.team.idp.controller;

import com.team.idp.dto.ProfileStatusRequest;
import com.team.idp.dto.UserProfileRequest;
import com.team.idp.service.ConflictException;
import com.team.idp.service.ProfileManagementService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.NoSuchElementException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/profiles")
public class ProfileManagementController {

    private final ProfileManagementService service;

    public ProfileManagementController(ProfileManagementService service) {
        this.service = service;
    }

    public ResponseEntity<?> list(String keyword, HttpServletRequest request) {
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<?> view(Long profileId, HttpServletRequest request) {
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<?> create(UserProfileRequest body, HttpServletRequest request) {
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<?> update(Long profileId, UserProfileRequest body, HttpServletRequest request) {
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<?> changeStatus(Long profileId, ProfileStatusRequest body, HttpServletRequest request) {
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<?> invalidInput(IllegalArgumentException e) {
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<?> notFound(NoSuchElementException e) {
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<?> conflict(ConflictException e) {
        return ResponseEntity.ok().build();
    }
}