package com.team.idp.service;

import com.team.idp.dto.ProfileResponse;
import com.team.idp.dto.ProfileUpdateRequest;
import com.team.idp.entity.AccountStatus;
import com.team.idp.entity.ProfileStatus;
import com.team.idp.entity.UserAccount;
import com.team.idp.entity.UserProfile;
import com.team.idp.repository.UserAccountRepository;
import com.team.idp.repository.UserProfileRepository;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileService {
    private final UserAccountRepository accountRepository;
    private final UserProfileRepository profileRepository;

    public ProfileService(UserAccountRepository accountRepository, UserProfileRepository profileRepository) {
        this.accountRepository = accountRepository;
        this.profileRepository = profileRepository;
    }

    @Transactional(readOnly = true)
    public ProfileResponse view(Long userId) {
        UserAccount account = accountRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Account not found"));
        UserProfile role = profileRepository.findById(account.getProfileID())
                .orElseThrow(() -> new NoSuchElementException("Account role not found"));
        if (account.getAccountStatus() != AccountStatus.ACTIVE || role.getProfileStatus() != ProfileStatus.ACTIVE) {
            throw new IllegalStateException("This account is not active");
        }
        return ProfileResponse.from(account, role);
    }

    @Transactional
    public ProfileResponse update(Long userId, ProfileUpdateRequest request) {
        // Session-derived userId, never a userId supplied by the browser.
        UserAccount account = accountRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Account not found"));
        UserProfile role = profileRepository.findById(account.getProfileID())
                .orElseThrow(() -> new NoSuchElementException("Account role not found"));
        if (account.getAccountStatus() != AccountStatus.ACTIVE || role.getProfileStatus() != ProfileStatus.ACTIVE) {
            throw new IllegalStateException("This account is not active");
        }

        String username = request.username().trim();
        String email = request.email().trim();
        if (username.length() < 3 || username.length() > 50 || email.length() > 254 ||
                !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("Enter a valid username and email address");
        }
        accountRepository.findByUsername(username)
                .filter(existing -> !existing.getUserID().equals(userId))
                .ifPresent(existing -> { throw new IllegalArgumentException("Username is already taken"); });
        accountRepository.findByEmail(email)
                .filter(existing -> !existing.getUserID().equals(userId))
                .ifPresent(existing -> { throw new IllegalArgumentException("Email is already registered"); });

        account.setUsername(username);
        account.setEmail(email);
        // Do not change password, account status, role or any other user's account.
        return ProfileResponse.from(accountRepository.save(account), role);
    }
}
