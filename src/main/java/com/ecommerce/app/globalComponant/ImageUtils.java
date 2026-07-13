/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Component.java to edit this template
 */
package com.ecommerce.app.globalComponant;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import javax.imageio.ImageIO;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 * @author libertyerp_local
 */
@Component
public class ImageUtils {

    String BASE_FOLDER = Paths.get(System.getProperty("user.home"), "universesecommerce").toString();
    // Max file size in bytes (e.g., 50 MB)
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    private static final Set<String> ALLOWED_BANNER_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");
    private static final Set<String> ALLOWED_BANNER_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/webp");
    private static final Map<String, Integer> IMAGE_SIZES = Map.of(
            "thumb", 100,
            "medium", 300,
            "large", 600
    );

    public String multipleImageSizeSave(MultipartFile file, Long userId, String imgDirName) throws IOException {

        // 1️⃣ Validate file
        String originalFilename = Objects.requireNonNull(file.getOriginalFilename());
        String extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();

        if (!List.of(".jpg", ".jpeg", ".png").contains(extension)) {
            throw new IOException("Invalid file type. Only JPG, JPEG, PNG allowed.");
        }

        // 2️⃣ Ensure user/media folder exists
        Path basePath = Paths.get(BASE_FOLDER, imgDirName);
        if (!Files.exists(basePath)) {
            Files.createDirectories(basePath);
        }

        // 3️⃣ Generate unique file name
        String fileName = UUID.randomUUID().toString() + extension;

        // 4️⃣ Save original file
        Path originalPath = basePath.resolve(fileName);
        file.transferTo(originalPath.toFile());

        // 5️⃣ Resize images
        for (Map.Entry<String, Integer> entry : IMAGE_SIZES.entrySet()) {
            String folderName = entry.getKey();
            int size = entry.getValue();

            Path dirPath = basePath.resolve(folderName);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }

            File outputPath = dirPath.resolve(fileName).toFile();

            Thumbnails.of(originalPath.toFile())
                    .size(size, size)
                    .keepAspectRatio(true)
                    .toFile(outputPath);
        }

        return fileName;
    }

    public String multipleImageSizeSaveConvertWebp(MultipartFile file, Long userId, String imgDirName) throws IOException {

        // 1️⃣ Validate file
        String originalFilename = Objects.requireNonNull(file.getOriginalFilename());
        String extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();

        if (!List.of(".jpg", ".jpeg", ".png").contains(extension)) {
            throw new IOException("Invalid file type. Only JPG, JPEG, PNG allowed.");
        }

        if (file.getSize() > 2_000_000) { // 2MB limit
            throw new IOException("File size exceeds limit (2MB).");
        }

        // 2️⃣ Ensure user/media folder exists
        Path basePath = Paths.get(BASE_FOLDER, imgDirName);
        if (!Files.exists(basePath)) {
            Files.createDirectories(basePath);
        }

        // 3️⃣ Generate unique WebP file name
        String fileName = UUID.randomUUID().toString() + ".webp";

        // 4️⃣ Save original file as WebP
        Path originalPath = basePath.resolve(fileName);
        Thumbnails.of(file.getInputStream())
                .size(800, 800) // optional max size for original
                .outputFormat("webp")
                .toFile(originalPath.toFile());

        // 5️⃣ Resize images and save as WebP
        for (Map.Entry<String, Integer> entry : IMAGE_SIZES.entrySet()) {
            String folderName = entry.getKey();
            int size = entry.getValue();

            Path dirPath = basePath.resolve(folderName);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }

            File outputPath = dirPath.resolve(fileName).toFile();

            Thumbnails.of(originalPath.toFile())
                    .size(size, size)
                    .keepAspectRatio(true)
                    .outputFormat("webp")
                    .toFile(outputPath);
        }

        return fileName;
    }

    public String saveBannerImage(MultipartFile file, String imgDirPath,
            int width, int height) throws IOException {

        // 1️⃣ Check empty file
        if (file.isEmpty()) {
            throw new IOException("File is empty.");
        }

        // 2️⃣ Check file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IOException("File exceeds maximum size of " + (MAX_FILE_SIZE / (1024 * 1024)) + " MB.");
        }

        // 1️⃣ Validate file
        if (width <= 0 || height <= 0) {
            throw new IOException("Banner width and height must be valid before upload.");
        }

        String extension = extractExtension(file.getOriginalFilename());
        String contentType = normalize(file.getContentType());

        if (!ALLOWED_BANNER_EXTENSIONS.contains(extension)
                || !ALLOWED_BANNER_CONTENT_TYPES.contains(contentType)) {
            throw new IOException("Invalid file type. Only JPG, JPEG, PNG, and WEBP images are allowed.");
        }

        // 2️⃣ Prepare output directory (NIO safe)
        Path basePath = Paths.get(BASE_FOLDER, imgDirPath);
        if (Files.notExists(basePath)) {
            Files.createDirectories(basePath);
        }

        // 3️⃣ Generate UUID filename
        String uuidName = UUID.randomUUID().toString() + ".webp";

        // Correct file path creation
        Path outputPath = basePath.resolve(uuidName);
        File outputFile = outputPath.toFile();

        // 4️⃣ Read input image (NO need for ImageWriter check — Thumbnailator handles it)
        BufferedImage inputImage = ImageIO.read(file.getInputStream());
        if (inputImage == null) {
            throw new IOException("Invalid image file.");
        }

        // 5️⃣ Resize + save safely
        Thumbnails.of(inputImage)
                .forceSize(width, height)
                .outputFormat("webp")
                .allowOverwrite(true)
                .toFile(outputFile);

        return uuidName;
    }

    private String extractExtension(String originalFilename) throws IOException {
        String safeFilename = Objects.requireNonNullElse(originalFilename, "").trim();
        int extensionStart = safeFilename.lastIndexOf('.');
        if (extensionStart < 0 || extensionStart == safeFilename.length() - 1) {
            throw new IOException("Image file must include a JPG, PNG, or WEBP extension.");
        }
        return normalize(safeFilename.substring(extensionStart + 1));
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

}
