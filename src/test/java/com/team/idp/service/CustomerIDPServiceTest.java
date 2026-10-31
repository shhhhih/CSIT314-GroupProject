package com.team.idp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.team.idp.dto.IdpDetailsResponse;
import com.team.idp.dto.IdpSummaryResponse;
import com.team.idp.entity.IDP;
import com.team.idp.entity.IdpView;
import com.team.idp.repository.IDPRepository;
import com.team.idp.repository.IDPSearchRepository;
import com.team.idp.repository.IdpViewRepository;
import java.math.BigDecimal;
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
class CustomerIDPServiceTest {

    @Mock
    private IDPRepository idpRepository;

    @Mock
    private IDPSearchRepository idpSearchRepository;

    @Mock
    private IdpViewRepository idpViewRepository;

    @InjectMocks
    private CustomerIDPService customerIDPService;

    private IDP idp(Long id, String title) {
        IDP idp = new IDP(1L, 2L, title, "Warm timber and soft lighting", new BigDecimal("15000.00"),
                "Condo", "Completed");
        ReflectionTestUtils.setField(idp, "idpID", id);   // idpID has no setter (database sets it)
        return idp;
    }

    //US13 Search IDPs

    @Test
    void searchIDPs_blankKeyword_returnsAllIDPs() {
        when(idpSearchRepository.findAllByOrderByCreatedAtDesc())
                .thenReturn(List.of(idp(1L, "Modern Kitchen"), idp(2L, "Cosy Bedroom")));

        List<IdpSummaryResponse> results = customerIDPService.searchIDPs("   ");

        assertEquals(2, results.size());
        verify(idpSearchRepository, never()).searchByKeyword(any());
    }

    @Test
    void searchIDPs_nullKeyword_returnsAllIDPs() {
        when(idpSearchRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(idp(1L, "Modern Kitchen")));

        assertEquals(1, customerIDPService.searchIDPs(null).size());
    }

    @Test
    void searchIDPs_keyword_trimsAndReturnsMatches() {
        when(idpSearchRepository.searchByKeyword("kitchen")).thenReturn(List.of(idp(1L, "Modern Kitchen")));

        List<IdpSummaryResponse> results = customerIDPService.searchIDPs("  kitchen ");

        assertEquals(1, results.size());
        assertEquals(1L, results.get(0).idpId());
        assertEquals("Modern Kitchen", results.get(0).title());
    }

    @Test
    void searchIDPs_noMatch_returnsEmptyList() {
        when(idpSearchRepository.searchByKeyword("zzz")).thenReturn(List.of());

        assertTrue(customerIDPService.searchIDPs("zzz").isEmpty());
    }

    //US15 View IDP Details

    @Test
    void viewIDPDetails_existingIDP_increasesViewCountAndRecordsView() {
        IDP idp = idp(5L, "Modern Kitchen");
        when(idpRepository.findById(5L)).thenReturn(Optional.of(idp));
        when(idpRepository.save(idp)).thenReturn(idp);

        IdpDetailsResponse details = customerIDPService.viewIDPDetails(5L, 3L);

        assertEquals(1, details.viewCount());
        assertEquals("Warm timber and soft lighting", details.description());
        verify(idpViewRepository).save(any(IdpView.class));
    }

    @Test
    void viewIDPDetails_missingIDP_throwsNotFound() {
        when(idpRepository.findById(99L)).thenReturn(Optional.empty());

        NoSuchElementException error = assertThrows(NoSuchElementException.class,
                () -> customerIDPService.viewIDPDetails(99L, 3L));

        assertEquals("IDP not found", error.getMessage());
        verify(idpViewRepository, never()).save(any());
    }
}
