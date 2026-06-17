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
import java.util.UUID;
import javax.imageio.ImageIO;
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
}
