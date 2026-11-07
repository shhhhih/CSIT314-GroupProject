package com.team.idp.repository;

import com.team.idp.entity.Rating;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RatingRepository extends JpaRepository<Rating, Long> {

    /** The customer's existing rating of this IDP, if they rated it before. */
    Optional<Rating> findByCustomerIDAndIdpID(Long customerID, Long idpID);

    /** All ratings of an IDP, newest first. */
    List<Rating> findByIdpIDOrderByRatedAtDesc(Long idpID);
}
