package com.team.idp.dto;

import java.time.LocalDateTime;

/** One row of GET /api/favourites: { favouriteId, savedAt, idp: {IDP summary} } */
public record FavouriteItemResponse(Long favouriteId, LocalDateTime savedAt, IdpSummaryResponse idp) {
}
