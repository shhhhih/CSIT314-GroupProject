package com.team.idp.service;

import com.team.idp.dto.ProfileResponse;
import com.team.idp.dto.ProfileUpdateRequest;
import com.team.idp.entity.AccountStatus;
import com.team.idp.entity.ProfileStatus;
import com.team.idp.entity.UserAccount;
import com.team.idp.entity.UserProfile;
import com.team.idp.repository.UserAccountRepository;
import com.team.idp.repository.UserProfileRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {
    @Mock UserAccountRepository accounts;
    @Mock UserProfileRepository roles;
    ProfileService service;
    UserAccount account;

    @BeforeEach
    void setup() {
        service = new ProfileService(accounts, roles);
        account = new UserAccount("customer1", "customer1@test.com", "hashed", 3L, AccountStatus.ACTIVE);
    }

    void existingActiveAccount() {
        when(accounts.findById(1L)).thenReturn(Optional.of(account));
        when(roles.findById(3L)).thenReturn(Optional.of(new UserProfile("Customer", "", ProfileStatus.ACTIVE)));
    }

    @Test
    void viewReturnsDetailsWithoutPassword() {
        existingActiveAccount();
        ProfileResponse response = service.view(1L);
        assertEquals("customer1", response.username());
        assertEquals("customer1@test.com", response.email());
        assertEquals("Customer", response.role());
        assertEquals("ACTIVE", response.accountStatus());
    }

    @Test
    void updateChangesUsernameAndEmailAndSaves() {
        existingActiveAccount();
        when(accounts.findByUsername("updatedUser")).thenReturn(Optional.empty());
        when(accounts.findByEmail("new@test.com")).thenReturn(Optional.empty());
        when(accounts.save(account)).thenReturn(account);
        ProfileResponse response = service.update(1L, new ProfileUpdateRequest("updatedUser", "new@test.com"));
        assertEquals("updatedUser", response.username());
        assertEquals("new@test.com", response.email());
        verify(accounts).save(account);
    }

    @Test
    void duplicateUsernameRejected() {
        existingActiveAccount();
        // A separate ID ensures the collision belongs to a different user.
        // Mockito mock supplies the database ID without altering the entity.
        UserAccount existing = mock(UserAccount.class);
        when(existing.getUserID()).thenReturn(2L);
        when(accounts.findByUsername("taken")).thenReturn(Optional.of(existing));
        assertThrows(IllegalArgumentException.class,
                () -> service.update(1L, new ProfileUpdateRequest("taken", "new@test.com")));
        verify(accounts, never()).save(any());
    }

    @Test
    void duplicateEmailRejected() {
        existingActiveAccount();
        when(accounts.findByUsername("newUser")).thenReturn(Optional.empty());
        UserAccount existing = mock(UserAccount.class);
        when(existing.getUserID()).thenReturn(2L);
        when(accounts.findByEmail("taken@test.com")).thenReturn(Optional.of(existing));
        assertThrows(IllegalArgumentException.class,
                () -> service.update(1L, new ProfileUpdateRequest("newUser", "taken@test.com")));
        verify(accounts, never()).save(any());
    }

    @Test
    void missingUserRejected() {
        when(accounts.findById(1L)).thenReturn(Optional.empty());
        assertThrows(java.util.NoSuchElementException.class, () -> service.view(1L));
    }

    @Test
    void inactiveAccountRejected() {
        account.setAccountStatus(AccountStatus.DISABLED);
        existingActiveAccount();
        assertThrows(IllegalStateException.class, () -> service.view(1L));
    }

    @Test
    void malformedEmailRejectedBeforeSave() {
        existingActiveAccount();
        assertThrows(IllegalArgumentException.class,
                () -> service.update(1L, new ProfileUpdateRequest("newUser", "bad-email")));
        verify(accounts, never()).save(any());
    }
}
