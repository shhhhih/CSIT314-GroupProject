package com.team.idp.dto;

import com.team.idp.entity.Rating;
import java.time.LocalDateTime;

public record RatingResponse(Long ratingId, Long idpId, int stars, String comment, LocalDateTime ratedAt) {
    public static RatingResponse from(Rating rating) {
        return new RatingResponse(rating.getRatingID(), rating.getIdpID(), rating.getStars(),
                rating.getComment(), rating.getRatedAt());
    }
}
