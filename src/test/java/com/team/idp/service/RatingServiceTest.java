package com.team.idp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.team.idp.dto.IdpRatingsResponse;
import com.team.idp.dto.RatingRequest;
import com.team.idp.dto.RatingResponse;
import com.team.idp.entity.Rating;
import com.team.idp.repository.IDPRepository;
import com.team.idp.repository.RatingRepository;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RatingServiceTest {

    @Mock
    private RatingRepository ratingRepository;

    @Mock
    private IDPRepository idpRepository;

    @InjectMocks
    private RatingService ratingService;

    //Rate IDP

    @Test
    void rateIDP_firstRating_savesNewRating() {
        when(idpRepository.existsById(5L)).thenReturn(true);
        when(ratingRepository.findByCustomerIDAndIdpID(3L, 5L)).thenReturn(Optional.empty());
        when(ratingRepository.save(any(Rating.class))).thenAnswer(call -> call.getArgument(0));

        RatingResponse response = ratingService.rateIDP(3L, new RatingRequest(5L, 4, "  Love the lighting  "));

        assertEquals(5L, response.idpId());
        assertEquals(4, response.stars());
        assertEquals("Love the lighting", response.comment());
    }

    @Test
    void rateIDP_ratedBefore_replacesOldRating() {
        Rating old = new Rating(3L, 5L, 2, "Too dark");
        when(idpRepository.existsById(5L)).thenReturn(true);
        when(ratingRepository.findByCustomerIDAndIdpID(3L, 5L)).thenReturn(Optional.of(old));
        when(ratingRepository.save(old)).thenReturn(old);

        RatingResponse response = ratingService.rateIDP(3L, new RatingRequest(5L, 5, null));

        assertEquals(5, response.stars());
        assertNull(response.comment());
        verify(ratingRepository).save(old);
    }

    @Test
    void rateIDP_missingIdpId_throwsBadRequest() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> ratingService.rateIDP(3L, new RatingRequest(null, 4, null)));

        assertEquals("IDP id is required", error.getMessage());
        verifyNoInteractions(ratingRepository, idpRepository);
    }

    @Test
    void rateIDP_starsOutOfRange_throwsBadRequest() {
        for (Integer stars : new Integer[] {null, 0, 6}) {
            IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                    () -> ratingService.rateIDP(3L, new RatingRequest(5L, stars, null)));
            assertEquals("Rating must be between 1 and 5 stars", error.getMessage());
        }
        verifyNoInteractions(ratingRepository);
    }

    @Test
    void rateIDP_commentTooLong_throwsBadRequest() {
        String longComment = "a".repeat(501);

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> ratingService.rateIDP(3L, new RatingRequest(5L, 4, longComment)));

        assertEquals("Comment must be 500 characters or less", error.getMessage());
    }

    @Test
    void rateIDP_missingIDP_throwsNotFound() {
        when(idpRepository.existsById(99L)).thenReturn(false);

        NoSuchElementException error = assertThrows(NoSuchElementException.class,
                () -> ratingService.rateIDP(3L, new RatingRequest(99L, 4, null)));

        assertEquals("IDP not found", error.getMessage());
        verify(ratingRepository, never()).save(any());
    }

    //View IDP Ratings

    @Test
    void getIDPRatings_returnsAverageRoundedToOneDecimal() {
        when(idpRepository.existsById(5L)).thenReturn(true);
        when(ratingRepository.findByIdpIDOrderByRatedAtDesc(5L)).thenReturn(List.of(
                new Rating(1L, 5L, 5, null), new Rating(2L, 5L, 4, null), new Rating(3L, 5L, 4, null)));

        IdpRatingsResponse response = ratingService.getIDPRatings(5L);

        assertEquals(4.3, response.averageStars());
        assertEquals(3, response.ratingCount());
        assertEquals(3, response.ratings().size());
    }

    @Test
    void getIDPRatings_noRatings_returnsZero() {
        when(idpRepository.existsById(5L)).thenReturn(true);
        when(ratingRepository.findByIdpIDOrderByRatedAtDesc(5L)).thenReturn(List.of());

        IdpRatingsResponse response = ratingService.getIDPRatings(5L);

        assertEquals(0.0, response.averageStars());
        assertEquals(0, response.ratingCount());
        assertTrue(response.ratings().isEmpty());
    }

    @Test
    void getIDPRatings_missingIDP_throwsNotFound() {
        when(idpRepository.existsById(99L)).thenReturn(false);

        assertThrows(NoSuchElementException.class, () -> ratingService.getIDPRatings(99L));
    }
}
