package com.team.idp.controller;

import com.team.idp.dto.FavouriteRequest;
import com.team.idp.service.FavouriteService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.Map;
import java.util.NoSuchElementException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** API.md section 8: POST /api/favourites (US17) and GET /api/favourites (US18). Customer only. */
@RestController
@RequestMapping("/api/favourites")
public class FavouriteController {

    private final FavouriteService favouriteService;

    public FavouriteController(FavouriteService favouriteService) {
        this.favouriteService = favouriteService;
    }

    @PostMapping
    public ResponseEntity<?> saveToFavourites(@RequestBody(required = false) FavouriteRequest body,
                                              HttpServletRequest request) {
        ResponseEntity<?> denied = checkCustomer(request);
        if (denied != null) return denied;
        Long idpId = body == null ? null : body.idpId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(favouriteService.saveToFavourites(currentUserId(request), idpId));
    }

    @GetMapping
    public ResponseEntity<?> getFavouritesList(HttpServletRequest request) {
        ResponseEntity<?> denied = checkCustomer(request);
        if (denied != null) return denied;
        return ResponseEntity.ok(favouriteService.getFavouritesList(currentUserId(request)));
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

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> badRequest(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(Map.of("message", exception.getMessage()));
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<?> notFound(NoSuchElementException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", exception.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<?> alreadySaved(IllegalStateException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", exception.getMessage()));
    }

    /** Two clicks at the same moment: the database's unique rule catches the second one. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> duplicate() {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "You already saved this IDP"));
    }
}
