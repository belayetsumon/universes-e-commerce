/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Service.java to edit this template
 */
package com.ecommerce.app.module.settings.services;

import com.ecommerce.app.globalServices.ImageService;
import com.ecommerce.app.module.settings.model.GlobalSettings;
import com.ecommerce.app.module.settings.repository.GlobalSettingsRepository;
import com.ecommerce.app.services.StorageProperties;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 * @author libertyerp_local
 */
@Service
@Transactional
public class GlobalSettingsService {

    private static final String SETTINGS_IMAGE_DIR = "settings";
    private static final String SETTINGS_IMAGE_URL_PREFIX = "/files/" + SETTINGS_IMAGE_DIR + "/";
    private static final String SETTINGS_LOGO_DESKTOP_DIR = SETTINGS_IMAGE_DIR + "/logo/desktop";
    private static final String SETTINGS_LOGO_MOBILE_DIR = SETTINGS_IMAGE_DIR + "/logo/mobile";
    private static final String SETTINGS_LOGO_SQUARE_DIR = SETTINGS_IMAGE_DIR + "/logo/square";
    private static final int SITE_LOGO_DESKTOP_WIDTH = 600;
    private static final int SITE_LOGO_DESKTOP_HEIGHT = 200;
    private static final int SITE_LOGO_MOBILE_WIDTH = 300;
    private static final int SITE_LOGO_MOBILE_HEIGHT = 100;
    private static final int SITE_LOGO_SQUARE_SIZE = 256;
    private static final int FAVICON_SIZE = 64;
    private static final int OG_IMAGE_WIDTH = 1200;
    private static final int OG_IMAGE_HEIGHT = 630;

    @Autowired
    private GlobalSettingsRepository globalSettingsRepository;

    @Autowired
    private ImageService imageService;

    @Autowired
    private StorageProperties storageProperties;

    @Cacheable(value = "globalSettings")
    public GlobalSettings getActiveSettings() {
        return globalSettingsRepository
                .findFirstByActiveTrueOrderByIdAsc()
                .orElseGet(() -> {
                    GlobalSettings settings = createDefaultSettings();
                    settings.setActive(true);
                    return globalSettingsRepository.save(settings);
                });
    }

    @CacheEvict(value = "globalSettings", allEntries = true)
    public GlobalSettings updateSettings(
            GlobalSettings formData,
            MultipartFile siteLogoFile,
            MultipartFile faviconFile,
            MultipartFile ogImageFile
    ) throws IOException {

        if (formData == null) {
            throw new IllegalArgumentException("Form data cannot be null");
        }

        GlobalSettings settings = getActiveSettings();

        applyBasic(formData, settings);
        applySeo(formData, settings);
        applyStore(formData, settings);
        applyPayment(formData, settings);
        applyDelivery(formData, settings);
        applyOrder(formData, settings);
        applySocial(formData, settings);
        applyPolicy(formData, settings);
        applyMaintenance(formData, settings);
        applyImages(settings, siteLogoFile, faviconFile, ogImageFile);

        return globalSettingsRepository.save(settings);
    }

    // Consolidated single-field update methods into a generic method
    @CacheEvict(value = "globalSettings", allEntries = true)
    public void updateSettingsField(GlobalSettings formData, SettingsSection section) {
        if (formData == null) {
            return;
        }
        GlobalSettings settings = getActiveSettings();

        switch (section) {
            case BASIC:
                applyBasic(formData, settings);
                break;
            case SEO:
                applySeo(formData, settings);
                break;
            case STORE:
                applyStore(formData, settings);
                break;
            case PAYMENT:
                applyPayment(formData, settings);
                break;
            case DELIVERY:
                applyDelivery(formData, settings);
                break;
            case ORDER:
                applyOrder(formData, settings);
                break;
            case SOCIAL:
                applySocial(formData, settings);
                break;
            case POLICY:
                applyPolicy(formData, settings);
                break;
            case MAINTENANCE:
                applyMaintenance(formData, settings);
                break;
        }

        globalSettingsRepository.save(settings);
    }

