package com.team.idp.seed;

import com.team.idp.entity.ProfileStatus;
import com.team.idp.entity.UserProfile;
import com.team.idp.repository.UserProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(10)
public class ProfileSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ProfileSeeder.class);

    private final UserProfileRepository profileRepository;

    public ProfileSeeder(UserProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    @Override
    public void run(String... args) {
        if (profileRepository.count() > 0) {
            return;
        }
        profileRepository.save(new UserProfile("User Admin",
                "Manages accounts and profiles", ProfileStatus.ACTIVE));
        profileRepository.save(new UserProfile("Interior Designer",
                "Manages company and IDPs", ProfileStatus.ACTIVE));
        profileRepository.save(new UserProfile("Customer",
                "Searches and saves IDPs", ProfileStatus.ACTIVE));
        profileRepository.save(new UserProfile("Platform Manager",
                "Manages categories and reports", ProfileStatus.ACTIVE));

        log.info("Seeded {} user profiles", profileRepository.count());
    }
}