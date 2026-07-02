package com.ecommerce.app.module.settings.services;

import com.ecommerce.app.globalServices.ImageService;
import com.ecommerce.app.module.settings.model.GlobalSettings;
import com.ecommerce.app.module.settings.repository.GlobalSettingsRepository;
import com.ecommerce.app.services.StorageProperties;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
public class GlobalSettingsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalSettingsService.class);

    private static final int SETTINGS_ID = 1;
    private static final long MAX_IMAGE_SIZE_BYTES = 5L * 1024L * 1024L;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

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

    private static final String DEFAULT_SITE_NAME = "Universal e-Commerce Platform";
    private static final String DEFAULT_ADMIN_EMAIL = "admin@gmail.com";
    private static final String DEFAULT_TIMEZONE = "Asia/Dhaka";
    private static final String DEFAULT_CURRENCY = "BDT";
    private static final String DEFAULT_LANGUAGE = "en";
    private static final String DEFAULT_ORDER_PREFIX = "ORD";
    private static final String DEFAULT_INVOICE_PREFIX = "INV";

    private final GlobalSettingsRepository globalSettingsRepository;
    private final ImageService imageService;
    private final StorageProperties storageProperties;

    public GlobalSettingsService(
            GlobalSettingsRepository globalSettingsRepository,
            ImageService imageService,
            StorageProperties storageProperties
    ) {
        this.globalSettingsRepository = globalSettingsRepository;
        this.imageService = imageService;
        this.storageProperties = storageProperties;
    }

    public GlobalSettings getActiveSettings() {
        try {
            return globalSettingsRepository.findById(SETTINGS_ID)
                    .or(() -> globalSettingsRepository.findFirstByActiveTrueOrderByIdAsc())
                    .orElseGet(this::createDefaultSettings);
        } catch (DataAccessException ex) {
            LOGGER.error("Unable to read global settings. Check global_settings table, columns, and database permissions.", ex);
            throw new SettingsOperationException("Settings database is not ready. Please verify the global_settings table.", ex);
        }
    }

    public GlobalSettings defaultSettingsForForm() {
        return defaultSettings();
    }

    public void prepareRejectedForm(GlobalSettings formData) {
        if (formData == null) {
            return;
        }
        try {
            copyMediaAndAuditFields(getActiveSettings(), formData);
        } catch (RuntimeException ex) {
            LOGGER.warn("Unable to hydrate rejected settings form with current media paths", ex);
        }
    }

    public GlobalSettings updateSettings(
            GlobalSettings formData,
            MultipartFile siteLogoFile,
            MultipartFile faviconFile,
            MultipartFile ogImageFile
    ) {
        requireFormData(formData);
        validateSettings(formData, siteLogoFile, faviconFile, ogImageFile);

        GlobalSettings settings = getActiveSettings();
        assertVersionIsCurrent(formData, settings);

        applyAllSections(formData, settings);
        prepareForSave(settings);
        applyImages(settings, siteLogoFile, faviconFile, ogImageFile);

        try {
            GlobalSettings saved = globalSettingsRepository.save(settings);
            LOGGER.info("Global settings updated successfully. settingsId={}, version={}", saved.getId(), saved.getVersion());
            return saved;
        } catch (DataAccessException ex) {
            LOGGER.error("Database failure while saving global settings. settingsId={}", settings.getId(), ex);
            throw new SettingsOperationException("Database error while saving settings. Please verify the global_settings table and try again.", ex);
        }
    }

    public GlobalSettings updateSettingsField(GlobalSettings formData, SettingsSection section) {
        requireFormData(formData);
        if (section == null) {
            throw new SettingsValidationException("Settings section is required.");
        }
        validateSection(formData, section);

        GlobalSettings settings = getActiveSettings();
        assertVersionIsCurrent(formData, settings);
        section.apply(formData, settings, this);
        prepareForSave(settings);

        try {
            GlobalSettings saved = globalSettingsRepository.save(settings);
            LOGGER.info("Global settings section updated. section={}, settingsId={}, version={}",
                    section.name(), saved.getId(), saved.getVersion());
            return saved;
        } catch (DataAccessException ex) {
            LOGGER.error("Database failure while saving global settings section. section={}, settingsId={}",
                    section.name(), settings.getId(), ex);
            throw new SettingsOperationException("Database error while saving " + section.getLabel().toLowerCase(Locale.ROOT) + " settings.", ex);
        }
    }

    public GlobalSettings deleteImage(SettingsImageType imageType) {
        if (imageType == null) {
            throw new SettingsValidationException("Image type is required.");
        }

        GlobalSettings settings = getActiveSettings();
        imageType.clear(settings, this);
        prepareForSave(settings);

        try {
            GlobalSettings saved = globalSettingsRepository.save(settings);
            LOGGER.info("Global settings image deleted. type={}, settingsId={}", imageType.name(), saved.getId());
            return saved;
        } catch (DataAccessException ex) {
            LOGGER.error("Database failure while deleting global settings image. type={}", imageType.name(), ex);
            throw new SettingsOperationException("Database error while deleting " + imageType.getLabel().toLowerCase(Locale.ROOT) + ".", ex);
        }
    }

    private GlobalSettings createDefaultSettings() {
        GlobalSettings settings = defaultSettings();
        try {
            GlobalSettings saved = globalSettingsRepository.save(settings);
            LOGGER.info("Created default global settings row. settingsId={}", saved.getId());
            return saved;
        } catch (DataAccessException ex) {
            LOGGER.error("Unable to create default global settings row. Check global_settings schema and database user privileges.", ex);
            throw new SettingsOperationException("Settings database is not ready. Please repair the global_settings table.", ex);
        }
    }

    private GlobalSettings defaultSettings() {
        GlobalSettings settings = new GlobalSettings();
        settings.setSiteName(DEFAULT_SITE_NAME);
        settings.setAdminEmail(DEFAULT_ADMIN_EMAIL);
        settings.setTimezone(DEFAULT_TIMEZONE);
        settings.setCurrency(DEFAULT_CURRENCY);
        settings.setLanguage(DEFAULT_LANGUAGE);
        settings.setDefaultCurrency(DEFAULT_CURRENCY);
        settings.setTaxEnabled(false);
        settings.setTaxPercentage(BigDecimal.ZERO);
        settings.setStockManagementEnabled(true);
        settings.setLowStockAlertQty(5);
        settings.setAllowGuestCheckout(true);
        settings.setMinimumOrderAmount(BigDecimal.ZERO);
        settings.setCodEnabled(true);
        settings.setOnlinePaymentEnabled(false);
        settings.setPartialPaymentEnabled(false);
        settings.setEmiEnabled(false);
        settings.setDeliveryEnabled(true);
        settings.setFreeDeliveryEnabled(false);
        settings.setFreeDeliveryMinAmount(BigDecimal.ZERO);
        settings.setInsideDhakaDeliveryCharge(BigDecimal.ZERO);
        settings.setOutsideDhakaDeliveryCharge(BigDecimal.ZERO);
        settings.setCashOnDeliveryCharge(BigDecimal.ZERO);
        settings.setOrderPrefix(DEFAULT_ORDER_PREFIX);
        settings.setInvoicePrefix(DEFAULT_INVOICE_PREFIX);
        settings.setAutoConfirmOrder(false);
        settings.setAutoCancelUnpaidOrder(true);
        settings.setCancelOrderAfterMinutes(60);
        settings.setReturnAllowedDays(7);
        settings.setRefundAllowedDays(7);
        settings.setMaintenanceMode(false);
        settings.setRegistrationEnabled(true);
        settings.setVendorRegistrationEnabled(true);
        settings.setActive(true);
        return settings;
    }

    private void validateSettings(
            GlobalSettings source,
            MultipartFile siteLogoFile,
            MultipartFile faviconFile,
            MultipartFile ogImageFile
    ) {
        List<String> errors = new ArrayList<>();
        validateBasic(source, errors);
        validateSeo(source, errors);
        validateStore(source, errors);
        validateDelivery(source, errors);
        validateOrder(source, errors);
        validateSocial(source, errors);
        validateMaintenance(source, errors);
        validateImage(siteLogoFile, "Site logo", errors);
        validateImage(faviconFile, "Favicon", errors);
        validateImage(ogImageFile, "OG image", errors);

        if (!errors.isEmpty()) {
            throw new SettingsValidationException(errors);
        }
    }

    private void validateSection(GlobalSettings source, SettingsSection section) {
        List<String> errors = new ArrayList<>();
        section.validate(source, this, errors);
        if (!errors.isEmpty()) {
            throw new SettingsValidationException(errors);
        }
    }

    private void validateBasic(GlobalSettings source, List<String> errors) {
        required(source.getSiteName(), "Site name", errors);
        max(source.getSiteName(), 150, "Site name", errors);
        max(source.getSiteTitle(), 200, "Site title", errors);
        max(source.getSiteTagline(), 300, "Site tagline", errors);
        url(source.getSiteUrl(), "Site URL", errors);
        required(source.getAdminEmail(), "Admin email", errors);
        email(source.getAdminEmail(), "Admin email", errors);
        email(source.getSupportEmail(), "Support email", errors);
        max(source.getSupportPhone(), 50, "Support phone", errors);
        max(source.getTimezone(), 80, "Timezone", errors);
        max(source.getCurrency(), 10, "Currency", errors);
        max(source.getLanguage(), 10, "Language", errors);
    }

    private void validateSeo(GlobalSettings source, List<String> errors) {
        max(source.getMetaTitle(), 200, "Meta title", errors);
        max(source.getMetaDescription(), 500, "Meta description", errors);
        max(source.getMetaKeywords(), 500, "Meta keywords", errors);
        max(source.getOgTitle(), 200, "OG title", errors);
        max(source.getOgDescription(), 500, "OG description", errors);
        max(source.getGoogleAnalyticsId(), 100, "Google Analytics ID", errors);
        max(source.getFacebookPixelId(), 100, "Facebook Pixel ID", errors);
    }

    private void validateStore(GlobalSettings source, List<String> errors) {
        max(source.getDefaultCurrency(), 10, "Default currency", errors);
        range(source.getTaxPercentage(), BigDecimal.ZERO, BigDecimal.valueOf(100), "Tax percentage", errors);
        nonNegative(source.getMinimumOrderAmount(), "Minimum order amount", errors);
        nonNegative(source.getMaximumOrderAmount(), "Maximum order amount", errors);
        nonNegative(source.getLowStockAlertQty(), "Low stock alert quantity", errors);
        if (source.getMinimumOrderAmount() != null
                && source.getMaximumOrderAmount() != null
                && source.getMaximumOrderAmount().compareTo(source.getMinimumOrderAmount()) < 0) {
            errors.add("Maximum order amount cannot be lower than minimum order amount.");
        }
    }

    private void validateDelivery(GlobalSettings source, List<String> errors) {
        nonNegative(source.getFreeDeliveryMinAmount(), "Free delivery minimum amount", errors);
        nonNegative(source.getInsideDhakaDeliveryCharge(), "Inside Dhaka delivery charge", errors);
        nonNegative(source.getOutsideDhakaDeliveryCharge(), "Outside Dhaka delivery charge", errors);
        nonNegative(source.getCashOnDeliveryCharge(), "Cash on delivery charge", errors);
        max(source.getDeliveryTimeText(), 150, "Delivery time text", errors);
    }

    private void validateOrder(GlobalSettings source, List<String> errors) {
        max(source.getOrderPrefix(), 20, "Order prefix", errors);
        max(source.getInvoicePrefix(), 20, "Invoice prefix", errors);
        nonNegative(source.getCancelOrderAfterMinutes(), "Cancel order after minutes", errors);
        nonNegative(source.getReturnAllowedDays(), "Return allowed days", errors);
        nonNegative(source.getRefundAllowedDays(), "Refund allowed days", errors);
    }

    private void validateSocial(GlobalSettings source, List<String> errors) {
        url(source.getFacebookUrl(), "Facebook URL", errors);
        url(source.getYoutubeUrl(), "YouTube URL", errors);
        url(source.getInstagramUrl(), "Instagram URL", errors);
        url(source.getLinkedinUrl(), "LinkedIn URL", errors);
        url(source.getTwitterUrl(), "Twitter/X URL", errors);
        max(source.getWhatsappNumber(), 50, "WhatsApp number", errors);
    }

    private void validateMaintenance(GlobalSettings source, List<String> errors) {
        if (Boolean.TRUE.equals(source.getMaintenanceMode())) {
            max(source.getMaintenanceMessage(), 1000, "Maintenance message", errors);
        }
    }

    private void validateImage(MultipartFile file, String label, List<String> errors) {
        if (!hasFile(file)) {
            return;
        }
        if (file.getSize() > MAX_IMAGE_SIZE_BYTES) {
            errors.add(label + " must be 5 MB or smaller.");
        }
        String contentType = file.getContentType();
        if (contentType == null
                || !(contentType.equals("image/jpeg")
                || contentType.equals("image/png")
                || contentType.equals("image/webp"))) {
            errors.add(label + " must be a JPG, PNG, or WebP image.");
        }
        if (storageProperties.getRootPath() == null || storageProperties.getRootPath().isBlank()) {
            errors.add("Storage root path is not configured for image uploads.");
        }
    }

    private void applyAllSections(GlobalSettings source, GlobalSettings target) {
        for (SettingsSection section : SettingsSection.values()) {
            section.apply(source, target, this);
        }
    }

    private void applyBasic(GlobalSettings source, GlobalSettings target) {
        target.setSiteName(requiredText(source.getSiteName(), DEFAULT_SITE_NAME));
        target.setSiteTitle(cleanText(source.getSiteTitle()));
        target.setSiteTagline(cleanText(source.getSiteTagline()));
        target.setSiteUrl(cleanText(source.getSiteUrl()));
        target.setAdminEmail(requiredText(source.getAdminEmail(), DEFAULT_ADMIN_EMAIL));
        target.setSupportEmail(cleanText(source.getSupportEmail()));
        target.setSupportPhone(cleanText(source.getSupportPhone()));
        target.setAddress(cleanText(source.getAddress()));
        target.setTimezone(defaultText(source.getTimezone(), DEFAULT_TIMEZONE));
        target.setCurrency(defaultText(source.getCurrency(), DEFAULT_CURRENCY));
        target.setLanguage(defaultText(source.getLanguage(), DEFAULT_LANGUAGE));
    }

    private void applySeo(GlobalSettings source, GlobalSettings target) {
        target.setMetaTitle(cleanText(source.getMetaTitle()));
        target.setMetaDescription(cleanText(source.getMetaDescription()));
        target.setMetaKeywords(cleanText(source.getMetaKeywords()));
        target.setOgTitle(cleanText(source.getOgTitle()));
        target.setOgDescription(cleanText(source.getOgDescription()));
        target.setGoogleAnalyticsId(cleanText(source.getGoogleAnalyticsId()));
        target.setFacebookPixelId(cleanText(source.getFacebookPixelId()));
    }

    private void applyStore(GlobalSettings source, GlobalSettings target) {
        target.setDefaultCurrency(defaultText(source.getDefaultCurrency(), DEFAULT_CURRENCY));
        target.setTaxEnabled(checked(source.getTaxEnabled()));
        target.setTaxPercentage(nonNegativeAmount(source.getTaxPercentage(), BigDecimal.ZERO));
        target.setStockManagementEnabled(checked(source.getStockManagementEnabled()));
        target.setLowStockAlertQty(nonNegativeInteger(source.getLowStockAlertQty(), 0));
        target.setAllowGuestCheckout(checked(source.getAllowGuestCheckout()));
        target.setMinimumOrderAmount(nonNegativeAmount(source.getMinimumOrderAmount(), BigDecimal.ZERO));
        target.setMaximumOrderAmount(optionalNonNegativeAmount(source.getMaximumOrderAmount()));
    }

    private void applyPayment(GlobalSettings source, GlobalSettings target) {
        target.setCodEnabled(checked(source.getCodEnabled()));
        target.setOnlinePaymentEnabled(checked(source.getOnlinePaymentEnabled()));
        target.setPartialPaymentEnabled(checked(source.getPartialPaymentEnabled()));
        target.setEmiEnabled(checked(source.getEmiEnabled()));
    }

    private void applyDelivery(GlobalSettings source, GlobalSettings target) {
        target.setDeliveryEnabled(checked(source.getDeliveryEnabled()));
        target.setFreeDeliveryEnabled(checked(source.getFreeDeliveryEnabled()));
        target.setFreeDeliveryMinAmount(nonNegativeAmount(source.getFreeDeliveryMinAmount(), BigDecimal.ZERO));
        target.setInsideDhakaDeliveryCharge(nonNegativeAmount(source.getInsideDhakaDeliveryCharge(), BigDecimal.ZERO));
        target.setOutsideDhakaDeliveryCharge(nonNegativeAmount(source.getOutsideDhakaDeliveryCharge(), BigDecimal.ZERO));
        target.setDeliveryTimeText(cleanText(source.getDeliveryTimeText()));
        target.setCashOnDeliveryCharge(nonNegativeAmount(source.getCashOnDeliveryCharge(), BigDecimal.ZERO));
    }

    private void applyOrder(GlobalSettings source, GlobalSettings target) {
        target.setOrderPrefix(defaultText(source.getOrderPrefix(), DEFAULT_ORDER_PREFIX));
        target.setInvoicePrefix(defaultText(source.getInvoicePrefix(), DEFAULT_INVOICE_PREFIX));
        target.setAutoConfirmOrder(checked(source.getAutoConfirmOrder()));
        target.setAutoCancelUnpaidOrder(checked(source.getAutoCancelUnpaidOrder()));
        target.setCancelOrderAfterMinutes(nonNegativeInteger(source.getCancelOrderAfterMinutes(), 0));
        target.setReturnAllowedDays(nonNegativeInteger(source.getReturnAllowedDays(), 0));
        target.setRefundAllowedDays(nonNegativeInteger(source.getRefundAllowedDays(), 0));
    }

    private void applySocial(GlobalSettings source, GlobalSettings target) {
        target.setFacebookUrl(cleanText(source.getFacebookUrl()));
        target.setYoutubeUrl(cleanText(source.getYoutubeUrl()));
        target.setInstagramUrl(cleanText(source.getInstagramUrl()));
        target.setLinkedinUrl(cleanText(source.getLinkedinUrl()));
        target.setTwitterUrl(cleanText(source.getTwitterUrl()));
        target.setWhatsappNumber(cleanText(source.getWhatsappNumber()));
    }

    private void applyPolicy(GlobalSettings source, GlobalSettings target) {
        target.setAboutUs(cleanText(source.getAboutUs()));
        target.setContactUsContent(cleanText(source.getContactUsContent()));
        target.setHelpPageContent(cleanText(source.getHelpPageContent()));
        target.setTermsOfUseContent(cleanText(source.getTermsOfUseContent()));
        target.setTermsAndConditions(cleanText(source.getTermsAndConditions()));
        target.setPrivacyPolicy(cleanText(source.getPrivacyPolicy()));
        target.setPaymentMethodsContent(cleanText(source.getPaymentMethodsContent()));
        target.setReturnPolicy(cleanText(source.getReturnPolicy()));
        target.setRefundPolicy(cleanText(source.getRefundPolicy()));
        target.setShippingPolicy(cleanText(source.getShippingPolicy()));
    }

    private void applyMaintenance(GlobalSettings source, GlobalSettings target) {
        target.setMaintenanceMode(checked(source.getMaintenanceMode()));
        target.setMaintenanceMessage(cleanText(source.getMaintenanceMessage()));
        target.setRegistrationEnabled(checked(source.getRegistrationEnabled()));
        target.setVendorRegistrationEnabled(checked(source.getVendorRegistrationEnabled()));
    }

    private void applyImages(
            GlobalSettings target,
            MultipartFile siteLogoFile,
            MultipartFile faviconFile,
            MultipartFile ogImageFile
    ) {
        try {
            if (hasFile(siteLogoFile)) {
                replaceSiteLogo(target, siteLogoFile);
            }
            if (hasFile(faviconFile)) {
                replaceFavicon(target, faviconFile);
            }
            if (hasFile(ogImageFile)) {
                replaceOgImage(target, ogImageFile);
            }
        } catch (IOException ex) {
            LOGGER.error("Failed to process global settings image upload", ex);
            throw new SettingsOperationException("Image upload failed. Please verify the file type and storage permission.", ex);
        }
    }

    private void replaceSiteLogo(GlobalSettings target, MultipartFile file) throws IOException {
        String previousSiteLogo = target.getSiteLogo();
        String previousDesktopLogo = target.getSiteLogoDesktop();
        String previousMobileLogo = target.getSiteLogoMobile();
        String previousSquareLogo = target.getSiteLogoSquare();

        String fileName = imageService.validateAndRename(file);
        imageService.resizeAndUpload(file, SITE_LOGO_DESKTOP_WIDTH, SITE_LOGO_DESKTOP_HEIGHT, SETTINGS_LOGO_DESKTOP_DIR, fileName);
        imageService.resizeAndUpload(file, SITE_LOGO_MOBILE_WIDTH, SITE_LOGO_MOBILE_HEIGHT, SETTINGS_LOGO_MOBILE_DIR, fileName);
        imageService.resizeAndUpload(file, SITE_LOGO_SQUARE_SIZE, SITE_LOGO_SQUARE_SIZE, SETTINGS_LOGO_SQUARE_DIR, fileName);

        String desktopUrl = buildSettingsFileUrl(SETTINGS_LOGO_DESKTOP_DIR, fileName);
        target.setSiteLogo(desktopUrl);
        target.setSiteLogoDesktop(desktopUrl);
        target.setSiteLogoMobile(buildSettingsFileUrl(SETTINGS_LOGO_MOBILE_DIR, fileName));
        target.setSiteLogoSquare(buildSettingsFileUrl(SETTINGS_LOGO_SQUARE_DIR, fileName));

        deleteFilesQuietly(previousSiteLogo, previousDesktopLogo, previousMobileLogo, previousSquareLogo);
    }

    private void replaceFavicon(GlobalSettings target, MultipartFile file) throws IOException {
        String previousFavicon = target.getFavicon();
        target.setFavicon(storeImage(file, FAVICON_SIZE, FAVICON_SIZE));
        deleteFilesQuietly(previousFavicon);
    }

    private void replaceOgImage(GlobalSettings target, MultipartFile file) throws IOException {
        String previousOgImage = target.getOgImage();
        target.setOgImage(storeImage(file, OG_IMAGE_WIDTH, OG_IMAGE_HEIGHT));
        deleteFilesQuietly(previousOgImage);
    }

    private String storeImage(MultipartFile file, int width, int height) throws IOException {
        String fileName = imageService.validateAndRename(file);
        imageService.resizeAndUpload(file, width, height, SETTINGS_IMAGE_DIR, fileName);
        return SETTINGS_IMAGE_URL_PREFIX + fileName;
    }

    private void clearSiteLogo(GlobalSettings settings) {
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

    private void clearFavicon(GlobalSettings settings) {
        deleteFilesQuietly(settings.getFavicon());
        settings.setFavicon(null);
    }

    private void clearOgImage(GlobalSettings settings) {
        deleteFilesQuietly(settings.getOgImage());
        settings.setOgImage(null);
    }

    private void prepareForSave(GlobalSettings settings) {
        settings.setSiteName(requiredText(settings.getSiteName(), DEFAULT_SITE_NAME));
        settings.setAdminEmail(requiredText(settings.getAdminEmail(), DEFAULT_ADMIN_EMAIL));
        settings.setTimezone(defaultText(settings.getTimezone(), DEFAULT_TIMEZONE));
        settings.setCurrency(defaultText(settings.getCurrency(), DEFAULT_CURRENCY));
        settings.setLanguage(defaultText(settings.getLanguage(), DEFAULT_LANGUAGE));
        settings.setDefaultCurrency(defaultText(settings.getDefaultCurrency(), DEFAULT_CURRENCY));
        settings.setTaxPercentage(nonNegativeAmount(settings.getTaxPercentage(), BigDecimal.ZERO));
        settings.setLowStockAlertQty(nonNegativeInteger(settings.getLowStockAlertQty(), 0));
        settings.setMinimumOrderAmount(nonNegativeAmount(settings.getMinimumOrderAmount(), BigDecimal.ZERO));
        settings.setMaximumOrderAmount(optionalNonNegativeAmount(settings.getMaximumOrderAmount()));
        settings.setFreeDeliveryMinAmount(nonNegativeAmount(settings.getFreeDeliveryMinAmount(), BigDecimal.ZERO));
        settings.setInsideDhakaDeliveryCharge(nonNegativeAmount(settings.getInsideDhakaDeliveryCharge(), BigDecimal.ZERO));
        settings.setOutsideDhakaDeliveryCharge(nonNegativeAmount(settings.getOutsideDhakaDeliveryCharge(), BigDecimal.ZERO));
        settings.setCashOnDeliveryCharge(nonNegativeAmount(settings.getCashOnDeliveryCharge(), BigDecimal.ZERO));
        settings.setOrderPrefix(defaultText(settings.getOrderPrefix(), DEFAULT_ORDER_PREFIX));
        settings.setInvoicePrefix(defaultText(settings.getInvoicePrefix(), DEFAULT_INVOICE_PREFIX));
        settings.setCancelOrderAfterMinutes(nonNegativeInteger(settings.getCancelOrderAfterMinutes(), 0));
        settings.setReturnAllowedDays(nonNegativeInteger(settings.getReturnAllowedDays(), 0));
        settings.setRefundAllowedDays(nonNegativeInteger(settings.getRefundAllowedDays(), 0));
        settings.setActive(true);
    }

    private void copyMediaAndAuditFields(GlobalSettings source, GlobalSettings target) {
        target.setSiteLogo(source.getSiteLogo());
        target.setSiteLogoDesktop(source.getSiteLogoDesktop());
        target.setSiteLogoMobile(source.getSiteLogoMobile());
        target.setSiteLogoSquare(source.getSiteLogoSquare());
        target.setFavicon(source.getFavicon());
        target.setOgImage(source.getOgImage());
        target.setUuid(source.getUuid());
        target.setVersion(source.getVersion());
        target.setCreatedOn(source.getCreatedOn());
        target.setCreatedBy(source.getCreatedBy());
        target.setUpdatedOn(source.getUpdatedOn());
        target.setUpdatedBy(source.getUpdatedBy());
    }

    private void assertVersionIsCurrent(GlobalSettings formData, GlobalSettings settings) {
        if (settings.getVersion() == null) {
            throw new SettingsOperationException(
                    "Settings version is missing in the database. Please repair the global_settings.version column before saving."
            );
        }
        if (formData.getVersion() == null) {
            throw new OptimisticLockingFailureException(
                    "Settings version is missing from the form. Please reload the settings page and try again."
            );
        }
        if (!Objects.equals(formData.getVersion(), settings.getVersion())) {
            throw new OptimisticLockingFailureException(
                    "Settings were updated by another user. Please reload the page and try again."
            );
        }
    }

    private void requireFormData(GlobalSettings formData) {
        if (formData == null) {
            throw new SettingsValidationException("Settings form data is required.");
        }
    }

    private boolean hasFile(MultipartFile file) {
        return file != null && !file.isEmpty();
    }

    private void required(String value, String label, List<String> errors) {
        if (cleanText(value) == null) {
            errors.add(label + " is required.");
        }
    }

    private void email(String value, String label, List<String> errors) {
        String cleanValue = cleanText(value);
        if (cleanValue != null && !EMAIL_PATTERN.matcher(cleanValue).matches()) {
            errors.add(label + " must be a valid email address.");
        }
    }

    private void url(String value, String label, List<String> errors) {
        String cleanValue = cleanText(value);
        if (cleanValue != null
                && !(cleanValue.startsWith("https://") || cleanValue.startsWith("http://"))) {
            errors.add(label + " must start with http:// or https://.");
        }
    }

    private void max(String value, int length, String label, List<String> errors) {
        String cleanValue = cleanText(value);
        if (cleanValue != null && cleanValue.length() > length) {
            errors.add(label + " must be " + length + " characters or fewer.");
        }
    }

    private void nonNegative(Integer value, String label, List<String> errors) {
        if (value != null && value < 0) {
            errors.add(label + " cannot be negative.");
        }
    }

    private void nonNegative(BigDecimal value, String label, List<String> errors) {
        if (value != null && value.compareTo(BigDecimal.ZERO) < 0) {
            errors.add(label + " cannot be negative.");
        }
    }

    private void range(BigDecimal value, BigDecimal minimum, BigDecimal maximum, String label, List<String> errors) {
        if (value == null) {
            return;
        }
        if (value.compareTo(minimum) < 0 || value.compareTo(maximum) > 0) {
            errors.add(label + " must be between " + minimum + " and " + maximum + ".");
        }
    }

    private String requiredText(String value, String defaultValue) {
        String cleanValue = cleanText(value);
        if (cleanValue != null) {
            return cleanValue;
        }
        return defaultValue;
    }

    private String defaultText(String value, String defaultValue) {
        String cleanValue = cleanText(value);
        return cleanValue != null ? cleanValue : defaultValue;
    }

    private String cleanText(String value) {
        if (value == null) {
            return null;
        }
        String cleanValue = value.trim();
        return cleanValue.isEmpty() ? null : cleanValue;
    }

    private Boolean checked(Boolean value) {
        return Boolean.TRUE.equals(value);
    }

    private Integer nonNegativeInteger(Integer value, Integer fallback) {
        Integer resolvedValue = value != null ? value : fallback;
        return resolvedValue != null && resolvedValue < 0 ? fallback : resolvedValue;
    }

    private BigDecimal nonNegativeAmount(BigDecimal value, BigDecimal fallback) {
        BigDecimal resolvedValue = value != null ? value : fallback;
        return resolvedValue != null && resolvedValue.compareTo(BigDecimal.ZERO) < 0 ? fallback : resolvedValue;
    }

    private BigDecimal optionalNonNegativeAmount(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) < 0 ? null : value;
    }

    private String buildSettingsFileUrl(String subDir, String fileName) {
        return String.format("/files/%s/%s", subDir, fileName);
    }

    private void deleteFilesQuietly(String... fileUrls) {
        Set<String> uniqueUrls = new LinkedHashSet<>();
        for (String fileUrl : fileUrls) {
            if (fileUrl != null && !fileUrl.isBlank()) {
                uniqueUrls.add(fileUrl);
            }
        }
        uniqueUrls.forEach(this::deleteStoredFileQuietly);
    }

    private void deleteStoredFileQuietly(String fileUrl) {
        String relativePath = extractStorageRelativePath(fileUrl);
        String rootPathValue = storageProperties.getRootPath();
        if (relativePath == null || rootPathValue == null || rootPathValue.isBlank()) {
            return;
        }

        Path rootPath = Paths.get(rootPathValue).toAbsolutePath().normalize();
        Path filePath = rootPath.resolve(relativePath).normalize();
        if (!filePath.startsWith(rootPath)) {
            LOGGER.warn("Skipped deleting settings image outside storage root. fileUrl={}", fileUrl);
            return;
        }

        try {
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            LOGGER.warn("Unable to delete old settings image. path={}", filePath, ex);
        }
    }

    private String extractStorageRelativePath(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank() || !fileUrl.startsWith("/files/")) {
            return null;
        }
        return fileUrl.substring("/files/".length()).replace("/", java.io.File.separator);
    }

    public enum SettingsSection {
        BASIC("basic", "Basic") {
            @Override
            void apply(GlobalSettings source, GlobalSettings target, GlobalSettingsService service) {
                service.applyBasic(source, target);
            }

            @Override
            void validate(GlobalSettings source, GlobalSettingsService service, List<String> errors) {
                service.validateBasic(source, errors);
            }
        },
        SEO("seo", "SEO") {
            @Override
            void apply(GlobalSettings source, GlobalSettings target, GlobalSettingsService service) {
                service.applySeo(source, target);
            }

            @Override
            void validate(GlobalSettings source, GlobalSettingsService service, List<String> errors) {
                service.validateSeo(source, errors);
            }
        },
        STORE("store", "Store") {
            @Override
            void apply(GlobalSettings source, GlobalSettings target, GlobalSettingsService service) {
                service.applyStore(source, target);
            }

            @Override
            void validate(GlobalSettings source, GlobalSettingsService service, List<String> errors) {
                service.validateStore(source, errors);
            }
        },
        PAYMENT("payment", "Payment") {
            @Override
            void apply(GlobalSettings source, GlobalSettings target, GlobalSettingsService service) {
                service.applyPayment(source, target);
            }
        },
        DELIVERY("delivery", "Delivery") {
            @Override
            void apply(GlobalSettings source, GlobalSettings target, GlobalSettingsService service) {
                service.applyDelivery(source, target);
            }

            @Override
            void validate(GlobalSettings source, GlobalSettingsService service, List<String> errors) {
                service.validateDelivery(source, errors);
            }
        },
        ORDER("order", "Order") {
            @Override
            void apply(GlobalSettings source, GlobalSettings target, GlobalSettingsService service) {
                service.applyOrder(source, target);
            }

            @Override
            void validate(GlobalSettings source, GlobalSettingsService service, List<String> errors) {
                service.validateOrder(source, errors);
            }
        },
        SOCIAL("social", "Social") {
            @Override
            void apply(GlobalSettings source, GlobalSettings target, GlobalSettingsService service) {
                service.applySocial(source, target);
            }

            @Override
            void validate(GlobalSettings source, GlobalSettingsService service, List<String> errors) {
                service.validateSocial(source, errors);
            }
        },
        POLICY("policy", "Policy") {
            @Override
            void apply(GlobalSettings source, GlobalSettings target, GlobalSettingsService service) {
                service.applyPolicy(source, target);
            }
        },
        MAINTENANCE("maintenance", "Maintenance") {
            @Override
            void apply(GlobalSettings source, GlobalSettings target, GlobalSettingsService service) {
                service.applyMaintenance(source, target);
            }

            @Override
            void validate(GlobalSettings source, GlobalSettingsService service, List<String> errors) {
                service.validateMaintenance(source, errors);
            }
        };

        private final String fragment;
        private final String label;

        SettingsSection(String fragment, String label) {
            this.fragment = fragment;
            this.label = label;
        }

        public String getFragment() {
            return fragment;
        }

        public String getLabel() {
            return label;
        }

        abstract void apply(GlobalSettings source, GlobalSettings target, GlobalSettingsService service);

        void validate(GlobalSettings source, GlobalSettingsService service, List<String> errors) {
        }
    }

    public enum SettingsImageType {
        SITE_LOGO("Site logo") {
            @Override
            void clear(GlobalSettings settings, GlobalSettingsService service) {
                service.clearSiteLogo(settings);
            }
        },
        FAVICON("Favicon") {
            @Override
            void clear(GlobalSettings settings, GlobalSettingsService service) {
                service.clearFavicon(settings);
            }
        },
        OG_IMAGE("OG image") {
            @Override
            void clear(GlobalSettings settings, GlobalSettingsService service) {
                service.clearOgImage(settings);
            }
        };

        private final String label;

        SettingsImageType(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }

        abstract void clear(GlobalSettings settings, GlobalSettingsService service);
    }

    public static class SettingsValidationException extends RuntimeException {

        private final List<String> errors;

        public SettingsValidationException(String error) {
            this(List.of(error));
        }

        public SettingsValidationException(List<String> errors) {
            super(String.join(" ", errors));
            this.errors = List.copyOf(errors);
        }

        public List<String> getErrors() {
            return errors;
        }
    }

    public static class SettingsOperationException extends RuntimeException {

        public SettingsOperationException(String message) {
            super(message);
        }

        public SettingsOperationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
