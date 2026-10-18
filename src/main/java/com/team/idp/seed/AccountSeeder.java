package com.team.idp.seed;

import com.team.idp.entity.AccountStatus;
import com.team.idp.entity.UserAccount;
import com.team.idp.repository.UserAccountRepository;
import com.team.idp.repository.UserProfileRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Order(20)
public class AccountSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AccountSeeder.class);

    private final UserAccountRepository accountRepository;
    private final UserProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;

    public AccountSeeder(UserAccountRepository accountRepository,
                         UserProfileRepository profileRepository,
                         PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.profileRepository = profileRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (accountRepository.count() > 0) {
            return;
        }

        Long adminId = profileId("User Admin");
        Long designerId = profileId("Interior Designer");
        Long customerId = profileId("Customer");
        Long platformId = profileId("Platform Manager");

        String password = passwordEncoder.encode("Password123");  
        Random random = new Random(1);                             
        List<UserAccount> accounts = new ArrayList<>();

        
        accounts.add(account("admin1", password, adminId, random));
        accounts.add(account("designer1", password, designerId, random));
        accounts.add(account("customer1", password, customerId, random));
        accounts.add(account("platform1", password, platformId, random));

        
        for (int i = 1; i <= 100; i++) {
            accounts.add(account(String.format("designer%03d", i), password, designerId, random));
            accounts.add(account(String.format("customer%03d", i), password, customerId, random));
        }

        accountRepository.saveAll(accounts);
        log.info("Seeded {} user accounts", accounts.size());
    }

    private Long profileId(String profileName) {
        return profileRepository.findByProfileName(profileName)
                .orElseThrow(() -> new IllegalStateException(
                        "Profile not found: " + profileName + " (ProfileSeeder must run first)"))
                .getProfileID();
    }

    private UserAccount account(String username, String password, Long profileId, Random random) {
        UserAccount account = new UserAccount(username, username + "@test.com",
                password, profileId, AccountStatus.ACTIVE);
       
        account.setCreatedAt(LocalDateTime.now()
                .minusDays(random.nextInt(90))
                .minusMinutes(random.nextInt(1440)));
        return account;
    }
}