    @CacheEvict(value = "globalSettings", allEntries = true)
    public void deleteImage(SettingsImageType imageType) {
        if (imageType == null) {
            throw new IllegalArgumentException("Image type is required");
        }

        GlobalSettings settings = getActiveSettings();

        switch (imageType) {
            case SITE_LOGO -> {
                deleteFilesQuietly(
                        settings.getSiteLogo(),
                        settings.getSiteLogoDesktop(),
                        settings.getSiteLogoMobile(),
                        settings.getSiteLogoSquare()
                );
                settings.setSiteLogo(null);
                settings.setSiteLogoDesktop(null);
                settings.setSiteLogoMobile(null);
                settings.setSiteLogoSquare(null);
            }
            case FAVICON -> {
                deleteFilesQuietly(settings.getFavicon());
                settings.setFavicon(null);
            }
            case OG_IMAGE -> {
                deleteFilesQuietly(settings.getOgImage());
                settings.setOgImage(null);
            }
            default ->
                throw new IllegalArgumentException("Unsupported image type: " + imageType);
        }

        globalSettingsRepository.save(settings);
    }

    // Remove the redundant singleton save method - remove save() and delete() methods
    private GlobalSettings createDefaultSettings() {
        GlobalSettings settings = new GlobalSettings();
        settings.setSiteName("Universes Ecommerce");
        settings.setAdminEmail("admin@gmail.com");
        settings.setActive(true);
        settings.setTaxEnabled(false);
        settings.setStockManagementEnabled(true);
        settings.setAllowGuestCheckout(true);
        settings.setCodEnabled(true);
        settings.setOnlinePaymentEnabled(true);
        settings.setDeliveryEnabled(true);
        settings.setAutoConfirmOrder(false);
        settings.setAutoCancelUnpaidOrder(false);
        settings.setRegistrationEnabled(true);
        settings.setVendorRegistrationEnabled(false);
        settings.setMaintenanceMode(false);
        return settings;
    }

    private void applyBasic(GlobalSettings source, GlobalSettings target) {
        copyIfNotNull(source::getSiteName, target::setSiteName);
        copyIfNotNull(source::getSiteTitle, target::setSiteTitle);
        copyIfNotNull(source::getSiteTagline, target::setSiteTagline);
        copyIfNotNull(source::getSiteUrl, target::setSiteUrl);
        copyIfNotNull(source::getAdminEmail, target::setAdminEmail);
        copyIfNotNull(source::getSupportEmail, target::setSupportEmail);
        copyIfNotNull(source::getSupportPhone, target::setSupportPhone);
        copyIfNotNull(source::getAddress, target::setAddress);
        copyIfNotNull(source::getTimezone, target::setTimezone);
        copyIfNotNull(source::getCurrency, target::setCurrency);
        copyIfNotNull(source::getLanguage, target::setLanguage);
    }

    private void applySeo(GlobalSettings source, GlobalSettings target) {
        copyIfNotNull(source::getMetaTitle, target::setMetaTitle);
        copyIfNotNull(source::getMetaDescription, target::setMetaDescription);
        copyIfNotNull(source::getMetaKeywords, target::setMetaKeywords);
        copyIfNotNull(source::getOgTitle, target::setOgTitle);
        copyIfNotNull(source::getOgDescription, target::setOgDescription);
        copyIfNotNull(source::getGoogleAnalyticsId, target::setGoogleAnalyticsId);
        copyIfNotNull(source::getFacebookPixelId, target::setFacebookPixelId);
    }

    private void applyStore(GlobalSettings source, GlobalSettings target) {

        copyIfNotNull(source::getDefaultCurrency, target::setDefaultCurrency);
        copyIfNotNull(source::getTaxEnabled, target::setTaxEnabled);
        copyIfNotNull(source::getTaxPercentage, target::setTaxPercentage);
        copyIfNotNull(source::getStockManagementEnabled, target::setStockManagementEnabled);
        copyIfNotNull(source::getLowStockAlertQty, target::setLowStockAlertQty);
        copyIfNotNull(source::getAllowGuestCheckout, target::setAllowGuestCheckout);
        copyIfNotNull(source::getMinimumOrderAmount, target::setMinimumOrderAmount);
        copyIfNotNull(source::getMaximumOrderAmount, target::setMaximumOrderAmount);
    }

    private void applyPayment(GlobalSettings source, GlobalSettings target) {
        copyIfNotNull(source::getCodEnabled, target::setCodEnabled);
        copyIfNotNull(source::getOnlinePaymentEnabled, target::setOnlinePaymentEnabled);
        copyIfNotNull(source::getPartialPaymentEnabled, target::setPartialPaymentEnabled);
        copyIfNotNull(source::getEmiEnabled, target::setEmiEnabled);
    }

