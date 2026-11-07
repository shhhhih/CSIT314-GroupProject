
package com.team.idp.controller;

import com.team.idp.entity.Company;
import com.team.idp.entity.IDP;
import com.team.idp.repository.CompanyRepository;
import com.team.idp.repository.IDPRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.util.List;
import java.util.Locale;

import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/designer/idps")
public class ViewMyIDPsController {

    private final IDPRepository idpRepository;
    private final CompanyRepository companyRepository;

    public ViewMyIDPsController(
            IDPRepository idpRepository,
            CompanyRepository companyRepository) {

        this.idpRepository = idpRepository;
        this.companyRepository = companyRepository;
    }

    // US7: Display the logged-in designer's projects
    @GetMapping
    public List<IDP> displayMyIDPs(
            @RequestParam(required = false) String sortBy,
            HttpServletRequest request) {

        Long companyID = getAuthenticatedCompanyID(request);

        if (sortBy != null && !sortBy.isBlank()) {
            return sortMyIDPs(companyID, sortBy);
        }

        return getMyIDPs(companyID);
    }

    // US7 UML: getMyIDPs(companyID)
    public List<IDP> getMyIDPs(Long companyID) {

        return idpRepository.findByCompanyID(
                companyID,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
    }

    // US7 UML: sortMyIDPs(companyID, sortBy)
    public List<IDP> sortMyIDPs(
            Long companyID,
            String sortBy) {

        String option = sortBy.trim()
                .toLowerCase(Locale.ROOT);

        Sort sort;

        switch (option) {

            case "date", "newest" ->
                    sort = Sort.by(
                            Sort.Direction.DESC,
                            "createdAt"
                    );

            case "oldest" ->
                    sort = Sort.by(
                            Sort.Direction.ASC,
                            "createdAt"
                    );

            case "title" ->
                    sort = Sort.by(
                            Sort.Direction.ASC,
                            "title"
                    );

            default -> throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid sort option"
            );
        }

        return idpRepository.findByCompanyID(
                companyID, sort);
    }

    // US7: View a specific project's details
    @GetMapping("/{idpID}")
    public IDP getIDP(
            @PathVariable Long idpID,
            HttpServletRequest request) {

        Long companyID = getAuthenticatedCompanyID(request);

        return idpRepository
                .findByIdpIDAndCompanyID(idpID, companyID)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Project not found"
                ));
    }

    // Get company ID from authenticated designer session
    private Long getAuthenticatedCompanyID(
            HttpServletRequest request) {

        HttpSession session = request.getSession(false);

        if (session == null
                || !(session.getAttribute("userId") instanceof Long)) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Please log in first"
            );
        }

        Object profile = session.getAttribute("profileName");

        if (!(profile instanceof String profileName)
                || !(profileName.equalsIgnoreCase("Interior Designer")
                || profileName.equalsIgnoreCase("Designer"))) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only Interior Designers can view their IDPs"
            );
        }

        Long userID = (Long) session.getAttribute("userId");

        Company company = companyRepository.findByUserID(userID)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Designer company information does not exist"
                ));

        return company.getCompanyID();
    }
}

