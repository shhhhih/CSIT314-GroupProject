
package com.team.idp.service;

import com.team.idp.dto.CustomerRegisterRequest;
import com.team.idp.entity.AccountStatus;
import com.team.idp.entity.ProfileStatus;
import com.team.idp.entity.UserAccount;
import com.team.idp.entity.UserProfile;
import com.team.idp.repository.UserAccountRepository;
import com.team.idp.repository.UserProfileRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerRegistrationService {

    private final UserAccountRepository accountRepository;
    private final UserProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;

    public CustomerRegistrationService(
            UserAccountRepository accountRepository,
            UserProfileRepository profileRepository,
            PasswordEncoder passwordEncoder) {

        this.accountRepository = accountRepository;
        this.profileRepository = profileRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserAccount register(CustomerRegisterRequest request) {

        if (request == null) {
            throw new IllegalArgumentException("Registration details are required");
        }

        String username = request.username() == null
                ? "" : request.username().trim();

        String email = request.email() == null
                ? "" : request.email().trim();

        String password = request.password();
        String confirmPassword = request.confirmPassword();

        if (username.length() < 3 || username.length() > 50) {
            throw new IllegalArgumentException(
                    "Username must be between 3 and 50 characters");
        }

        if (email.isBlank() || email.length() > 254
                || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("Enter a valid email address");
        }

        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException(
                    "Password must contain at least 8 characters");
        }

        if (!password.equals(confirmPassword)) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        if (accountRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username is already registered");
        }

        if (accountRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email is already registered");
        }

        UserProfile customerProfile = profileRepository
                .findByProfileName("Customer")
                .orElseThrow(() -> new IllegalStateException(
                        "Customer profile has not been configured"));

        if (customerProfile.getProfileStatus() != ProfileStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Customer registration is currently unavailable");
        }

        UserAccount account = new UserAccount(
                username,
                email,
                passwordEncoder.encode(password),
                customerProfile.getProfileID(),
                AccountStatus.ACTIVE
        );

        return accountRepository.save(account);
    }
}
