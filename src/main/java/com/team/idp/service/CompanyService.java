package com.team.idp.service;

import com.team.idp.entity.Company;
import com.team.idp.repository.CompanyRepository;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.web.multipart.MultipartFile;

@Service
public class CompanyService {

    private final CompanyRepository companyRepository;

    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    // Create company information
    @Transactional
    public Company createCompany(Long userID, Company company) {

        validateCompany(company);

        if (companyRepository.findByUserID(userID).isPresent()) {
            throw new IllegalArgumentException(
                    "You have already created company information");
        }

        company.setUserID(userID);

        return companyRepository.save(company);
    }

    // View company information belonging to a designer
    public Optional<Company> getMyCompany(Long userID) {
        return companyRepository.findByUserID(userID);
    }

    // View company information by company ID
    public Company getCompanyById(Long companyID) {

        return companyRepository.findById(companyID)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Company information not found"));
    }

    // Update existing company information
    @Transactional
    public Company updateCompany(Long userID, Company updatedCompany) {

        validateCompany(updatedCompany);

        Company existingCompany = companyRepository.findByUserID(userID)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Company information not found"));

        existingCompany.setCompanyName(updatedCompany.getCompanyName());
        existingCompany.setDescription(updatedCompany.getDescription());
        existingCompany.setEmail(updatedCompany.getEmail());
        existingCompany.setPhone(updatedCompany.getPhone());
        existingCompany.setAddress(updatedCompany.getAddress());
        existingCompany.setWebsite(updatedCompany.getWebsite());

        return companyRepository.save(existingCompany);
    }

    // Validate required company information
    private void validateCompany(Company company) {

        if (company == null
                || isBlank(company.getCompanyName())
                || isBlank(company.getDescription())
                || isBlank(company.getEmail())
                || isBlank(company.getPhone())
                || isBlank(company.getAddress())) {

            throw new IllegalArgumentException(
                    "Please fill in all required fields");
        }

        if (!company.getEmail().matches(
                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {

            throw new IllegalArgumentException(
                    "Please fill in all required fields with valid details");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
    // Upload company logo
    @Transactional
    public Company uploadCompanyLogo(Long userID, MultipartFile file) throws IOException {

    // Find the designer's company
    Company company = companyRepository.findByUserID(userID)
            .orElseThrow(() ->
                    new IllegalArgumentException(
                            "Company information not found"));

    // Check if a file was selected
    if (file == null || file.isEmpty()) {
        throw new IllegalArgumentException(
                "Please select a logo to upload");
    }

    // Check whether the file is larger than 5 MB
    if (file.getSize() > 5 * 1024 * 1024) {
        throw new IllegalArgumentException(
                "Logo must be a JPG or PNG file under 5 MB");
    }

    // Check the file extension
    String filename = file.getOriginalFilename();

    if (filename == null ||
            !(filename.toLowerCase().endsWith(".jpg")
            || filename.toLowerCase().endsWith(".jpeg")
            || filename.toLowerCase().endsWith(".png"))) {

        throw new IllegalArgumentException(
                "Logo must be a JPG or PNG file under 5 MB");
    }

    // Create a folder for company logos
    Path folder = Paths.get("uploads/company-logos");

    Files.createDirectories(folder);

    // Give the uploaded file a name
    String extension = filename.substring(filename.lastIndexOf('.')).toLowerCase();
    String newFilename = "company_" + userID + "_" + System.currentTimeMillis() + extension;
    // Save the image
    Path filePath = folder.resolve(newFilename);

    file.transferTo(filePath);

    // Save the image path in the database
    company.setLogoPath(filePath.toString().replace("\\", "/"));

    return companyRepository.save(company);
}
}

