package com.team.idp.service;

import com.team.idp.dto.ProfileStatusRequest;
import com.team.idp.dto.UserProfileRequest;
import com.team.idp.dto.UserProfileResponse;
import com.team.idp.entity.ProfileStatus;
import com.team.idp.entity.UserProfile;
import com.team.idp.repository.UserProfileRepository;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileManagementService {

    private static final String ADMIN_PROFILE = "User Admin";
    private static final Sort BY_ID = Sort.by("profileID");

    private final UserProfileRepository profileRepository;

    public ProfileManagementService(UserProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    @Transactional
    public UserProfileResponse create(UserProfileRequest request) {
        String name = cleanName(request);
        String description = cleanDescription(request);
        if (profileRepository.findByProfileName(name).isPresent()) {
            throw new ConflictException("Profile name already exists");
        }
        UserProfile saved = profileRepository.save(
                new UserProfile(name, description, ProfileStatus.ACTIVE));
        return UserProfileResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public UserProfileResponse view(Long profileId) {
        return UserProfileResponse.from(find(profileId));
    }

    @Transactional(readOnly = true)
    public List<UserProfileResponse> search(String keyword) {
        List<UserProfile> found;
        if (keyword == null || keyword.isBlank()) {
            found = profileRepository.findAll(BY_ID);
        } else {
            String text = keyword.trim();
            found = profileRepository
                    .findByProfileNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                            text, text, BY_ID);
        }
        return found.stream().map(UserProfileResponse::from).toList();
    }

    @Transactional
    public UserProfileResponse update(Long profileId, UserProfileRequest request) {
        UserProfile profile = find(profileId);
        String name = cleanName(request);
        String description = cleanDescription(request);
        profileRepository.findByProfileName(name)
                .filter(other -> !other.getProfileID().equals(profileId))
                .ifPresent(other -> {
                    throw new ConflictException("Profile name already exists");
                });
        profile.setProfileName(name);
        profile.setDescription(description);
        return UserProfileResponse.from(profileRepository.save(profile));
    }

    @Transactional
    public UserProfileResponse changeStatus(Long profileId, ProfileStatusRequest request) {
        ProfileStatus status = parseStatus(request);
        UserProfile profile = find(profileId);
        if (status == ProfileStatus.SUSPENDED && ADMIN_PROFILE.equals(profile.getProfileName())) {
            throw new ConflictException("The User Admin profile cannot be suspended");
        }
        profile.setProfileStatus(status);
        return UserProfileResponse.from(profileRepository.save(profile));
    }

    private UserProfile find(Long profileId) {
        return profileRepository.findById(profileId)
                .orElseThrow(() -> new NoSuchElementException("Profile not found"));
    }

    private String cleanName(UserProfileRequest request) {
        if (request == null || request.profileName() == null || request.profileName().isBlank()) {
            throw new IllegalArgumentException("Profile name is required");
        }
        return request.profileName().trim();
    }

    private String cleanDescription(UserProfileRequest request) {
        return request.description() == null ? "" : request.description().trim();
    }

    private ProfileStatus parseStatus(ProfileStatusRequest request) {
        if (request == null || request.profileStatus() == null) {
            throw new IllegalArgumentException("Invalid profile status");
        }
        try {
            return ProfileStatus.valueOf(request.profileStatus().trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid profile status");
        }
    }
}