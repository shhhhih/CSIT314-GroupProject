package com.team.idp.dto;

import com.team.idp.entity.UserProfile;
import java.time.LocalDateTime;

public record UserProfileResponse(Long profileId, String profileName, String description,
                                  String profileStatus, LocalDateTime createdAt) {

    public static UserProfileResponse from(UserProfile profile) {
        return new UserProfileResponse(profile.getProfileID(), profile.getProfileName(),
                profile.getDescription(), profile.getProfileStatus().name(), profile.getCreatedAt());
    }
}