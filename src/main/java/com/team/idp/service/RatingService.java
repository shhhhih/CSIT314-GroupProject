package com.team.idp.service;

import com.team.idp.dto.IdpRatingsResponse;
import com.team.idp.dto.RatingRequest;
import com.team.idp.dto.RatingResponse;
import com.team.idp.entity.Rating;
import com.team.idp.repository.IDPRepository;
import com.team.idp.repository.RatingRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Customer: Rate IDP, and View IDP Ratings. */
@Service
public class RatingService {

    static final int MAX_COMMENT_LENGTH = 500;

    private final RatingRepository ratingRepository;
    private final IDPRepository idpRepository;

    public RatingService(RatingRepository ratingRepository, IDPRepository idpRepository) {
        this.ratingRepository = ratingRepository;
        this.idpRepository = idpRepository;
    }

    /**
     * Saves the customer's rating of an IDP. If they rated it before, their old rating is replaced.
     * @throws IllegalArgumentException missing IDP id, stars not 1 to 5, or comment too long (400)
     * @throws NoSuchElementException   IDP does not exist (404)
     */
    @Transactional
    public RatingResponse rateIDP(Long customerId, RatingRequest request) {
        if (request == null || request.idpId() == null) {
            throw new IllegalArgumentException("IDP id is required");
        }
        Integer stars = request.stars();
        if (stars == null || stars < 1 || stars > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5 stars");
        }
        String comment = request.comment() == null || request.comment().isBlank() ? null : request.comment().trim();
        if (comment != null && comment.length() > MAX_COMMENT_LENGTH) {
            throw new IllegalArgumentException("Comment must be 500 characters or less");
        }
        if (!idpRepository.existsById(request.idpId())) {
            throw new NoSuchElementException("IDP not found");
        }

        Rating rating = ratingRepository.findByCustomerIDAndIdpID(customerId, request.idpId())
                .orElseGet(() -> new Rating(customerId, request.idpId(), stars, comment));
        rating.setStars(stars);       // for an existing rating, replace the old stars and comment
        rating.setComment(comment);
        rating.setRatedAt(LocalDateTime.now());
        return RatingResponse.from(ratingRepository.save(rating));
    }

    /** All ratings of an IDP with the average (rounded to one decimal; 0.0 when nobody has rated it). */
    @Transactional(readOnly = true)
    public IdpRatingsResponse getIDPRatings(Long idpId) {
        if (!idpRepository.existsById(idpId)) {
            throw new NoSuchElementException("IDP not found");
        }
        List<Rating> ratings = ratingRepository.findByIdpIDOrderByRatedAtDesc(idpId);
        double average = ratings.stream().mapToInt(Rating::getStars).average().orElse(0.0);
        double rounded = Math.round(average * 10) / 10.0;
        return new IdpRatingsResponse(idpId, rounded, ratings.size(),
                ratings.stream().map(RatingResponse::from).toList());
    }
}
