package com.team.idp.controller;

import com.team.idp.service.CustomerIDPService;
import com.team.idp.service.IDPResponseEnricher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.Map;
import java.util.NoSuchElementException;
import java.math.BigDecimal;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** API.md section 7: GET /api/idps (US13) and GET /api/idps/{idpId} (US15). Customer only. */
@RestController
@RequestMapping("/api/idps")
public class CustomerIDPController {

    private final CustomerIDPService customerIDPService;
    private final IDPResponseEnricher enricher;

    public CustomerIDPController(CustomerIDPService customerIDPService, IDPResponseEnricher enricher) {
        this.customerIDPService = customerIDPService;
        this.enricher = enricher;
    }

    @GetMapping

    public ResponseEntity<?> searchIDPs(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "categoryId", required = false) Long categoryId,
            @RequestParam(name = "propertyType", required = false) String propertyType,
            @RequestParam(name = "minBudget", required = false) BigDecimal minBudget,
            @RequestParam(name = "maxBudget", required = false) BigDecimal maxBudget,
            HttpServletRequest request) {

        ResponseEntity<?> denied = checkCustomer(request);
        if (denied != null) return denied;

        return ResponseEntity.ok(
                customerIDPService.searchIDPsWithFilters(
                        keyword,
                        categoryId,
                        propertyType,
                        minBudget,
                        maxBudget
                ).stream()
                 .map(enricher::summary)
                 .toList()
        );
    }

    @GetMapping("/{idpId}")
    public ResponseEntity<?> viewIDPDetails(
            @PathVariable("idpId") Long idpId,
            HttpServletRequest request) {

        ResponseEntity<?> denied = checkCustomer(request);
        if (denied != null) return denied;

        return ResponseEntity.ok(
                enricher.details(
                        customerIDPService.viewIDPDetails(
                                idpId,
                                currentUserId(request)
                        )
                )
        );
    }

    }

    // ---- session helpers (same approach as ProfileController) ----

    private Long currentUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return null;
        Object id = session.getAttribute("userId");
        return id instanceof Long ? (Long) id : null;
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<?> notFound(NoSuchElementException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", exception.getMessage()));
    }
}
