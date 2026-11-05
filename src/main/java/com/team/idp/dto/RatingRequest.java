package com.team.idp.dto;

/** Body of POST /api/ratings: { "idpId": 5, "stars": 4, "comment": "Love the lighting" } (comment optional) */
public record RatingRequest(Long idpId, Integer stars, String comment) {
}
