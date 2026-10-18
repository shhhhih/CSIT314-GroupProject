package com.team.idp.seed;

import com.team.idp.entity.AccountStatus;
import com.team.idp.entity.ProfileStatus;
import com.team.idp.entity.UserAccount;
import com.team.idp.entity.UserProfile;
import com.team.idp.repository.UserAccountRepository;
import com.team.idp.repository.UserProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Order(25)
public class LoginTestSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(LoginTestSeeder.class);

    private final UserAccountRepository accountRepository;
    private final UserProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;

    public LoginTestSeeder(UserAccountRepository accountRepository,
                           UserProfileRepository profileRepository,
                           PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.profileRepository = profileRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (accountRepository.findByUsername("disabled1").isPresent()) {
            return;
        }
        UserProfile customer = profileRepository.findByProfileName("Customer")
                .orElseThrow(() -> new IllegalStateException(
                        "Profile not found: Customer (ProfileSeeder must run first)"));

        String password = passwordEncoder.encode("Password123");

        UserProfile suspended = profileRepository.save(
                new UserProfile("Suspended Test Profile", "For testing only", ProfileStatus.SUSPENDED));

        accountRepository.save(new UserAccount("disabled1", "disabled1@test.com", password,
                customer.getProfileID(), AccountStatus.DISABLED));
        accountRepository.save(new UserAccount("suspended1", "suspended1@test.com", password,
                suspended.getProfileID(), AccountStatus.ACTIVE));

        log.info("Seeded login test accounts (disabled1, suspended1)");
    }
}