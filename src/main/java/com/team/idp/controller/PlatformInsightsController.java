
package com.team.idp.controller;

import com.team.idp.service.PlatformInsightsService;

import java.time.LocalDate;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
public class PlatformInsightsController {

    private final PlatformInsightsService insightsService;

    public PlatformInsightsController(
            PlatformInsightsService insightsService) {

        this.insightsService = insightsService;
    }

    @GetMapping("/insights")
    public Map<String, Object> getInsights(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate from,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate to) {

        return insightsService.getInsights(from, to);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> invalidDates(
            IllegalArgumentException error) {

        return ResponseEntity.badRequest().body(
                Map.of("message", error.getMessage())
        );
    }
}
