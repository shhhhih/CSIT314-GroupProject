
package com.team.idp.controller;

import com.team.idp.entity.Category;
import com.team.idp.entity.IDP;
import com.team.idp.repository.CategoryRepository;
import com.team.idp.service.CreateIDPService;
import com.team.idp.service.IDPImageService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/idps")
public class CreateIDPController {

    private final CreateIDPService createIDPService;
    private final CategoryRepository categoryRepository;
    private final IDPImageService imageService;

    public CreateIDPController(
            CreateIDPService createIDPService,
            CategoryRepository categoryRepository,
            IDPImageService imageService) {

        this.createIDPService = createIDPService;
        this.categoryRepository = categoryRepository;
        this.imageService = imageService;
    }

    // US6: Get available IDP categories
    @GetMapping("/categories")
    public List<Category> getCategoryList() {
        return categoryRepository.findAll();
    }

    // US6: Upload optional project images
    @PostMapping(
            value = "/images",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public List<String> uploadProjectImages(
            @RequestParam("files") List<MultipartFile> files,
            HttpServletRequest request) {

        Long userID = getAuthenticatedDesignerID(request);

        return imageService.uploadProjectImages(files, userID);
    }

    // US6: Create an Interior Design Project
    @PostMapping
    public ResponseEntity<?> createIDP(
            @RequestBody IDP idp,
            HttpServletRequest request) {

        Long userID;

        try {
            userID = getAuthenticatedDesignerID(request);

        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(Map.of(
                            "message",
                            e.getReason() == null
                                    ? "Access denied"
                                    : e.getReason()
                    ));
        }

        try {
            boolean created = createIDPService.createIDP(
                    idp, userID);

            if (created) {
                return ResponseEntity
                        .status(HttpStatus.CREATED)
                        .body(Map.of(
                                "success", true,
                                "message", "Project created successfully"
                        ));
            }

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "Unable to create project"
                    ));

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message", e.getMessage()
                    ));

        } catch (IllegalStateException e) {

            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(Map.of(
                            "message", e.getMessage()
                    ));
        }
    }

    // Check that the user is a logged-in designer
    private Long getAuthenticatedDesignerID(
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
                || !isDesigner(profileName)) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only Interior Designers can create IDPs"
            );
        }

        return (Long) session.getAttribute("userId");
    }

    private boolean isDesigner(String profileName) {

        return profileName.equalsIgnoreCase("Interior Designer")
                || profileName.equalsIgnoreCase("Designer");
    }

    // Handle invalid upload data
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleValidationError(
            IllegalArgumentException exception) {

        return ResponseEntity.badRequest()
                .body(Map.of(
                        "message", exception.getMessage()
                ));
    }
}




