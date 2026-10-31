package com.team.idp.service;

import com.team.idp.dto.DailyReportResponse;
import com.team.idp.repository.IDPRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;

@Service
public class DailyReportService {

    private final IDPRepository idpRepository;

    public DailyReportService(IDPRepository idpRepository) {
        this.idpRepository = idpRepository;
    }

    public DailyReportResponse generateDailyReport(LocalDate date) {

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();

        long totalIDPsCreated = idpRepository.countByCreatedAtBetween(start, end);

        return new DailyReportResponse(date,totalIDPsCreated);
    }
}