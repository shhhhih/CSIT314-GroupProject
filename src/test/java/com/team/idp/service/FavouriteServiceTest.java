package com.team.idp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.team.idp.dto.FavouriteItemResponse;
import com.team.idp.dto.FavouriteResponse;
import com.team.idp.entity.Favourite;
import com.team.idp.entity.IDP;
import com.team.idp.repository.FavouriteRepository;
import com.team.idp.repository.IDPRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class FavouriteServiceTest {

    @Mock
    private FavouriteRepository favouriteRepository;

    @Mock
    private IDPRepository idpRepository;

    @InjectMocks
    private FavouriteService favouriteService;

    private IDP idp(Long id) {
        IDP idp = new IDP(1L, 2L, "Modern Kitchen", "Warm timber", new BigDecimal("15000.00"),
                "Condo", "Completed");
        ReflectionTestUtils.setField(idp, "idpID", id);
        return idp;
    }

    private Favourite favourite(Long favouriteId, Long customerId, Long idpId) {
        Favourite favourite = new Favourite(customerId, idpId);
        ReflectionTestUtils.setField(favourite, "favouriteID", favouriteId);
        favourite.setSavedAt(LocalDateTime.of(2026, 10, 25, 10, 0));
        return favourite;
    }

    //US17 Save IDP To Favourites

    @Test
    void saveToFavourites_newFavourite_savesAndIncreasesShortlistCount() {
        IDP idp = idp(5L);
        when(idpRepository.findById(5L)).thenReturn(Optional.of(idp));
        when(favouriteRepository.existsByCustomerIDAndIdpID(3L, 5L)).thenReturn(false);
        when(favouriteRepository.save(any(Favourite.class))).thenReturn(favourite(10L, 3L, 5L));

        FavouriteResponse response = favouriteService.saveToFavourites(3L, 5L);

        assertEquals(10L, response.favouriteId());
        assertEquals(5L, response.idpId());
        assertEquals(1, idp.getShortlistCount());
        verify(idpRepository).save(idp);
    }

    @Test
    void saveToFavourites_missingIdpId_throwsBadRequest() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> favouriteService.saveToFavourites(3L, null));

        assertEquals("IDP id is required", error.getMessage());
        verifyNoInteractions(idpRepository, favouriteRepository);
    }

    @Test
    void saveToFavourites_missingIDP_throwsNotFound() {
        when(idpRepository.findById(99L)).thenReturn(Optional.empty());

        NoSuchElementException error = assertThrows(NoSuchElementException.class,
                () -> favouriteService.saveToFavourites(3L, 99L));

        assertEquals("IDP not found", error.getMessage());
        verify(favouriteRepository, never()).save(any());
    }

    @Test
    void saveToFavourites_alreadySaved_throwsConflict() {
        IDP idp = idp(5L);
        when(idpRepository.findById(5L)).thenReturn(Optional.of(idp));
        when(favouriteRepository.existsByCustomerIDAndIdpID(3L, 5L)).thenReturn(true);

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> favouriteService.saveToFavourites(3L, 5L));

        assertEquals("You already saved this IDP", error.getMessage());
        assertEquals(0, idp.getShortlistCount());
        verify(favouriteRepository, never()).save(any());
    }

    //US18 View Favourites List

    @Test
    void getFavouritesList_returnsSavedIDPs() {
        when(favouriteRepository.findByCustomerIDOrderBySavedAtDesc(3L))
                .thenReturn(List.of(favourite(10L, 3L, 5L)));
        when(idpRepository.findById(5L)).thenReturn(Optional.of(idp(5L)));

        List<FavouriteItemResponse> list = favouriteService.getFavouritesList(3L);

        assertEquals(1, list.size());
        assertEquals(10L, list.get(0).favouriteId());
        assertEquals(5L, list.get(0).idp().idpId());
    }

    @Test
    void getFavouritesList_noFavourites_returnsEmptyList() {
        when(favouriteRepository.findByCustomerIDOrderBySavedAtDesc(3L)).thenReturn(List.of());

        assertTrue(favouriteService.getFavouritesList(3L).isEmpty());
    }

    @Test
    void getFavouritesList_skipsDeletedIDP() {
        when(favouriteRepository.findByCustomerIDOrderBySavedAtDesc(3L))
                .thenReturn(List.of(favourite(10L, 3L, 5L)));
        when(idpRepository.findById(5L)).thenReturn(Optional.empty());

        assertTrue(favouriteService.getFavouritesList(3L).isEmpty());
    }
}
