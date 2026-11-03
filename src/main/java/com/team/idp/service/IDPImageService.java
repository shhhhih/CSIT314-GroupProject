package com.team.idp.service;

import com.team.idp.entity.IDPImage;
import com.team.idp.repository.IDPImageRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class IDPImageService {
    private final IDPImageRepository images;
    private final DesignerIDPService designer;
    public IDPImageService(IDPImageRepository images, DesignerIDPService designer) {
        this.images = images;
        this.designer = designer;
    }

    @Transactional
    public List<String> upload(Long designerUserId, Long idpId, List<MultipartFile> files) throws IOException {
        designer.view(designerUserId, idpId); // enforce ownership before any file is written
        if (files == null || files.isEmpty() || files.size() + images.countByIdpID(idpId) > 10)
            throw new IllegalArgumentException("Upload between 1 and 10 images per IDP");
        // Inspect content signature, not only the supplied filename or content type.
        List<String> extensions = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty() || file.getSize() > 5L * 1024 * 1024)
                throw new IllegalArgumentException("Images must be PNG or JPG under 5 MB");
            byte[] first = file.getBytes();
            boolean png = first.length >= 8 && (first[0]&255)==137 && (first[1]&255)==80 &&
                    (first[2]&255)==78 && (first[3]&255)==71 && (first[4]&255)==13 &&
                    (first[5]&255)==10 && (first[6]&255)==26 && (first[7]&255)==10;
            boolean jpg = first.length >= 3 && (first[0]&255)==255 &&
                    (first[1]&255)==216 && (first[2]&255)==255;
            if (!png && !jpg) throw new IllegalArgumentException("Images must be PNG or JPG under 5 MB");
            extensions.add(png ? ".png" : ".jpg");
        }
        Path folder = Paths.get("uploads", "idps", idpId.toString());
        Files.createDirectories(folder);
        for (int index = 0; index < files.size(); index++) {
            String filename = UUID.randomUUID().toString() + extensions.get(index);
            Files.write(folder.resolve(filename),files.get(index).getBytes());
            images.save(new IDPImage(idpId,"/uploads/idps/"+idpId+"/"+filename));
        }
        return IDPPhotoOrder.coverFirst(images.findByIdpIDOrderByImageIDAsc(idpId))
                .stream().map(IDPImage::getImageUrl).toList();
    }

    @Transactional(readOnly = true)
    public List<IDPImage> getProjectImages(Long designerUserId, Long idpId) {
        designer.view(designerUserId, idpId); // ownership check
        return IDPPhotoOrder.coverFirst(images.findByIdpIDOrderByImageIDAsc(idpId));
    }

    @Transactional
    public void setCoverImage(Long designerUserId, Long idpId, Long imageId) {
        designer.view(designerUserId, idpId); // ownership check
        IDPImage selected = images.findById(imageId)
                .orElseThrow(() -> new java.util.NoSuchElementException("Image not found"));
        if (!idpId.equals(selected.getIdpID()))
            throw new java.util.NoSuchElementException("Image not found");

        // Only one cover per project; this is persisted in MySQL.
        List<IDPImage> projectPhotos = images.findByIdpIDOrderByImageIDAsc(idpId);
        for (IDPImage photo : projectPhotos) {
            photo.setCover(photo.getImageID().equals(imageId));
        }
        images.saveAll(projectPhotos);
    }

    @Transactional
    public void deleteImage(Long designerUserId, Long idpId, Long imageId) {
        designer.view(designerUserId, idpId); // ownership check
        IDPImage image = images.findById(imageId)
                .orElseThrow(() -> new java.util.NoSuchElementException("Image not found"));
        if (!idpId.equals(image.getIdpID()))
            throw new java.util.NoSuchElementException("Image not found");

        boolean wasCover = image.isCover();
        images.delete(image);
        images.flush();
        if (wasCover) {
            // Choose a remaining photo so a deleted cover is never referenced.
            images.findByIdpIDOrderByImageIDAsc(idpId).stream().findFirst()
                    .ifPresent(next -> { next.setCover(true); images.save(next); });
        }
        // Intentionally keep the file on disk for safe rollback / manual cleanup.
    }
}
