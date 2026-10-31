package com.team.idp.controller;

import com.team.idp.entity.Company;
import com.team.idp.service.CompanyService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {

    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    // Create company information
    @PostMapping
    public ResponseEntity<?> createCompany(
            @RequestBody Company company,
            HttpServletRequest request) {

        Long userID = getDesignerUserID(request);

        if (userID == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "message",
                            "Please log in as an interior designer"));
        }

        try {
            Company createdCompany =
                    companyService.createCompany(userID, company);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(createdCompany);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }

    // View the logged-in designer's company information
    @GetMapping("/me")
    public ResponseEntity<?> getMyCompany(HttpServletRequest request) {

        Long userID = getDesignerUserID(request);

        if (userID == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "message",
                            "Please log in as an interior designer"));
        }

        return companyService.getMyCompany(userID)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of(
                                "message",
                                "No company information found. Please add your company information.")));
    }

    // Update the logged-in designer's company information
    @PutMapping("/me")
    public ResponseEntity<?> updateCompany(
            @RequestBody Company company,
            HttpServletRequest request) {

        Long userID = getDesignerUserID(request);

        if (userID == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "message",
                            "Please log in as an interior designer"));
        }

        try {
            Company updatedCompany =
                    companyService.updateCompany(userID, company);

            return ResponseEntity.ok(updatedCompany);

        } catch (IllegalArgumentException e) {
            if ("Company information not found".equals(e.getMessage())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", e.getMessage()));
            }

            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }

    // Allow visitors to view a company's public information
    @GetMapping("/{id}")
    public ResponseEntity<?> getCompanyById(
            @PathVariable("id") Long companyID) {

        try {
            Company company = companyService.getCompanyById(companyID);

            return ResponseEntity.ok(company);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    // Upload or replace the company logo
    @PostMapping("/me/logo")
    public ResponseEntity<?> uploadCompanyLogo(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {

        Long userID = getDesignerUserID(request);

        if (userID == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "message",
                            "Please log in as an interior designer"));
        }

        try {
            Company company =
                    companyService.uploadCompanyLogo(userID, file);

            return ResponseEntity.ok(company);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "message",
                            "Unable to save the company logo"));
        }
    }

    // Check the session and verify the user's role
    private Long getDesignerUserID(HttpServletRequest request) {

        HttpSession session = request.getSession(false);

        if (session == null) {
            return null;
        }

        Object profileName = session.getAttribute("profileName");
        Object userID = session.getAttribute("userId");

        if (!"Interior Designer".equals(profileName)
                || !(userID instanceof Long)) {
            return null;
        }

        return (Long) userID;
    }
}