package com.ecommerce.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ecommerce.app.globalServices.ImageService;
import com.ecommerce.app.globalServices.ImageUploadPolicy;
import com.ecommerce.app.globalServices.ImageUploadValidationException;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Set;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

class ImageServiceUploadPolicyTest {

    private final ImageService imageService = new ImageService();

    @Test
    void rejectsImageBelowConfiguredMinimumFileSize() {
        MockMultipartFile image = new MockMultipartFile(
                "pic", "small.png", "image/png", new byte[9]);

        ImageUploadValidationException exception = assertThrows(
                ImageUploadValidationException.class,
                () -> imageService.validateAndRename(image, policy(10, 1_000_000, 1, 1, 100, 100))
        );

        assertEquals("The image must be at least 10 bytes.", exception.getMessage());
    }

    @Test
    void rejectsImageBelowConfiguredMinimumDimensions() throws IOException {
        MockMultipartFile image = pngImage(39, 30);

        ImageUploadValidationException exception = assertThrows(
                ImageUploadValidationException.class,
                () -> imageService.validateAndRename(image, policy(1, 1_000_000, 40, 30, 100, 100))
        );

        assertEquals("Image dimensions must be at least 40 x 30 pixels.", exception.getMessage());
    }

    @Test
    void acceptsImageAtExactConfiguredDimensionBoundaries() throws IOException {
        MockMultipartFile image = pngImage(40, 30);

        String storedName = imageService.validateAndRename(
                image,
                policy(1, image.getSize(), 40, 30, 40, 30)
        );

        assertTrue(storedName.endsWith(".webp"));
    }

    @Test
    void rejectsInvalidPolicyRangesBeforeUploadProcessing() {
        assertThrows(
                IllegalArgumentException.class,
                () -> policy(20, 10, 1, 1, 100, 100)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> policy(1, 10, 101, 1, 100, 100)
        );
    }

    private ImageUploadPolicy policy(
            long minBytes,
            long maxBytes,
            int minWidth,
            int minHeight,
            int maxWidth,
            int maxHeight) {
        return new ImageUploadPolicy(
                Set.of("image/png"),
                Set.of("png"),
                Set.of("png"),
                minBytes,
                maxBytes,
                minWidth,
                minHeight,
                maxWidth,
                maxHeight,
                "PNG images"
        );
    }

    private MockMultipartFile pngImage(int width, int height) throws IOException {
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "png", output);
        return new MockMultipartFile("pic", "featured.png", "image/png", output.toByteArray());
    }
}
