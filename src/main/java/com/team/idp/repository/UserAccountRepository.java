
package com.team.idp.repository;

import com.team.idp.entity.UserAccount;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAccountRepository
        extends JpaRepository<UserAccount, Long> {

    long countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(
            LocalDateTime start,
            LocalDateTime end
    );

    Optional<UserAccount> findByUsername(String username);

    Optional<UserAccount> findByEmail(String email);

    // Count customers or interior designers by their profile ID
    long countByProfileID(Long profileID);
}
