package com.team.idp.service;

import com.team.idp.entity.AccountStatus;
import com.team.idp.entity.ProfileStatus;
import com.team.idp.entity.UserAccount;
import com.team.idp.entity.UserProfile;
import com.team.idp.repository.UserAccountRepository;
import com.team.idp.repository.UserProfileRepository;
import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class LoginService {

    private final UserAccountRepository accountRepository;
    private final UserProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;

    public LoginService(UserAccountRepository accountRepository,
                        UserProfileRepository profileRepository,
                        PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.profileRepository = profileRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<UserAccount> findByUsernameOrEmail(String usernameOrEmail) {
        if (usernameOrEmail == null || usernameOrEmail.isBlank()) {
            return Optional.empty();
        }
        String value = usernameOrEmail.trim();
        if (value.contains("@")) {
            return accountRepository.findByEmail(value);
        }
        return accountRepository.findByUsername(value);
    }

    public boolean validatePassword(UserAccount account, String rawPassword) {
        if (rawPassword == null || rawPassword.isEmpty()) {
            return false;
        }
        return passwordEncoder.matches(rawPassword, account.getPassword());
    }

    public UserAccount authenticate(String usernameOrEmail, String rawPassword) {
        Optional<UserAccount> found = findByUsernameOrEmail(usernameOrEmail);
        if (found.isEmpty() || !validatePassword(found.get(), rawPassword)) {
            return null;
        }
        return found.get();
    }

    public Optional<UserProfile> getUserProfile(Long profileID) {
        return profileRepository.findById(profileID);
    }

    public boolean isActive(UserAccount account, UserProfile profile) {
        return account.getAccountStatus() == AccountStatus.ACTIVE
                && profile != null
                && profile.getProfileStatus() == ProfileStatus.ACTIVE;
    }

    public String getRedirectUrl(String profileName) {
        return switch (profileName) {
            case "User Admin" -> "/admin-home.html";
            case "Interior Designer" -> "/designer-home.html";
            case "Customer" -> "/customer-home.html";
            case "Platform Manager" -> "/platform-home.html";
            default -> "/login.html";
        };
    }
}