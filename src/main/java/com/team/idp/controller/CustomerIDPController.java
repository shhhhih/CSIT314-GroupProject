
package com.team.idp.controller;

import com.team.idp.service.CustomerIDPService;
import com.team.idp.service.IDPResponseEnricher;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.math.BigDecimal;
import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Customer IDP Controller
 *
 * US13 - Search and browse IDPs
 * US15 - View IDP details
 *
 * Only logged-in Customers can access these endpoints.
 * Guests use /api/guest/idps instead.
 */
@RestController
@RequestMapping("/api/idps")
public class CustomerIDPController {

    private final CustomerIDPService customerIDPService;
    private final IDPResponseEnricher enricher;

    public CustomerIDPController(
            CustomerIDPService customerIDPService,
            IDPResponseEnricher enricher) {

        this.customerIDPService = customerIDPService;
        this.enricher = enricher;
    }

    // US13 - Search IDPs with optional filters
    @GetMapping
    public ResponseEntity<?> searchIDPs(
            @RequestParam(name = "keyword", required = false)
            String keyword,

            @RequestParam(name = "categoryId", required = false)
            Long categoryId,

            @RequestParam(name = "propertyType", required = false)
            String propertyType,

            @RequestParam(name = "minBudget", required = false)
            BigDecimal minBudget,

            @RequestParam(name = "maxBudget", required = false)
            BigDecimal maxBudget,

            HttpServletRequest request) {

        // Check that user is logged in as Customer
        ResponseEntity<?> denied = checkCustomer(request);

        if (denied != null) {
            return denied;
        }

        // Search IDPs and include additional project information
        return ResponseEntity.ok(
                customerIDPService.searchIDPsWithFilters(
                        keyword,
                        categoryId,
                        propertyType,
                        minBudget,
                        maxBudget
                )
                .stream()
                .map(enricher::summary)
                .toList()
        );
    }

    // US15 - View IDP details
    @GetMapping("/{idpId}")
    public ResponseEntity<?> viewIDPDetails(
            @PathVariable("idpId") Long idpId,
            HttpServletRequest request) {

        // Check that user is logged in as Customer
        ResponseEntity<?> denied = checkCustomer(request);

        if (denied != null) {
            return denied;
        }

        // Retrieve project details and enrich response
        return ResponseEntity.ok(
                enricher.details(
                        customerIDPService.viewIDPDetails(
                                idpId,
                                currentUserId(request)
                        )
                )
        );
    }

    // Check whether the current user has Customer access
    private ResponseEntity<?> checkCustomer(
            HttpServletRequest request) {

        HttpSession session = request.getSession(false);

        // User is not logged in
        if (session == null
                || !(session.getAttribute("userId") instanceof Long)) {

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "message",
                            "Please log in first"
                    ));
        }

        // User does not have Customer role
        if (!"Customer".equals(session.getAttribute("profileName"))) {

            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                            "message",
                            "Customer access required"
                    ));
        }

        // Access allowed
        return null;
    }

    // Retrieve the logged-in user's ID
    private Long currentUserId(
            HttpServletRequest request) {

        HttpSession session = request.getSession(false);

        if (session == null) {
            return null;
        }

        Object id = session.getAttribute("userId");

        return id instanceof Long ? (Long) id : null;
    }

    // Handle IDPs that cannot be found
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<?> notFound(
            NoSuchElementException exception) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                        "message",
                        exception.getMessage()
                ));
    }
}
