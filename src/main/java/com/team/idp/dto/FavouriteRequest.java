package com.team.idp.dto;

/** Body of POST /api/favourites: { "idpId": 5 } */
public record FavouriteRequest(Long idpId) {
}
