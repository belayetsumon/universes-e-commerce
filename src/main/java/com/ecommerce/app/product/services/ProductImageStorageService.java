package com.ecommerce.app.product.services;

import com.ecommerce.app.globalServices.ImageService;
import com.ecommerce.app.globalServices.ImageUploadPolicy;
import java.io.IOException;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ProductImageStorageService {

    private static final long MAX_UPLOAD_SIZE_BYTES = 10L * 1024L * 1024L;
    private static final int MAX_SOURCE_WIDTH = 8000;
    private static final int MAX_SOURCE_HEIGHT = 8000;
    private static final int PRODUCT_IMAGE_MAX_WIDTH = 800;
    private static final int PRODUCT_IMAGE_MAX_HEIGHT = 600;
    private static final int CATEGORY_IMAGE_MAX_WIDTH = 300;
    private static final int CATEGORY_IMAGE_MAX_HEIGHT = 225;
    private static final String ROOT_IMAGE_DIRECTORY = "";

    private static final ImageUploadPolicy PRODUCT_IMAGE_POLICY = new ImageUploadPolicy(
            Set.of("image/jpeg", "image/png", "image/webp"),
            Set.of("jpg", "jpeg", "png", "webp"),
            Set.of("jpeg", "png", "webp"),
            MAX_UPLOAD_SIZE_BYTES,
            MAX_SOURCE_WIDTH,
            MAX_SOURCE_HEIGHT,
            "JPG, PNG, or WEBP images"
    );

    private final ImageService imageService;

    public ProductImageStorageService(ImageService imageService) {
        this.imageService = imageService;
    }

    public String storeProductImage(MultipartFile file) throws IOException {
        return imageService.resizeAndUploadHighQualityWebp(
                file,
                PRODUCT_IMAGE_POLICY,
                PRODUCT_IMAGE_MAX_WIDTH,
                PRODUCT_IMAGE_MAX_HEIGHT,
                ROOT_IMAGE_DIRECTORY
        );
    }

    public String storeCategoryImage(MultipartFile file) throws IOException {
        return imageService.resizeAndUploadHighQualityWebp(
                file,
                PRODUCT_IMAGE_POLICY,
                CATEGORY_IMAGE_MAX_WIDTH,
                CATEGORY_IMAGE_MAX_HEIGHT,
                ROOT_IMAGE_DIRECTORY
        );
    }
}
