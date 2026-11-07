
package com.team.idp.controller;

import com.team.idp.dto.IdpSummaryResponse;
import com.team.idp.service.CustomerIDPService;
import com.team.idp.service.IDPResponseEnricher;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/guest/idps")
public class GuestIDPController {

    private final CustomerIDPService customerIDPService;
    private final IDPResponseEnricher enricher;

    public GuestIDPController(CustomerIDPService customerIDPService, IDPResponseEnricher enricher) {
        this.customerIDPService = customerIDPService;
        this.enricher = enricher;
    }

    @GetMapping
    public List<IdpSummaryResponse> searchIDPs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String propertyType,
            @RequestParam(required = false) BigDecimal minBudget,
            @RequestParam(required = false) BigDecimal maxBudget) {

        return customerIDPService.searchPublishedIDPsWithFilters(
                keyword,
                categoryId,
                propertyType,
                minBudget,
                maxBudget
        ).stream().map(enricher::summary).toList();
    }
}
