package com.team.idp.service;

import com.team.idp.dto.IdpDetailsResponse;
import com.team.idp.dto.IdpSummaryResponse;
import com.team.idp.entity.IDP;
import com.team.idp.entity.IdpView;
import com.team.idp.repository.IDPRepository;
import com.team.idp.repository.IDPSearchRepository;
import com.team.idp.repository.IdpViewRepository;
import java.util.List;
import java.math.BigDecimal;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** US13 Search IDPs and US15 View IDP Details (Customer). */
@Service
public class CustomerIDPService {

    private final IDPRepository idpRepository;
    private final IDPSearchRepository idpSearchRepository;
    private final IdpViewRepository idpViewRepository;

    public CustomerIDPService(IDPRepository idpRepository,
                              IDPSearchRepository idpSearchRepository,
                              IdpViewRepository idpViewRepository) {
        this.idpRepository = idpRepository;
        this.idpSearchRepository = idpSearchRepository;
        this.idpViewRepository = idpViewRepository;
    }

    /** US13: blank keyword returns all IDPs; otherwise partial, case-insensitive match. Empty list if none. */
    @Transactional(readOnly = true)
    public List<IdpSummaryResponse> searchIDPs(String keyword) {
        List<IDP> results = (keyword == null || keyword.isBlank())
                ? idpSearchRepository.findAllByOrderByCreatedAtDesc()
                : idpSearchRepository.searchByKeyword(keyword.trim());
        return results.stream().map(IdpSummaryResponse::from).toList();
    
        
    }

    /** Search IDPs using optional keyword, category, property type and budget filters. */
    @Transactional(readOnly = true)
    public List<IdpSummaryResponse> searchIDPsWithFilters(
        String keyword,
        Long categoryId,
        String propertyType,
        BigDecimal minBudget,
        BigDecimal maxBudget) {

    String cleanKeyword = (keyword == null || keyword.isBlank())
            ? null : keyword.trim();

    String cleanPropertyType = (propertyType == null || propertyType.isBlank())
            ? null : propertyType.trim();

    if (minBudget != null && minBudget.signum() < 0) {
        throw new IllegalArgumentException("Minimum budget cannot be negative");
    }

    if (maxBudget != null && maxBudget.signum() < 0) {
        throw new IllegalArgumentException("Maximum budget cannot be negative");
    }

    if (minBudget != null && maxBudget != null
            && minBudget.compareTo(maxBudget) > 0) {
        throw new IllegalArgumentException(
                "Minimum budget cannot exceed maximum budget");
    }

    List<IDP> results = idpSearchRepository.searchWithFilters(
            cleanKeyword, categoryId, cleanPropertyType, minBudget, maxBudget);

    return results.stream()
            .map(IdpSummaryResponse::from)
            .toList();
}

    /** Guest search: only returns published IDPs. */
    @Transactional(readOnly = true)
    public List<IdpSummaryResponse> searchPublishedIDPsWithFilters(
            String keyword,
            Long categoryId,
            String propertyType,
            BigDecimal minBudget,
            BigDecimal maxBudget) {

        String cleanKeyword = (keyword == null || keyword.isBlank())
                ? null : keyword.trim();

        String cleanPropertyType = (propertyType == null || propertyType.isBlank())
                ? null : propertyType.trim();

        if (minBudget != null && minBudget.signum() < 0) {
            throw new IllegalArgumentException("Minimum budget cannot be negative");
        }

        if (maxBudget != null && maxBudget.signum() < 0) {
            throw new IllegalArgumentException("Maximum budget cannot be negative");
        }

        if (minBudget != null && maxBudget != null
                && minBudget.compareTo(maxBudget) > 0) {
            throw new IllegalArgumentException(
                    "Minimum budget cannot exceed maximum budget");
        }

        List<IDP> results = idpSearchRepository.searchPublishedWithFilters(
                cleanKeyword,
                categoryId,
                cleanPropertyType,
                minBudget,
                maxBudget
        );

        return results.stream()
                .map(IdpSummaryResponse::from)
                .toList();
    }



    /** US15: returns the details, adds 1 to viewCount and stores a dated view record. */
    @Transactional
    public IdpDetailsResponse viewIDPDetails(Long idpId, Long customerId) {
        IDP idp = idpRepository.findById(idpId)
                .orElseThrow(() -> new NoSuchElementException("IDP not found"));

        idp.setViewCount(idp.getViewCount() + 1);               // Increase View Count <<include>>
        IDP saved = idpRepository.save(idp);
        idpViewRepository.save(new IdpView(idpId, customerId)); // dated record for the daily report

        return IdpDetailsResponse.from(saved);
    }
}
