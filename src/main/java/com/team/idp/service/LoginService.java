package com.team.idp.service;

import com.team.idp.entity.UserAccount;
import com.team.idp.entity.UserProfile;
import com.team.idp.repository.UserAccountRepository;
import com.team.idp.repository.UserProfileRepository;
import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class LoginService {

    public LoginService(UserAccountRepository accountRepository,
                        UserProfileRepository profileRepository,
                        PasswordEncoder passwordEncoder) {
    }

    public Optional<UserAccount> findByUsernameOrEmail(String usernameOrEmail) {
        throw new UnsupportedOperationException();
    }

    public boolean validatePassword(UserAccount account, String rawPassword) {
        throw new UnsupportedOperationException();
    }

    public UserAccount authenticate(String usernameOrEmail, String rawPassword) {
        throw new UnsupportedOperationException();
    }

    public Optional<UserProfile> getUserProfile(Long profileID) {
        throw new UnsupportedOperationException();
    }

    public boolean isActive(UserAccount account, UserProfile profile) {
        throw new UnsupportedOperationException();
    }

    public String getRedirectUrl(String profileName) {
        throw new UnsupportedOperationException();
    }
}