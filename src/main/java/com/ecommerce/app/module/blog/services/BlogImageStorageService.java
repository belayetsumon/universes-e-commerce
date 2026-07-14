package com.ecommerce.app.module.blog.services;

import com.ecommerce.app.globalServices.ImageService;
import com.ecommerce.app.globalServices.ImageUploadPolicy;
import java.io.IOException;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class BlogImageStorageService {

    private static final long MAX_UPLOAD_SIZE_BYTES = 10L * 1024L * 1024L;
    private static final int MAX_SOURCE_WIDTH = 8000;
    private static final int MAX_SOURCE_HEIGHT = 8000;
    private static final int FEATURED_IMAGE_MAX_WIDTH = 1440;
    private static final int FEATURED_IMAGE_MAX_HEIGHT = 900;
    private static final String BLOG_IMAGE_DIRECTORY = "blog";
    private static final String BLOG_IMAGE_URL_PREFIX = "/files/" + BLOG_IMAGE_DIRECTORY + "/";

    private static final ImageUploadPolicy BLOG_FEATURED_IMAGE_POLICY = new ImageUploadPolicy(
            Set.of("image/jpeg", "image/png", "image/webp"),
            Set.of("jpg", "jpeg", "png", "webp"),
            Set.of("jpeg", "png", "webp"),
            MAX_UPLOAD_SIZE_BYTES,
            MAX_SOURCE_WIDTH,
            MAX_SOURCE_HEIGHT,
            "JPG, PNG, or WEBP images"
    );

    private final ImageService imageService;

    public BlogImageStorageService(ImageService imageService) {
        this.imageService = imageService;
    }

    public String storeFeaturedImage(MultipartFile file) throws IOException {
        if (!hasFile(file)) {
            return null;
        }
        String fileName = imageService.resizeAndUploadHighQualityWebp(
                file,
                BLOG_FEATURED_IMAGE_POLICY,
                FEATURED_IMAGE_MAX_WIDTH,
                FEATURED_IMAGE_MAX_HEIGHT,
                BLOG_IMAGE_DIRECTORY
        );
        return BLOG_IMAGE_URL_PREFIX + fileName;
    }

    public boolean hasFile(MultipartFile file) {
        return file != null && !file.isEmpty();
    }
}
