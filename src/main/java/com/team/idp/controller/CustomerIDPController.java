package com.team.idp.controller;

import com.team.idp.service.CustomerIDPService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
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

/** API.md section 7: GET /api/idps (US13) and GET /api/idps/{idpId} (US15). Customer only. */
@RestController
@RequestMapping("/api/idps")
public class CustomerIDPController {

    private final CustomerIDPService customerIDPService;

    public CustomerIDPController(CustomerIDPService customerIDPService) {
        this.customerIDPService = customerIDPService;
    }

    @GetMapping
    public ResponseEntity<?> searchIDPs(@RequestParam(name = "keyword", required = false) String keyword,
                                        HttpServletRequest request) {
        ResponseEntity<?> denied = checkCustomer(request);
        if (denied != null) return denied;
        return ResponseEntity.ok(customerIDPService.searchIDPs(keyword));
    }

    @GetMapping("/{idpId}")
    public ResponseEntity<?> viewIDPDetails(@PathVariable("idpId") Long idpId, HttpServletRequest request) {
        ResponseEntity<?> denied = checkCustomer(request);
        if (denied != null) return denied;
        return ResponseEntity.ok(customerIDPService.viewIDPDetails(idpId, currentUserId(request)));
    }

    // ---- session helpers (same approach as ProfileController) ----

    private ResponseEntity<?> checkCustomer(HttpServletRequest request) {
        if (currentUserId(request) == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Please log in first"));
        }
        HttpSession session = request.getSession(false);
        if (!"Customer".equals(session.getAttribute("profileName"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "You do not have permission to do this"));
        }
        return null;
    }

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
