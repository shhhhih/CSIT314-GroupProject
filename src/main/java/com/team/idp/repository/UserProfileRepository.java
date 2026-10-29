package com.team.idp.repository;

import java.util.List;
import org.springframework.data.domain.Sort;
import com.team.idp.entity.UserProfile;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    Optional<UserProfile> findByProfileName(String profileName);
    List<UserProfile> findByProfileNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String profileName, String description, Sort sort);
}