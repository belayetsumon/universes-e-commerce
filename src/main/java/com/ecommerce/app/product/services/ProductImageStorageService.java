package com.ecommerce.app.product.services;

import com.ecommerce.app.globalServices.ImageService;
import com.ecommerce.app.globalServices.ImageUploadPolicy;
import com.ecommerce.app.module.settings.dto.ImageUploadSettingsSnapshot;
import com.ecommerce.app.module.settings.services.ImageUploadSettingsService;
import com.ecommerce.app.product.dto.ProductFeaturedImageRequirements;
import java.io.IOException;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ProductImageStorageService {

    private static final long MAX_UPLOAD_SIZE_BYTES = 10L * 1024L * 1024L;
    private static final int MAX_SOURCE_WIDTH = 8000;
    private static final int MAX_SOURCE_HEIGHT = 8000;
    private static final int GALLERY_IMAGE_MAX_WIDTH = 800;
    private static final int GALLERY_IMAGE_MAX_HEIGHT = 600;
    private static final int CATEGORY_IMAGE_MAX_WIDTH = 300;
    private static final int CATEGORY_IMAGE_MAX_HEIGHT = 225;
    private static final String ROOT_IMAGE_DIRECTORY = "";

    private static final ImageUploadPolicy GENERAL_PRODUCT_IMAGE_POLICY = new ImageUploadPolicy(
            Set.of("image/jpeg", "image/png", "image/webp"),
            Set.of("jpg", "jpeg", "png", "webp"),
            Set.of("jpeg", "png", "webp"),
            MAX_UPLOAD_SIZE_BYTES,
            MAX_SOURCE_WIDTH,
            MAX_SOURCE_HEIGHT,
            "JPG, PNG, or WEBP images"
    );

    private final ImageService imageService;
    private final ImageUploadSettingsService imageUploadSettingsService;

    public ProductImageStorageService(
            ImageService imageService,
            ImageUploadSettingsService imageUploadSettingsService) {
        this.imageService = imageService;
        this.imageUploadSettingsService = imageUploadSettingsService;
    }

    public String storeProductImage(MultipartFile file) throws IOException {
        return imageService.resizeAndUploadHighQualityWebp(
                file,
                GENERAL_PRODUCT_IMAGE_POLICY,
                GALLERY_IMAGE_MAX_WIDTH,
                GALLERY_IMAGE_MAX_HEIGHT,
                ROOT_IMAGE_DIRECTORY
        );
    }

    public String storeFeaturedProductImage(MultipartFile file) throws IOException {
        ImageUploadSettingsSnapshot settings = imageUploadSettingsService.getCurrentSettings();
        return imageService.resizeAndUploadHighQualityWebp(
                file,
                imageUploadSettingsService.productFeaturedImagePolicy(settings),
                settings.getProductFeaturedImageOutputMaxWidth(),
                settings.getProductFeaturedImageOutputMaxHeight(),
                ROOT_IMAGE_DIRECTORY
        );
    }

    public ProductFeaturedImageRequirements getFeaturedImageRequirements() {
        return new ProductFeaturedImageRequirements(imageUploadSettingsService.getCurrentSettings());
    }

    public String storeCategoryImage(MultipartFile file) throws IOException {
        return imageService.resizeAndUploadHighQualityWebp(
                file,
                GENERAL_PRODUCT_IMAGE_POLICY,
                CATEGORY_IMAGE_MAX_WIDTH,
                CATEGORY_IMAGE_MAX_HEIGHT,
                ROOT_IMAGE_DIRECTORY
        );
    }

}
