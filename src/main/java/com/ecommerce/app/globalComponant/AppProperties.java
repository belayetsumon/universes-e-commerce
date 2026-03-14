/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Component.java to edit this template
 */
package com.ecommerce.app.globalComponant;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 *
 * @author libertyerp_local
 */
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private String name;
    private String header;
    private String footer;

    private ImageProperties image = new ImageProperties();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getFooter() {
        return footer;
    }

    public void setFooter(String footer) {
        this.footer = footer;
    }

    public ImageProperties getImage() {
        return image;
    }

    public void setImage(ImageProperties image) {
        this.image = image;
    }

    public static class ImageProperties {

        private String watermarkText;
        private float watermarkOpacity;
        private String watermarkPosition;

        // Getters & Setters
        public String getWatermarkText() {
            return watermarkText;
        }

        public void setWatermarkText(String watermarkText) {
            this.watermarkText = watermarkText;
        }

        public float getWatermarkOpacity() {
            return watermarkOpacity;
        }

        public void setWatermarkOpacity(float watermarkOpacity) {
            this.watermarkOpacity = watermarkOpacity;
        }

        public String getWatermarkPosition() {
            return watermarkPosition;
        }

        public void setWatermarkPosition(String watermarkPosition) {
            this.watermarkPosition = watermarkPosition;
        }
    }
}
