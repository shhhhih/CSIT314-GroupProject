package com.team.idp.controller;

import com.team.idp.dto.RatingRequest;
import com.team.idp.service.RatingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.Map;
import java.util.NoSuchElementException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * POST /api/ratings (Customer only): rate an IDP 1 to 5 stars, or change your rating.
 * GET  /api/ratings/idp/{idpId} (any logged-in user): average, count and list of ratings.
 */
@RestController
@RequestMapping("/api/ratings")
public class RatingController {

    private final RatingService ratingService;

    public RatingController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    @PostMapping
    public ResponseEntity<?> rateIDP(@RequestBody(required = false) RatingRequest body, HttpServletRequest request) {
        Long userId = currentUserId(request);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Please log in first"));
        }
        if (!"Customer".equals(request.getSession(false).getAttribute("profileName"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "You do not have permission to do this"));
        }
        return ResponseEntity.ok(ratingService.rateIDP(userId, body));
    }

    @GetMapping("/idp/{idpId}")
    public ResponseEntity<?> getIDPRatings(@PathVariable("idpId") Long idpId) {
        return ResponseEntity.ok(ratingService.getIDPRatings(idpId));
    }

    private Long currentUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return null;
        Object id = session.getAttribute("userId");
        return id instanceof Long ? (Long) id : null;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> badRequest(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(Map.of("message", exception.getMessage()));
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<?> notFound(NoSuchElementException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", exception.getMessage()));
    }

    /** Two ratings sent at the same moment: the database's unique rule rejects the second. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> duplicate() {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "Please try again"));
    }
}
