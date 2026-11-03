package com.team.idp.controller;

import com.team.idp.dto.DesignerIdpRequest;
import com.team.idp.service.DesignerIDPService;
import com.team.idp.service.IDPImageService;
import java.io.IOException;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.Map;
import java.util.NoSuchElementException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/designer/idps")
public class DesignerIDPController {
    private final DesignerIDPService service;
    private final IDPImageService imageService;
    public DesignerIDPController(DesignerIDPService service, IDPImageService imageService) {
        this.service = service;
        this.imageService = imageService;
    }

    private Long designer(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || !(session.getAttribute("userId") instanceof Long id)
                || !"Interior Designer".equals(session.getAttribute("profileName")))
            throw new SecurityException("Please log in as an Interior Designer");
        return id;
    }

    @GetMapping
    public Object list(@RequestParam(defaultValue="date") String sort,
                       @RequestParam(defaultValue="desc") String order, HttpServletRequest request) {
        return service.list(designer(request),sort,order);
    }
    @GetMapping("/{id}")
    public Object view(@PathVariable Long id,HttpServletRequest request) {
        return service.view(designer(request),id);
    }
    @PostMapping
    public ResponseEntity<?> create(@RequestBody DesignerIdpRequest body,HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(designer(request),body));
    }
    @PutMapping("/{id}")
    public Object update(@PathVariable Long id,@RequestBody DesignerIdpRequest body,HttpServletRequest request) {
        return service.update(designer(request),id,body);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id,HttpServletRequest request) {
        service.delete(designer(request),id);
        return ResponseEntity.noContent().build();
    }
    @PostMapping("/{id}/images")
    public Object upload(@PathVariable Long id, @RequestParam("images") List<MultipartFile> images,
                         HttpServletRequest request) throws IOException {
        return Map.of("images", imageService.upload(designer(request), id, images));
    }
    @GetMapping("/{id}/images")
    public List<com.team.idp.entity.IDPImage> getImages(
            @PathVariable Long id, HttpServletRequest request) {
        return imageService.getProjectImages(designer(request), id);
    }

    @DeleteMapping("/{id}/images/{imageId}")
    public ResponseEntity<Void> deleteImage(
            @PathVariable Long id, @PathVariable Long imageId, HttpServletRequest request) {
        imageService.deleteImage(designer(request), id, imageId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/images/{imageId}/cover")
    public ResponseEntity<Map<String, String>> setCoverImage(
            @PathVariable Long id, @PathVariable Long imageId, HttpServletRequest request) {
        imageService.setCoverImage(designer(request), id, imageId);
        return ResponseEntity.ok(Map.of("message", "Cover photo updated"));
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<?> uploadFailure(IOException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Unable to store image"));
    }
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> invalid(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
    }
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<?> missing(NoSuchElementException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message",e.getMessage()));
    }
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<?> missingCompany(IllegalStateException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message",e.getMessage()));
    }
    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<?> forbidden(SecurityException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message",e.getMessage()));
    }
}
