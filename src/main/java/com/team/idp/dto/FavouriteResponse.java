package com.team.idp.dto;

import com.team.idp.entity.Favourite;
import java.time.LocalDateTime;

/** 201 response of POST /api/favourites. */
public record FavouriteResponse(Long favouriteId, Long idpId, LocalDateTime savedAt) {
    public static FavouriteResponse from(Favourite favourite) {
        return new FavouriteResponse(favourite.getFavouriteID(), favourite.getIdpID(), favourite.getSavedAt());
    }
}
