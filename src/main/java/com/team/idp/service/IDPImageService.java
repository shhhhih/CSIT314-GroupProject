
package com.team.idp.service;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class IDPImageService {

    private static final long MAX_SIZE = 5 * 1024 * 1024;
    private static final int MAX_IMAGES = 5;

    private final Path uploadDirectory =
            Paths.get("uploads").toAbsolutePath().normalize();

    public List<String> uploadProjectImages(
            List<MultipartFile> files, Long userID) {

        if (userID == null) {
            throw new IllegalArgumentException(
                    "User ID is required");
        }

        if (files == null || files.isEmpty()
                || files.size() > MAX_IMAGES) {
            throw new IllegalArgumentException(
                    "Please upload between 1 and 5 images");
        }

        List<String> imagePaths = new ArrayList<>();
        List<Path> writtenFiles = new ArrayList<>();
        boolean successful = false;

        try {
            Path userDirectory = uploadDirectory
                    .resolve(userID.toString());

            Files.createDirectories(userDirectory);

            if (Files.isSymbolicLink(userDirectory)) {
                throw new IllegalArgumentException(
                        "Invalid upload directory");
            }

            for (MultipartFile file : files) {

                // Validate file size
                if (file == null || file.isEmpty()
                        || file.getSize() > MAX_SIZE) {
                    throw new IllegalArgumentException(
                            "Each image must be between 1 byte and 5 MB");
                }

                // Accept JPG and PNG only
                String contentType = file.getContentType();

                if (!"image/jpeg".equalsIgnoreCase(contentType)
                        && !"image/png".equalsIgnoreCase(contentType)) {
                    throw new IllegalArgumentException(
                            "Only JPG and PNG images are allowed");
                }

                String extension;

                // Verify the file signature
                try (InputStream input = file.getInputStream()) {

                    byte[] header = input.readNBytes(8);

                    boolean jpeg = header.length >= 3
                            && (header[0] & 0xff) == 0xff
                            && (header[1] & 0xff) == 0xd8
                            && (header[2] & 0xff) == 0xff;

                    boolean png = header.length == 8
                            && (header[0] & 0xff) == 0x89
                            && header[1] == 'P'
                            && header[2] == 'N'
                            && header[3] == 'G'
                            && (header[4] & 0xff) == 0x0d
                            && (header[5] & 0xff) == 0x0a
                            && (header[6] & 0xff) == 0x1a
                            && (header[7] & 0xff) == 0x0a;

                    if (jpeg && "image/jpeg".equalsIgnoreCase(contentType)) {
                        extension = ".jpg";

                    } else if (png && "image/png".equalsIgnoreCase(contentType)) {
                        extension = ".png";

                    } else {
                        throw new IllegalArgumentException(
                                "Invalid JPG or PNG image");
                    }
                }

                // Check that the image is readable
                BufferedImage decoded;

                try (InputStream input = file.getInputStream()) {
                    decoded = ImageIO.read(input);
                }

                if (decoded == null) {
                    throw new IllegalArgumentException(
                            "Invalid image content");
                }

                // Generate a unique filename
                String filename = UUID.randomUUID() + extension;
                Path destination = userDirectory.resolve(filename);

                // Save the image to disk
                try (InputStream input = file.getInputStream()) {
                    Files.copy(input, destination);
                }

                writtenFiles.add(destination);

                // Return the stored path
                imagePaths.add(
                        "/uploads/" + userID + "/" + filename);
            }

            successful = true;
            return imagePaths;

        } catch (IOException e) {

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable to save project images",
                    e);

        } finally {

            // Remove partial uploads if an error occurs
            if (!successful) {
                for (Path path : writtenFiles) {
                    try {
                        Files.deleteIfExists(path);
                    } catch (IOException ignored) {
                        // Best-effort cleanup
                    }
                }
            }
        }
    }
}



