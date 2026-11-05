package com.team.idp.dto;

import java.time.LocalDate;
import java.util.List;

public record DailyReportResponse(LocalDate date, long newUsers, long newIdps,
        long totalViews, long totalShortlists, List<TopIdp> topIdps) {
    public record TopIdp(Long idpId, String title, long views) { }
}
