
package com.team.idp.service;

import com.team.idp.entity.Company;
import com.team.idp.entity.IDP;
import com.team.idp.repository.CategoryRepository;
import com.team.idp.repository.CompanyRepository;
import com.team.idp.repository.IDPRepository;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateIDPService {

    private final IDPRepository idpRepository;
    private final CompanyRepository companyRepository;
    private final CategoryRepository categoryRepository;

    public CreateIDPService(
            IDPRepository idpRepository,
            CompanyRepository companyRepository,
            CategoryRepository categoryRepository) {

        this.idpRepository = idpRepository;
        this.companyRepository = companyRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public boolean createIDP(IDP idp, Long userID) {

        if (idp == null || userID == null) {
            throw new IllegalArgumentException(
                    "Project information is required");
        }

        if (idp.getTitle() == null
                || idp.getTitle().isBlank()
                || idp.getDescription() == null
                || idp.getDescription().isBlank()) {

            throw new IllegalArgumentException(
                    "Please fill in all required fields with a valid budget");
        }

        BigDecimal budget = idp.getBudget();

        if (budget == null
                || budget.compareTo(BigDecimal.ZERO) <= 0
                || budget.scale() > 2
                || budget.precision() - budget.scale() > 10) {

            throw new IllegalArgumentException(
                    "Please fill in all required fields with a valid budget");
        }

        if (idp.getCategoryID() == null
                || !categoryRepository.existsById(idp.getCategoryID())) {

            throw new IllegalArgumentException(
                    "Please select a valid IDP category");
        }

        String propertyType = validatePropertyType(
                idp.getPropertyType());

        String projectStatus = validateProjectStatus(
                idp.getProjectStatus());

        Company company = companyRepository.findByUserID(userID)
                .orElseThrow(() -> new IllegalStateException(
                        "Designer company information does not exist"));

        IDP newIDP = new IDP(
                company.getCompanyID(),
                idp.getCategoryID(),
                idp.getTitle().trim(),
                idp.getDescription().trim(),
                budget,
                propertyType,
                projectStatus
        );

        newIDP.setViewCount(0);
        newIDP.setShortlistCount(0);

        // US6: Validate optional uploaded images
        List<String> imagePaths = idp.getImages();

        if (imagePaths != null && !imagePaths.isEmpty()) {

            if (imagePaths.size() > 5) {
                throw new IllegalArgumentException(
                        "A maximum of 5 project images is allowed");
            }

            Path ownerDirectory = Paths.get("uploads")
                    .toAbsolutePath()
                    .normalize()
                    .resolve(userID.toString());

            String expectedPrefix = "/uploads/" + userID + "/";

            List<File> uploadedFiles = new ArrayList<>();

            for (String imagePath : imagePaths) {

                if (imagePath == null
                        || !imagePath.startsWith(expectedPrefix)) {
                    throw new IllegalArgumentException(
                            "Invalid project image path");
                }

                String filename = imagePath.substring(
                        expectedPrefix.length());

                if (!filename.matches(
                        "[0-9a-fA-F]{8}-"
                        + "[0-9a-fA-F]{4}-"
                        + "[0-9a-fA-F]{4}-"
                        + "[0-9a-fA-F]{4}-"
                        + "[0-9a-fA-F]{12}\\.(jpg|png)")) {
                    throw new IllegalArgumentException(
                            "Invalid project image filename");
                }

                Path storedFile = ownerDirectory
                        .resolve(filename)
                        .normalize();

                if (!storedFile.startsWith(ownerDirectory)
                        || !Files.isRegularFile(
                                storedFile,
                                LinkOption.NOFOLLOW_LINKS)) {

                    throw new IllegalArgumentException(
                            "Uploaded project image does not exist");
                }

                uploadedFiles.add(storedFile.toFile());
            }

            // Calls the existing method in IDP.java
            newIDP.saveImages(uploadedFiles);
        }

        IDP savedIDP = idpRepository.saveAndFlush(newIDP);

        return savedIDP.getIdpID() != null;
    }

    private String validatePropertyType(String value) {

        if (value == null) {
            throw new IllegalArgumentException(
                    "Property type is required");
        }

        return switch (value.trim().toUpperCase(Locale.ROOT)) {
            case "HDB" -> "HDB";
            case "CONDO" -> "Condo";
            case "LANDED" -> "Landed";
            default -> throw new IllegalArgumentException(
                    "Property type must be HDB, Condo or Landed");
        };
    }

    private String validateProjectStatus(String value) {

        if (value == null) {
            throw new IllegalArgumentException(
                    "Project status is required");
        }

        return switch (value.trim().toUpperCase(Locale.ROOT)) {
            case "CURRENT" -> "Current";
            case "PAST" -> "Past";
            default -> throw new IllegalArgumentException(
                    "Project status must be Current or Past");
        };
    }
}

