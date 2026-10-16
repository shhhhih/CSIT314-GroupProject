package com.team.idp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.team.idp.entity.AccountStatus;
import com.team.idp.entity.ProfileStatus;
import com.team.idp.entity.UserAccount;
import com.team.idp.entity.UserProfile;
import com.team.idp.repository.UserAccountRepository;
import com.team.idp.repository.UserProfileRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    @Mock
    private UserAccountRepository accountRepository;

    @Mock
    private UserProfileRepository profileRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private LoginService loginService;

    private UserAccount activeAccount() {
        return new UserAccount("customer1", "customer1@test.com", "hashed", 3L, AccountStatus.ACTIVE);
    }

    private UserProfile profile(ProfileStatus status) {
        return new UserProfile("Customer", "Customer profile", status);
    }

    //Validate Username or Email

    @Test
    void findByUsernameOrEmail_username_returnsAccount() {
        when(accountRepository.findByUsername("customer1")).thenReturn(Optional.of(activeAccount()));

        assertTrue(loginService.findByUsernameOrEmail("customer1").isPresent());
    }

    @Test
    void findByUsernameOrEmail_email_searchesByEmail() {
        when(accountRepository.findByEmail("customer1@test.com")).thenReturn(Optional.of(activeAccount()));

        assertTrue(loginService.findByUsernameOrEmail("customer1@test.com").isPresent());
        verify(accountRepository, never()).findByUsername(anyString());
    }

    @Test
    void findByUsernameOrEmail_blank_returnsEmpty() {
        assertTrue(loginService.findByUsernameOrEmail("   ").isEmpty());
        verifyNoInteractions(accountRepository);
    }

    //Validate Password

    @Test
    void validatePassword_correct_returnsTrue() {
        when(passwordEncoder.matches("Password123", "hashed")).thenReturn(true);

        assertTrue(loginService.validatePassword(activeAccount(), "Password123"));
    }

    @Test
    void validatePassword_wrong_returnsFalse() {
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        assertFalse(loginService.validatePassword(activeAccount(), "wrong"));
    }

    //Authenticate User

    @Test
    void authenticate_validCredentials_returnsAccount() {
        UserAccount account = activeAccount();
        when(accountRepository.findByUsername("customer1")).thenReturn(Optional.of(account));
        when(passwordEncoder.matches("Password123", "hashed")).thenReturn(true);

        assertSame(account, loginService.authenticate("customer1", "Password123"));
    }

    @Test
    void authenticate_unknownUser_returnsNull() {
        when(accountRepository.findByUsername("nobody")).thenReturn(Optional.empty());

        assertNull(loginService.authenticate("nobody", "Password123"));
    }

    @Test
    void authenticate_wrongPassword_returnsNull() {
        when(accountRepository.findByUsername("customer1")).thenReturn(Optional.of(activeAccount()));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        assertNull(loginService.authenticate("customer1", "wrong"));
    }

    @Test
    void isActive_activeAccountAndActiveProfile_true() {
        assertTrue(loginService.isActive(activeAccount(), profile(ProfileStatus.ACTIVE)));
    }

    @Test
    void isActive_disabledAccount_false() {
        UserAccount disabled = new UserAccount("d1", "d1@test.com", "hashed", 3L, AccountStatus.DISABLED);

        assertFalse(loginService.isActive(disabled, profile(ProfileStatus.ACTIVE)));
    }

    @Test
    void isActive_suspendedProfile_false() {
        assertFalse(loginService.isActive(activeAccount(), profile(ProfileStatus.SUSPENDED)));
    }

    @Test
    void isActive_missingProfile_false() {
        assertFalse(loginService.isActive(activeAccount(), null));
    }

    @Test
    void getUserProfile_returnsProfile() {
        UserProfile p = profile(ProfileStatus.ACTIVE);
        when(profileRepository.findById(3L)).thenReturn(Optional.of(p));

        assertSame(p, loginService.getUserProfile(3L).orElseThrow());
    }

    //Redirect User to Dashboard By Role

    @Test
    void getRedirectUrl_eachProfile() {
        assertEquals("/admin-home.html", loginService.getRedirectUrl("User Admin"));
        assertEquals("/designer-home.html", loginService.getRedirectUrl("Interior Designer"));
        assertEquals("/customer-home.html", loginService.getRedirectUrl("Customer"));
        assertEquals("/platform-home.html", loginService.getRedirectUrl("Platform Manager"));
    }

    @Test
    void getRedirectUrl_unknownProfile_backToLogin() {
        assertEquals("/login.html", loginService.getRedirectUrl("Something Else"));
    }
}