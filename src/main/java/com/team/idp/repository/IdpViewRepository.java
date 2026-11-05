package com.team.idp.repository;

import com.team.idp.entity.IdpView;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IdpViewRepository extends JpaRepository<IdpView, Long> {

    java.util.List<IdpView> findByViewedAtGreaterThanEqualAndViewedAtLessThan(java.time.LocalDateTime start, java.time.LocalDateTime end);

    java.util.List<IdpView> findByIdpID(Long idpID);

    /** US22 (daily report, B2): views recorded between two times. */
    long countByViewedAtBetween(LocalDateTime start, LocalDateTime end);
}
