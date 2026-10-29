package com.team.idp.service;

import com.team.idp.dto.ProfileStatusRequest;
import com.team.idp.dto.UserProfileRequest;
import com.team.idp.dto.UserProfileResponse;
import com.team.idp.repository.UserProfileRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ProfileManagementService {

    private final UserProfileRepository profileRepository;

    public ProfileManagementService(UserProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    public UserProfileResponse create(UserProfileRequest request) {
        throw new UnsupportedOperationException("TODO");
    }

    public UserProfileResponse view(Long profileId) {
        throw new UnsupportedOperationException("TODO");
    }

    public List<UserProfileResponse> search(String keyword) {
        throw new UnsupportedOperationException("TODO");
    }

    public UserProfileResponse update(Long profileId, UserProfileRequest request) {
        throw new UnsupportedOperationException("TODO");
    }

    public UserProfileResponse changeStatus(Long profileId, ProfileStatusRequest request) {
        throw new UnsupportedOperationException("TODO");
    }
}