    private void applyDelivery(GlobalSettings source, GlobalSettings target) {
        copyIfNotNull(source::getDeliveryEnabled, target::setDeliveryEnabled);
        copyIfNotNull(source::getFreeDeliveryEnabled, target::setFreeDeliveryEnabled);
        copyIfNotNull(source::getFreeDeliveryMinAmount, target::setFreeDeliveryMinAmount);
        copyIfNotNull(source::getInsideDhakaDeliveryCharge, target::setInsideDhakaDeliveryCharge);
        copyIfNotNull(source::getOutsideDhakaDeliveryCharge, target::setOutsideDhakaDeliveryCharge);
        copyIfNotNull(source::getDeliveryTimeText, target::setDeliveryTimeText);
        copyIfNotNull(source::getCashOnDeliveryCharge, target::setCashOnDeliveryCharge);
    }

    private void applyOrder(GlobalSettings source, GlobalSettings target) {
        copyIfNotNull(source::getOrderPrefix, target::setOrderPrefix);
        copyIfNotNull(source::getInvoicePrefix, target::setInvoicePrefix);
        copyIfNotNull(source::getAutoConfirmOrder, target::setAutoConfirmOrder);
        copyIfNotNull(source::getAutoCancelUnpaidOrder, target::setAutoCancelUnpaidOrder);
        copyIfNotNull(source::getCancelOrderAfterMinutes, target::setCancelOrderAfterMinutes);
        copyIfNotNull(source::getReturnAllowedDays, target::setReturnAllowedDays);
        copyIfNotNull(source::getRefundAllowedDays, target::setRefundAllowedDays);
    }

    private void applySocial(GlobalSettings source, GlobalSettings target) {
        copyIfNotNull(source::getFacebookUrl, target::setFacebookUrl);
        copyIfNotNull(source::getYoutubeUrl, target::setYoutubeUrl);
        copyIfNotNull(source::getInstagramUrl, target::setInstagramUrl);
        copyIfNotNull(source::getLinkedinUrl, target::setLinkedinUrl);
        copyIfNotNull(source::getTwitterUrl, target::setTwitterUrl);
        copyIfNotNull(source::getWhatsappNumber, target::setWhatsappNumber);
    }

    private void applyPolicy(GlobalSettings source, GlobalSettings target) {
        copyIfNotNull(source::getAboutUs, target::setAboutUs);
        copyIfNotNull(source::getContactUsContent, target::setContactUsContent);
        copyIfNotNull(source::getHelpPageContent, target::setHelpPageContent);
        copyIfNotNull(source::getTermsOfUseContent, target::setTermsOfUseContent);
        copyIfNotNull(source::getTermsAndConditions, target::setTermsAndConditions);
        copyIfNotNull(source::getPrivacyPolicy, target::setPrivacyPolicy);
        copyIfNotNull(source::getPaymentMethodsContent, target::setPaymentMethodsContent);
        copyIfNotNull(source::getReturnPolicy, target::setReturnPolicy);
        copyIfNotNull(source::getRefundPolicy, target::setRefundPolicy);
        copyIfNotNull(source::getShippingPolicy, target::setShippingPolicy);
    }

    private void applyMaintenance(GlobalSettings source, GlobalSettings target) {
        copyIfNotNull(source::getMaintenanceMode, target::setMaintenanceMode);
        copyIfNotNull(source::getMaintenanceMessage, target::setMaintenanceMessage);
        copyIfNotNull(source::getRegistrationEnabled, target::setRegistrationEnabled);
        copyIfNotNull(source::getVendorRegistrationEnabled, target::setVendorRegistrationEnabled);
    }

    // Helper method to reduce boilerplate null checks
    private <T> void copyIfNotNull(java.util.function.Supplier<T> getter, java.util.function.Consumer<T> setter) {
        T value = getter.get();
        if (value != null) {
            setter.accept(value);
        }
    }

