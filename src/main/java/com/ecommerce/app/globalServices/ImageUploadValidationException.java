package com.ecommerce.app.globalServices;

import java.io.IOException;

/**
 * A safe, user-facing validation failure for an image upload.
 */
public class ImageUploadValidationException extends IOException {

    public ImageUploadValidationException(String message) {
        super(message);
    }
}
