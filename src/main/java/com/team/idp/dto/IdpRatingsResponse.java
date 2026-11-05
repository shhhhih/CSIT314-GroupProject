package com.team.idp.dto;

import java.util.List;

/** GET /api/ratings/idp/{idpId}: average stars (one decimal, 0.0 if none), how many ratings, and the ratings. */
public record IdpRatingsResponse(Long idpId, double averageStars, int ratingCount, List<RatingResponse> ratings) {
}