    private void applyImages(
            GlobalSettings target,
            MultipartFile siteLogoFile,
            MultipartFile faviconFile,
            MultipartFile ogImageFile
    ) throws IOException {
        if (siteLogoFile != null && !siteLogoFile.isEmpty()) {
            String previousSiteLogo = target.getSiteLogo();
            String previousDesktopLogo = target.getSiteLogoDesktop();
            String previousMobileLogo = target.getSiteLogoMobile();
            String previousSquareLogo = target.getSiteLogoSquare();
            storeSiteLogoVariants(target, siteLogoFile);
            deleteFilesQuietly(previousSiteLogo, previousDesktopLogo, previousMobileLogo, previousSquareLogo);
        }
        if (faviconFile != null && !faviconFile.isEmpty()) {
            String previousFavicon = target.getFavicon();
            target.setFavicon(storeImage(faviconFile, FAVICON_SIZE, FAVICON_SIZE));
            deleteFilesQuietly(previousFavicon);
        }
        if (ogImageFile != null && !ogImageFile.isEmpty()) {
            String previousOgImage = target.getOgImage();
            target.setOgImage(storeImage(ogImageFile, OG_IMAGE_WIDTH, OG_IMAGE_HEIGHT));
            deleteFilesQuietly(previousOgImage);
        }
    }

    private void storeSiteLogoVariants(GlobalSettings target, MultipartFile file) throws IOException {
        String fileName = imageService.validateAndRename(file);

        if (!isValidImageFile(file)) {
            throw new IllegalArgumentException("Invalid image file format");
        }

        imageService.resizeAndUpload(file, SITE_LOGO_DESKTOP_WIDTH, SITE_LOGO_DESKTOP_HEIGHT,
                SETTINGS_LOGO_DESKTOP_DIR, fileName);
        imageService.resizeAndUpload(file, SITE_LOGO_MOBILE_WIDTH, SITE_LOGO_MOBILE_HEIGHT,
                SETTINGS_LOGO_MOBILE_DIR, fileName);
        imageService.resizeAndUpload(file, SITE_LOGO_SQUARE_SIZE, SITE_LOGO_SQUARE_SIZE,
                SETTINGS_LOGO_SQUARE_DIR, fileName);

        String desktopUrl = buildSettingsFileUrl(SETTINGS_LOGO_DESKTOP_DIR, fileName);
        target.setSiteLogo(desktopUrl);
        target.setSiteLogoDesktop(desktopUrl);
        target.setSiteLogoMobile(buildSettingsFileUrl(SETTINGS_LOGO_MOBILE_DIR, fileName));
        target.setSiteLogoSquare(buildSettingsFileUrl(SETTINGS_LOGO_SQUARE_DIR, fileName));
    }

    private String storeImage(MultipartFile file, int width, int height) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be null or empty");
        }

        String fileName = imageService.validateAndRename(file);
        imageService.resizeAndUpload(file, width, height, SETTINGS_IMAGE_DIR, fileName);
        return SETTINGS_IMAGE_URL_PREFIX + fileName;
    }

    private String buildSettingsFileUrl(String subDir, String fileName) {
        return String.format("/files/%s/%s", subDir, fileName);
    }

    private boolean isValidImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && (contentType.equals("image/jpeg")
                || contentType.equals("image/png")
                || contentType.equals("image/webp")
                || contentType.equals("image/svg+xml"));
    }

    private void deleteFilesQuietly(String... fileUrls) {
        Set<String> uniqueUrls = new LinkedHashSet<>();
        for (String fileUrl : fileUrls) {
            if (fileUrl != null && !fileUrl.isBlank()) {
                uniqueUrls.add(fileUrl);
            }
        }

        for (String fileUrl : uniqueUrls) {
            deleteStoredFileQuietly(fileUrl);
        }
    }

    private void deleteStoredFileQuietly(String fileUrl) {
        String relativePath = extractStorageRelativePath(fileUrl);
        if (relativePath == null) {
            return;
        }

        Path rootPath = Paths.get(storageProperties.getRootPath()).toAbsolutePath().normalize();
        Path filePath = rootPath.resolve(relativePath).normalize();

        if (!filePath.startsWith(rootPath)) {
            return;
        }

        try {
            Files.deleteIfExists(filePath);
        } catch (IOException ignored) {
        }
    }

    private String extractStorageRelativePath(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank() || !fileUrl.startsWith("/files/")) {
            return null;
        }
        return fileUrl.substring("/files/".length()).replace("/", java.io.File.separator);
    }

    // Enum for settings sections
    public enum SettingsSection {
        BASIC, SEO, STORE, PAYMENT, DELIVERY, ORDER, SOCIAL, POLICY, MAINTENANCE
    }

    public enum SettingsImageType {
        SITE_LOGO("Site logo"),
        FAVICON("Favicon"),
        OG_IMAGE("OG image");

        private final String label;

        SettingsImageType(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }
}
