package com.team.idp.dto;

import java.time.LocalDate;

public record DailyReportResponse(LocalDate reportDate, long totalIDPsCreated) {
    
}