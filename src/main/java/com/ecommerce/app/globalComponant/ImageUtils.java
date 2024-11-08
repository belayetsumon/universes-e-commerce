/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Component.java to edit this template
 */
package com.ecommerce.app.globalComponant;

import org.springframework.stereotype.Component;

/**
 *
 * @author libertyerp_local
 */
@Component
public class ImageUtils {
    
  /*
    public void processAndUploadImage(MultipartFile file, String uploadUrl) throws Exception {
        // Step 1: Load the image from MultipartFile
        BufferedImage image = ImageIO.read(file.getInputStream());

        // Step 2: Resize the image (example: 800x600)
        BufferedImage resizedImage = resizeImage(image, 800, 600);

        // Step 3: Generate new filename with UNIX timestamp and size
        long timestamp = System.currentTimeMillis() / 1000;
        long fileSize = file.getSize();
        String newFileName = timestamp + "_" + fileSize + ".webp";

        // Step 4: Convert to WebP format and save to disk
        Path outputPath = Paths.get(System.getProperty("java.io.tmpdir"), newFileName);
        File webpFile = convertToWebP(resizedImage, outputPath);

        // Step 5: Upload the image to the server
        uploadImageToServer(webpFile, uploadUrl);
    }

    // Resize the image
    private BufferedImage resizeImage(BufferedImage originalImage, int width, int height) {
        Image scaledImage = originalImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        BufferedImage resizedImage = new BufferedImage(width, height, originalImage.getType());
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(scaledImage, 0, 0, null);
        g.dispose();
        return resizedImage;
    }

    // Convert the image to WebP format (Assuming you have WebP-Java or external cwebp utility)
    private File convertToWebP(BufferedImage image, Path outputPath) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(outputPath.toFile())) {
            // Assuming you have WebP library available here
            WebP.write(image, fos);  // Use a WebP conversion library
        }
        return outputPath.toFile();
    }

    // Upload the image to the server using HttpClient
    private void uploadImageToServer(File imageFile, String uploadUrl) throws IOException {
        HttpPost uploadFile = new HttpPost(uploadUrl);

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addPart("file", new FileBody(imageFile));
        HttpEntity multipart = builder.build();
        uploadFile.setEntity(multipart);

        HttpResponse response = HttpClients.createDefault().execute(uploadFile);
        System.out.println("Server response: " + response.getStatusLine());
    }
    */
}
