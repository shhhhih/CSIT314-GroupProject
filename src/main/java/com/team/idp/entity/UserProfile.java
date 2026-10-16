package com.team.idp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_profiles")
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long profileID;

    @Column(nullable = false, unique = true)
    private String profileName;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProfileStatus profileStatus;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public UserProfile() {}

    public UserProfile(String profileName, String description, ProfileStatus profileStatus) {
        this.profileName = profileName;
        this.description = description;
        this.profileStatus = profileStatus;
    }

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public Long getProfileID() { return profileID; }
    public String getProfileName() { return profileName; }
    public void setProfileName(String profileName) { this.profileName = profileName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public ProfileStatus getProfileStatus() { return profileStatus; }
    public void setProfileStatus(ProfileStatus profileStatus) { this.profileStatus = profileStatus; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}