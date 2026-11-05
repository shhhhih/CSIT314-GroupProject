
package com.team.idp.service;

import com.team.idp.entity.Category;
import com.team.idp.entity.Favourite;
import com.team.idp.entity.IDP;
import com.team.idp.entity.IdpView;
import com.team.idp.entity.UserAccount;
import com.team.idp.entity.UserProfile;

import com.team.idp.repository.CategoryRepository;
import com.team.idp.repository.FavouriteRepository;
import com.team.idp.repository.IDPRepository;
import com.team.idp.repository.IdpViewRepository;
import com.team.idp.repository.UserAccountRepository;
import com.team.idp.repository.UserProfileRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PlatformInsightsService {

    private final IDPRepository idpRepository;
    private final CategoryRepository categoryRepository;
    private final IdpViewRepository viewRepository;
    private final FavouriteRepository favouriteRepository;
    private final UserAccountRepository accountRepository;
    private final UserProfileRepository profileRepository;

    public PlatformInsightsService(
            IDPRepository idpRepository,
            CategoryRepository categoryRepository,
            IdpViewRepository viewRepository,
            FavouriteRepository favouriteRepository,
            UserAccountRepository accountRepository,
            UserProfileRepository profileRepository) {

        this.idpRepository = idpRepository;
        this.categoryRepository = categoryRepository;
        this.viewRepository = viewRepository;
        this.favouriteRepository = favouriteRepository;
        this.accountRepository = accountRepository;
        this.profileRepository = profileRepository;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getInsights(
            LocalDate from,
            LocalDate to) {

        if (from == null || to == null || from.isAfter(to)) {
            throw new IllegalArgumentException("Invalid date range");
        }

        if (ChronoUnit.DAYS.between(from, to) > 366) {
            throw new IllegalArgumentException(
                    "Please select a period of 367 days or fewer");
        }

        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.plusDays(1).atStartOfDay();

        List<IDP> projects = idpRepository.findAll();
        List<Category> categories = categoryRepository.findAll();

        Map<Long, String> titles = projects.stream()
                .collect(Collectors.toMap(
                        IDP::getIdpID,
                        IDP::getTitle
                ));

        // US26: Views within selected period
        Map<Long, Long> viewCounts = viewRepository
                .findByViewedAtGreaterThanEqualAndViewedAtLessThan(
                        start, end)
                .stream()
                .collect(Collectors.groupingBy(
                        IdpView::getIdpID,
                        Collectors.counting()
                ));

        // US26: Shortlists within selected period
        Map<Long, Long> shortlistCounts = favouriteRepository
                .findAll()
                .stream()
                .filter(f -> within(
                        f.getSavedAt(), start, end))
                .collect(Collectors.groupingBy(
                        Favourite::getIdpID,
                        Collectors.counting()
                ));

        // US27: All-time projects per category
        Map<Long, Long> categoryCounts = projects.stream()
                .collect(Collectors.groupingBy(
                        IDP::getCategoryID,
                        Collectors.counting()
                ));

        List<Map<String, Object>> categoryResults =
                new ArrayList<>();

        for (Category category : categories) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("categoryName", category.getCategoryName());
            item.put("count", categoryCounts.getOrDefault(
                    category.getCategoryID(), 0L));
            categoryResults.add(item);
        }

        categoryResults.sort((a, b) ->
                Long.compare(
                        ((Number) b.get("count")).longValue(),
                        ((Number) a.get("count")).longValue()
                ));

        // US28: Registrations grouped by profile
        Map<Long, String> profileNames = profileRepository
                .findAll()
                .stream()
                .collect(Collectors.toMap(
                        UserProfile::getProfileID,
                        UserProfile::getProfileName
                ));

        Map<String, Long> registrationCounts = new HashMap<>();

        for (UserAccount account : accountRepository.findAll()) {
            if (within(account.getCreatedAt(), start, end)) {
                String role = profileNames.getOrDefault(
                        account.getProfileID(), "Other");

                registrationCounts.merge(role, 1L, Long::sum);
            }
        }

        List<Map<String, Object>> registrations = new ArrayList<>();

        for (Map.Entry<String, Long> entry :
                registrationCounts.entrySet()) {

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("role", entry.getKey());
            item.put("count", entry.getValue());
            registrations.add(item);
        }

        registrations.sort((a, b) ->
                Long.compare(
                        ((Number) b.get("count")).longValue(),
                        ((Number) a.get("count")).longValue()
                ));

        long totalRegistrations = registrationCounts.values()
                .stream()
                .mapToLong(Long::longValue)
                .sum();

        Map<String, Object> response = new LinkedHashMap<>();

        response.put("from", from);
        response.put("to", to);

        response.put("mostViewed",
                topProjects(viewCounts, titles));

        response.put("mostShortlisted",
                topProjects(shortlistCounts, titles));

        response.put("categories", categoryResults);
        response.put("registrations", registrations);
        response.put("totalRegistrations", totalRegistrations);

        return response;
    }

    private boolean within(
            LocalDateTime date,
            LocalDateTime start,
            LocalDateTime end) {

        return date != null
                && !date.isBefore(start)
                && date.isBefore(end);
    }

    private List<Map<String, Object>> topProjects(
            Map<Long, Long> counts,
            Map<Long, String> titles) {

        return counts.entrySet().stream()
                .sorted(
                        Map.Entry.<Long, Long>comparingByValue(
                                Comparator.reverseOrder())
                )
                .limit(10)
                .map(entry -> {
                    Map<String, Object> item =
                            new LinkedHashMap<>();

                    item.put("idpId", entry.getKey());

                    item.put("title", titles.getOrDefault(
                            entry.getKey(),
                            "Deleted project"
                    ));

                    item.put("count", entry.getValue());

                    return item;
                })
                .toList();
    }
}
