package com.team.idp.repository;

import com.team.idp.entity.Favourite;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FavouriteRepository extends JpaRepository<Favourite, Long> {

    /** US17: stop the same customer saving the same IDP twice. */
    boolean existsByCustomerIDAndIdpID(Long customerID, Long idpID);

    /** US18: my favourites, newest first. */
    List<Favourite> findByCustomerIDOrderBySavedAtDesc(Long customerID);

    /** US22 (daily report, B2): shortlists saved between two times. */
    long countBySavedAtBetween(LocalDateTime start, LocalDateTime end);
}
