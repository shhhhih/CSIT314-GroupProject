
package com.team.idp.controller;

import com.team.idp.repository.UserAccountRepository;
import com.team.idp.repository.UserProfileRepository;
import com.team.idp.repository.IDPRepository;
import com.team.idp.repository.IdpViewRepository;
import com.team.idp.repository.FavouriteRepository;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
public class PlatformDashboardController {

    private final UserAccountRepository accountRepository;
    private final UserProfileRepository profileRepository;
    private final IDPRepository idpRepository;
    private final IdpViewRepository viewRepository;
    private final FavouriteRepository favouriteRepository;

    public PlatformDashboardController(
            UserAccountRepository accountRepository,
            UserProfileRepository profileRepository,
            IDPRepository idpRepository,
            IdpViewRepository viewRepository,
            FavouriteRepository favouriteRepository) {

        this.accountRepository = accountRepository;
        this.profileRepository = profileRepository;
        this.idpRepository = idpRepository;
        this.viewRepository = viewRepository;
        this.favouriteRepository = favouriteRepository;
    }

    // Count users belonging to a particular role
    private long countUsersByRole(String role) {

        return profileRepository.findByProfileName(role)
                .map(profile ->
                        accountRepository.countByProfileID(
                                profile.getProfileID()
                        )
                )
                .orElse(0L);
    }

    // Return overall Platform Manager dashboard statistics
    @GetMapping("/overview")
    public Map<String, Long> getOverview() {

        return Map.of(
                "totalUsers", accountRepository.count(),

                "totalCustomers",
                countUsersByRole("Customer"),

                "totalDesigners",
                countUsersByRole("Interior Designer"),

                "totalIdps", idpRepository.count(),

                "totalViews", viewRepository.count(),

                "totalShortlists", favouriteRepository.count()
        );
    }
}
