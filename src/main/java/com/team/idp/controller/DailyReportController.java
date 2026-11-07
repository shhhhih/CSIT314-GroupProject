package com.team.idp.controller;

import com.team.idp.dto.DailyReportResponse;
import com.team.idp.service.DailyReportService;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
public class DailyReportController {

    private final DailyReportService dailyReportService;

    public DailyReportController(DailyReportService dailyReportService) {
        this.dailyReportService = dailyReportService;
    }

    @GetMapping("/daily")
    public DailyReportResponse generateDailyReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        return dailyReportService.generateDailyReport(date);
    }
}
