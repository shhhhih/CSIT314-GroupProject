package com.team.idp.dto;

import com.team.idp.entity.UserAccount;
import com.team.idp.entity.UserProfile;
import java.time.LocalDateTime;

public record ProfileResponse(Long userId, String username, String email,
                              String role, String accountStatus, LocalDateTime createdAt) {
    public static ProfileResponse from(UserAccount account, UserProfile role) {
        return new ProfileResponse(account.getUserID(), account.getUsername(), account.getEmail(),
                role.getProfileName(), account.getAccountStatus().name(), account.getCreatedAt());
    }
}
