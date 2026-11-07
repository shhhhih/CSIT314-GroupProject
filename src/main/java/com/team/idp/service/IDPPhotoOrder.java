package com.team.idp.service;

import com.team.idp.entity.IDPImage;
import java.util.Comparator;
import java.util.List;

/** Shared ordering: chosen cover first, then upload order. */
public final class IDPPhotoOrder {
    private IDPPhotoOrder() { }

    public static List<IDPImage> coverFirst(List<IDPImage> images) {
        return images.stream()
                .sorted(Comparator.comparing(IDPImage::isCover).reversed()
                        .thenComparing(IDPImage::getImageID))
                .toList();
    }
}
