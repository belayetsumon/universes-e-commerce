/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Service.java to edit this template
 */
/**
 *
 * @author libertyerp_local
 */
package com.ecommerce.app.globalServices;

import com.ecommerce.app.services.StorageProperties;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;
import java.util.UUID;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ImageService {

    @Autowired
    private StorageProperties storageProperties;

    public String validateAndRename(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IOException("File is empty");
        }

        // Check content type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IOException("File is not an image");
        }

        // Generate unique filename with webp extension
        return UUID.randomUUID().toString() + ".webp";
    }

    /**
     * Validates a restricted image upload before it is converted to WebP.
     * MIME type, filename extension, and decoded image format must all match
     * the supplied policy.
     */
    public String validateAndRename(MultipartFile file, ImageUploadPolicy policy) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new ImageUploadValidationException("Please select an image file to upload.");
        }
        if (file.getSize() > policy.maxFileSizeBytes()) {
            throw new ImageUploadValidationException("The image exceeds the "
                    + formatFileSize(policy.maxFileSizeBytes()) + " upload limit.");
        }

        String contentType = normalize(file.getContentType());
        if (!policy.allowedContentTypes().contains(contentType)) {
            throw unsupportedType(policy);
        }

        String extension = extractExtension(file.getOriginalFilename());
        if (!policy.allowedExtensions().contains(extension)) {
            throw unsupportedType(policy);
        }

        validateDecodedImage(file, policy);
        return UUID.randomUUID().toString() + ".webp";
    }

    /**
     * Resize image and upload to the target directory
     *
     * @param file
     * @param width
     * @param height
     * @param subDir
     * @param fileName
     * @return
     * @throws java.io.IOException
     */
    public String resizeAndUpload(MultipartFile file, int width, int height, String subDir, String fileName) throws IOException {
        BufferedImage originalImage = ImageIO.read(file.getInputStream());
        if (originalImage == null) {
            throw new IOException("Invalid image file");
        }

        // Create target directory if not exists
        File dir = new File(storageProperties.getRootPath(), subDir);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Failed to create directory: " + dir.getAbsolutePath());
        }

        // Resize and save
        Thumbnails.of(originalImage)
                .size(width, height)
                .outputFormat("webp")
                .toFile(new File(dir, fileName));

        return fileName;
    }

    public String resizeAndUploadHighQualityWebp(MultipartFile file,
            ImageUploadPolicy policy,
            int maxWidth,
            int maxHeight,
            String subDir) throws IOException {
        String fileName = validateAndRename(file, policy);
        return resizeAndUploadHighQualityWebp(file, maxWidth, maxHeight, subDir, fileName);
    }

    public String resizeAndUploadHighQualityWebp(MultipartFile file,
            int maxWidth,
            int maxHeight,
            String subDir,
            String fileName) throws IOException {
        if (maxWidth <= 0 || maxHeight <= 0) {
            throw new IOException("Image resize dimensions must be valid.");
        }

        BufferedImage originalImage = ImageIO.read(file.getInputStream());
        if (originalImage == null) {
            throw new IOException("Invalid image file");
        }

        File dir = new File(storageProperties.getRootPath(), subDir == null ? "" : subDir);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Failed to create directory: " + dir.getAbsolutePath());
        }

        Thumbnails.of(originalImage)
                .size(maxWidth, maxHeight)
                .keepAspectRatio(true)
                .outputQuality(1.0)
                .outputFormat("webp")
                .toFile(new File(dir, fileName));

        return fileName;
    }

    private void validateDecodedImage(MultipartFile file, ImageUploadPolicy policy) throws IOException {
        try (ImageInputStream imageInput = ImageIO.createImageInputStream(file.getInputStream())) {
            if (imageInput == null) {
                throw new ImageUploadValidationException("The uploaded file could not be read as an image.");
            }

            Iterator<ImageReader> readers = ImageIO.getImageReaders(imageInput);
            if (!readers.hasNext()) {
                throw new ImageUploadValidationException("The uploaded file is not a valid image.");
            }

            ImageReader reader = readers.next();
            try {
                String format = normalize(reader.getFormatName());
                if (!policy.allowedImageFormats().contains(format)) {
                    throw unsupportedType(policy);
                }

                reader.setInput(imageInput, true, true);
                int width = reader.getWidth(0);
                int height = reader.getHeight(0);
                if (width <= 0 || height <= 0 || width > policy.maxWidth() || height > policy.maxHeight()) {
                    throw new ImageUploadValidationException("Image dimensions must not exceed "
                            + policy.maxWidth() + " x " + policy.maxHeight() + " pixels.");
                }
            } finally {
                reader.dispose();
            }
        }
    }

    private ImageUploadValidationException unsupportedType(ImageUploadPolicy policy) {
        return new ImageUploadValidationException("Only " + policy.allowedFileDescription() + " files are allowed.");
    }

    private String extractExtension(String originalFilename) {
        if (originalFilename == null) {
            return "";
        }

        int extensionStart = originalFilename.lastIndexOf('.');
        if (extensionStart < 0 || extensionStart == originalFilename.length() - 1) {
            return "";
        }

        return normalize(originalFilename.substring(extensionStart + 1));
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private String formatFileSize(long bytes) {
        long megabytes = bytes / (1024 * 1024);
        return megabytes > 0 ? megabytes + " MB" : bytes + " bytes";
    }
}
