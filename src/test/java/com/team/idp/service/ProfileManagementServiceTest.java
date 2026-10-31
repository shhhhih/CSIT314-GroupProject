package com.team.idp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.team.idp.dto.ProfileStatusRequest;
import com.team.idp.dto.UserProfileRequest;
import com.team.idp.dto.UserProfileResponse;
import com.team.idp.entity.ProfileStatus;
import com.team.idp.entity.UserProfile;
import com.team.idp.repository.UserProfileRepository;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
class ProfileManagementServiceTest {

    @Mock
    private UserProfileRepository profileRepository;

    @InjectMocks
    private ProfileManagementService service;

    private UserProfile profile(String name, String description, ProfileStatus status) {
        return new UserProfile(name, description, status);
    }

    // ---------- create ----------

    @Test
    void createSavesAnActiveProfileWithTrimmedValues() {
        when(profileRepository.findByProfileName("Interior Decorator")).thenReturn(Optional.empty());
        when(profileRepository.save(any(UserProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        UserProfileResponse response =
                service.create(new UserProfileRequest("  Interior Decorator ", " Decorates "));

        assertEquals("Interior Decorator", response.profileName());
        assertEquals("Decorates", response.description());
        assertEquals("ACTIVE", response.profileStatus());
        verify(profileRepository).save(any(UserProfile.class));
    }

    @Test
    void createRejectsBlankName() {
        assertThrows(IllegalArgumentException.class,
                () -> service.create(new UserProfileRequest("   ", "x")));
        verify(profileRepository, never()).save(any(UserProfile.class));
    }

    @Test
    void createRejectsDuplicateName() {
        when(profileRepository.findByProfileName("Customer"))
                .thenReturn(Optional.of(profile("Customer", "", ProfileStatus.ACTIVE)));

        assertThrows(ConflictException.class,
                () -> service.create(new UserProfileRequest("Customer", "x")));
        verify(profileRepository, never()).save(any(UserProfile.class));
    }

    // ---------- view ----------

    @Test
    void viewReturnsTheProfile() {
        when(profileRepository.findById(1L))
                .thenReturn(Optional.of(profile("Customer", "Searches", ProfileStatus.ACTIVE)));

        UserProfileResponse response = service.view(1L);

        assertEquals("Customer", response.profileName());
        assertEquals("ACTIVE", response.profileStatus());
    }

    @Test
    void viewThrowsWhenProfileDoesNotExist() {
        when(profileRepository.findById(9L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> service.view(9L));
    }

    // ---------- search ----------

    @Test
    void searchWithoutKeywordReturnsAllProfiles() {
        when(profileRepository.findAll(any(Sort.class))).thenReturn(List.of(
                profile("Customer", "a", ProfileStatus.ACTIVE),
                profile("User Admin", "b", ProfileStatus.ACTIVE)));

        assertEquals(2, service.search(null).size());
        assertEquals(2, service.search("   ").size());
    }

    @Test
    void searchWithKeywordUsesTheTrimmedKeyword() {
        when(profileRepository.findByProfileNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                eq("design"), eq("design"), any(Sort.class)))
                .thenReturn(List.of(profile("Interior Designer", "Designs", ProfileStatus.ACTIVE)));

        List<UserProfileResponse> result = service.search("  design ");

        assertEquals(1, result.size());
        assertEquals("Interior Designer", result.get(0).profileName());
    }

    // ---------- update ----------

    @Test
    void updateChangesNameAndDescription() {
        UserProfile existing = profile("Customer", "old", ProfileStatus.ACTIVE);
        when(profileRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(profileRepository.findByProfileName("Home Owner")).thenReturn(Optional.empty());
        when(profileRepository.save(any(UserProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        UserProfileResponse response = service.update(1L, new UserProfileRequest("Home Owner", "new"));

        assertEquals("Home Owner", response.profileName());
        assertEquals("new", response.description());
    }

    @Test
    void updateAllowsKeepingTheSameName() {
        UserProfile existing = profile("Customer", "old", ProfileStatus.ACTIVE);
        UserProfile sameProfile = mock(UserProfile.class);
        when(sameProfile.getProfileID()).thenReturn(1L);
        when(profileRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(profileRepository.findByProfileName("Customer")).thenReturn(Optional.of(sameProfile));
        when(profileRepository.save(any(UserProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        UserProfileResponse response = service.update(1L, new UserProfileRequest("Customer", "new text"));

        assertEquals("new text", response.description());
    }

    @Test
    void updateRejectsANameUsedByAnotherProfile() {
        UserProfile existing = profile("Customer", "old", ProfileStatus.ACTIVE);
        UserProfile other = mock(UserProfile.class);
        when(other.getProfileID()).thenReturn(2L);
        when(profileRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(profileRepository.findByProfileName("Interior Designer")).thenReturn(Optional.of(other));

        assertThrows(ConflictException.class,
                () -> service.update(1L, new UserProfileRequest("Interior Designer", "x")));
        verify(profileRepository, never()).save(any(UserProfile.class));
    }

    @Test
    void updateRejectsBlankName() {
        when(profileRepository.findById(1L))
                .thenReturn(Optional.of(profile("Customer", "old", ProfileStatus.ACTIVE)));

        assertThrows(IllegalArgumentException.class,
                () -> service.update(1L, new UserProfileRequest(" ", "x")));
        verify(profileRepository, never()).save(any(UserProfile.class));
    }

    @Test
    void updateThrowsWhenProfileDoesNotExist() {
        when(profileRepository.findById(9L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> service.update(9L, new UserProfileRequest("Name", "x")));
    }

    // ---------- suspend / activate ----------

    @Test
    void suspendChangesTheStatusToSuspended() {
        when(profileRepository.findById(2L))
                .thenReturn(Optional.of(profile("Customer", "", ProfileStatus.ACTIVE)));
        when(profileRepository.save(any(UserProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        UserProfileResponse response = service.changeStatus(2L, new ProfileStatusRequest("SUSPENDED"));

        assertEquals("SUSPENDED", response.profileStatus());
    }

    @Test
    void activateChangesTheStatusToActive() {
        when(profileRepository.findById(2L))
                .thenReturn(Optional.of(profile("Customer", "", ProfileStatus.SUSPENDED)));
        when(profileRepository.save(any(UserProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        UserProfileResponse response = service.changeStatus(2L, new ProfileStatusRequest("ACTIVE"));

        assertEquals("ACTIVE", response.profileStatus());
    }

    @Test
    void changeStatusRejectsAnInvalidStatus() {
        assertThrows(IllegalArgumentException.class,
                () -> service.changeStatus(2L, new ProfileStatusRequest("DELETED")));
        verifyNoInteractions(profileRepository);
    }

    @Test
    void userAdminProfileCannotBeSuspended() {
        when(profileRepository.findById(1L))
                .thenReturn(Optional.of(profile("User Admin", "", ProfileStatus.ACTIVE)));

        assertThrows(ConflictException.class,
                () -> service.changeStatus(1L, new ProfileStatusRequest("SUSPENDED")));
        verify(profileRepository, never()).save(any(UserProfile.class));
    }

    @Test
    void changeStatusThrowsWhenProfileDoesNotExist() {
        when(profileRepository.findById(9L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> service.changeStatus(9L, new ProfileStatusRequest("SUSPENDED")));
    }
}