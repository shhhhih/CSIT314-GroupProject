package com.team.idp.service;

import com.team.idp.dto.DailyReportResponse;
import com.team.idp.entity.IdpView;
import com.team.idp.entity.IDP;
import com.team.idp.repository.UserAccountRepository;
import com.team.idp.repository.IDPRepository;
import com.team.idp.repository.IdpViewRepository;
import com.team.idp.repository.FavouriteRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DailyReportService {
    private final UserAccountRepository users;
    private final IDPRepository idps;
    private final IdpViewRepository views;
    private final FavouriteRepository favourites;

    public DailyReportService(UserAccountRepository users, IDPRepository idps,
            IdpViewRepository views, FavouriteRepository favourites) {
        this.users = users;
        this.idps = idps;
        this.views = views;
        this.favourites = favourites;
    }

    @Transactional(readOnly = true)
    public DailyReportResponse generateDailyReport(LocalDate date) {
        LocalDate target = date == null ? LocalDate.now() : date;
        LocalDateTime start = target.atStartOfDay();
        LocalDateTime end = target.plusDays(1).atStartOfDay();
        List<IdpView> dailyViews = views.findByViewedAtGreaterThanEqualAndViewedAtLessThan(start,end);
        Map<Long,Long> counts = dailyViews.stream()
                .collect(Collectors.groupingBy(IdpView::getIdpID,Collectors.counting()));
        List<DailyReportResponse.TopIdp> top = counts.entrySet().stream()
                .sorted(Map.Entry.<Long,Long>comparingByValue(Comparator.reverseOrder()))
                .limit(5)
                .map(entry -> {
                    String title = idps.findById(entry.getKey()).map(IDP::getTitle).orElse("Deleted project");
                    return new DailyReportResponse.TopIdp(entry.getKey(),title,entry.getValue());
                }).toList();
        return new DailyReportResponse(target,
                users.countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(start,end),
                idps.countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(start,end),
                dailyViews.size(),
                favourites.countBySavedAtGreaterThanEqualAndSavedAtLessThan(start,end), top);
    }
}
