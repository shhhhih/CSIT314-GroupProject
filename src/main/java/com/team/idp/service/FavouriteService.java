package com.team.idp.service;

import com.team.idp.dto.FavouriteItemResponse;
import com.team.idp.dto.FavouriteResponse;
import com.team.idp.dto.IdpSummaryResponse;
import com.team.idp.entity.Favourite;
import com.team.idp.entity.IDP;
import com.team.idp.repository.FavouriteRepository;
import com.team.idp.repository.IDPRepository;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** US17 Save IDP To Favourites and US18 View Favourites List (Customer). */
@Service
public class FavouriteService {

    private final FavouriteRepository favouriteRepository;
    private final IDPRepository idpRepository;

    public FavouriteService(FavouriteRepository favouriteRepository, IDPRepository idpRepository) {
        this.favouriteRepository = favouriteRepository;
        this.idpRepository = idpRepository;
    }

    /**
     * US17: saves the IDP to the customer's favourites and adds 1 to its shortlistCount.
     * @throws IllegalArgumentException idpId missing (400)
     * @throws NoSuchElementException   IDP does not exist (404)
     * @throws IllegalStateException    already saved (409)
     */
    @Transactional
    public FavouriteResponse saveToFavourites(Long customerId, Long idpId) {
        if (idpId == null) {
            throw new IllegalArgumentException("IDP id is required");
        }
        IDP idp = idpRepository.findById(idpId)
                .orElseThrow(() -> new NoSuchElementException("IDP not found"));
        if (favouriteRepository.existsByCustomerIDAndIdpID(customerId, idpId)) {
            throw new IllegalStateException("You already saved this IDP");
        }

        Favourite saved = favouriteRepository.save(new Favourite(customerId, idpId));
        idp.setShortlistCount(idp.getShortlistCount() + 1);   // Update Shortlist Count <<include>>
        idpRepository.save(idp);

        return FavouriteResponse.from(saved);
    }

    /** US18: the customer's favourites, newest first. Skips any favourite whose IDP was deleted. */
    @Transactional(readOnly = true)
    public List<FavouriteItemResponse> getFavouritesList(Long customerId) {
        return favouriteRepository.findByCustomerIDOrderBySavedAtDesc(customerId).stream()
                .map(fav -> {
                    Optional<IDP> idp = idpRepository.findById(fav.getIdpID());
                    return idp.map(i -> new FavouriteItemResponse(
                            fav.getFavouriteID(), fav.getSavedAt(), IdpSummaryResponse.from(i)));
                })
                .flatMap(Optional::stream)
                .toList();
    }
}
