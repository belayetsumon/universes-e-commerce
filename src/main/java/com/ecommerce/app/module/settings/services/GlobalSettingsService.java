package com.ecommerce.app.module.settings.services;

import com.ecommerce.app.globalServices.ImageService;
import com.ecommerce.app.globalServices.ImageUploadPolicy;
import com.ecommerce.app.module.settings.form.BasicSiteSettingsForm;
import com.ecommerce.app.module.settings.form.DeliverySettingsForm;
import com.ecommerce.app.module.settings.form.MaintenanceSettingsForm;
import com.ecommerce.app.module.settings.form.OrderSettingsForm;
import com.ecommerce.app.module.settings.form.PaymentSettingsForm;
import com.ecommerce.app.module.settings.form.PolicySettingsForm;
import com.ecommerce.app.module.settings.form.SeoSettingsForm;
import com.ecommerce.app.module.settings.form.SocialSettingsForm;
import com.ecommerce.app.module.settings.form.StoreSettingsForm;
import com.ecommerce.app.module.settings.model.GlobalSettings;
import com.ecommerce.app.module.settings.model.SalesOrderMode;
import com.ecommerce.app.module.settings.model.StoreMode;
import com.ecommerce.app.module.settings.model.TrackingImplementationMode;
import com.ecommerce.app.module.settings.repository.GlobalSettingsRepository;
import com.ecommerce.app.services.StorageProperties;
import com.ecommerce.app.vendor.repository.VendorprofileRepository;
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
    private static final int MAX_IMAGE_WIDTH = 8000;
    private static final int MAX_IMAGE_HEIGHT = 8000;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final Pattern GA4_MEASUREMENT_ID_PATTERN = Pattern.compile("^G-[A-Z0-9]{6,15}$");
    private static final Pattern GTM_CONTAINER_ID_PATTERN = Pattern.compile("^GTM-[A-Z0-9]{4,12}$");
    private static final Pattern FACEBOOK_PIXEL_ID_PATTERN = Pattern.compile("^\\d{5,30}$");
    private static final Pattern FACEBOOK_APP_ID_PATTERN = Pattern.compile("^\\d{5,30}$");
    private static final Set<String> SETTINGS_IMAGE_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/webp");
    private static final Set<String> SETTINGS_IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");
    private static final Set<String> SETTINGS_IMAGE_FORMATS = Set.of("jpeg", "png", "webp");

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
    private final VendorprofileRepository vendorprofileRepository;
    private final ImageService imageService;
    private final StorageProperties storageProperties;
    private volatile GlobalSettings cachedActiveSettings;

    public GlobalSettingsService(
            GlobalSettingsRepository globalSettingsRepository,
            VendorprofileRepository vendorprofileRepository,
            ImageService imageService,
            StorageProperties storageProperties
    ) {
        this.globalSettingsRepository = globalSettingsRepository;
        this.vendorprofileRepository = vendorprofileRepository;
        this.imageService = imageService;
        this.storageProperties = storageProperties;
    }

    public GlobalSettings getActiveSettings() {
        GlobalSettings cachedSettings = cachedActiveSettings;
        if (cachedSettings != null) {
            return cachedSettings;
        }
        synchronized (this) {
            if (cachedActiveSettings == null) {
                cachedActiveSettings = loadActiveSettings();
            }
            return cachedActiveSettings;
        }
    }

    private GlobalSettings loadActiveSettings() {
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

        GlobalSettings settings = loadActiveSettings();
        assertVersionIsCurrent(formData, settings);

        applyAllSections(formData, settings);
        prepareForSave(settings);
        applyImages(settings, siteLogoFile, faviconFile, ogImageFile);

        try {
            GlobalSettings saved = globalSettingsRepository.save(settings);
            cachedActiveSettings = saved;
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

        GlobalSettings settings = loadActiveSettings();
        assertVersionIsCurrent(formData, settings);
        section.apply(formData, settings, this);
        prepareForSave(settings);

        try {
            GlobalSettings saved = globalSettingsRepository.save(settings);
            cachedActiveSettings = saved;
            LOGGER.info("Global settings section updated. section={}, settingsId={}, version={}",
                    section.name(), saved.getId(), saved.getVersion());
            return saved;
        } catch (DataAccessException ex) {
            LOGGER.error("Database failure while saving global settings section. section={}, settingsId={}",
                    section.name(), settings.getId(), ex);
            throw new SettingsOperationException("Database error while saving " + section.getLabel().toLowerCase(Locale.ROOT) + " settings.", ex);
        }
    }

    public GlobalSettings updateBasicSiteSettings(
            BasicSiteSettingsForm form,
            MultipartFile siteLogoFile,
            MultipartFile faviconFile
    ) {
        requireFormData(form, "Basic site settings form data is required.");
        GlobalSettings source = toGlobalSettings(form);
        List<String> errors = new ArrayList<>();
        validateBasic(source, errors);
        validateImage(siteLogoFile, "Site logo", errors);
        validateImage(faviconFile, "Favicon", errors);
        if (!errors.isEmpty()) {
            throw new SettingsValidationException(errors);
        }

        GlobalSettings settings = getActiveSettings();
        assertVersionIsCurrent(form.getVersion(), settings);
        applyBasic(source, settings);
        prepareForSave(settings);
        applyBasicSiteImages(settings, siteLogoFile, faviconFile);
        return saveSection(settings, "Basic site");
    }

    public GlobalSettings updateSeoSettings(SeoSettingsForm form, MultipartFile ogImageFile) {
        requireFormData(form, "SEO settings form data is required.");
        GlobalSettings source = toGlobalSettings(form);
        List<String> errors = new ArrayList<>();
        validateSeo(source, errors);
        validateImage(ogImageFile, "OG image", errors);
        if (!errors.isEmpty()) {
            throw new SettingsValidationException(errors);
        }

        GlobalSettings settings = getActiveSettings();
        assertVersionIsCurrent(form.getVersion(), settings);
        applySeo(source, settings);
        prepareForSave(settings);
        applyOgImage(settings, ogImageFile);
        return saveSection(settings, "SEO");
    }

    public GlobalSettings updateStoreSettings(StoreSettingsForm form) {
        requireFormData(form, "Store settings form data is required.");
        GlobalSettings source = toGlobalSettings(form);
        validateSection(source, SettingsSection.STORE);

        GlobalSettings settings = getActiveSettings();
        assertVersionIsCurrent(form.getVersion(), settings);
        applyStore(source, settings);
        prepareForSave(settings);
        return saveSection(settings, "Store");
    }

    public GlobalSettings updatePaymentSettings(PaymentSettingsForm form) {
        requireFormData(form, "Payment settings form data is required.");
        GlobalSettings source = toGlobalSettings(form);

        GlobalSettings settings = getActiveSettings();
        assertVersionIsCurrent(form.getVersion(), settings);
        applyPayment(source, settings);
        prepareForSave(settings);
        return saveSection(settings, "Payment");
    }

    public GlobalSettings updateDeliverySettings(DeliverySettingsForm form) {
        requireFormData(form, "Delivery settings form data is required.");
        GlobalSettings source = toGlobalSettings(form);
        validateSection(source, SettingsSection.DELIVERY);

        GlobalSettings settings = getActiveSettings();
        assertVersionIsCurrent(form.getVersion(), settings);
        applyDelivery(source, settings);
        prepareForSave(settings);
        return saveSection(settings, "Delivery");
    }

    public GlobalSettings updateOrderSettings(OrderSettingsForm form) {
        requireFormData(form, "Order settings form data is required.");
        GlobalSettings source = toGlobalSettings(form);
        validateSection(source, SettingsSection.ORDER);

        GlobalSettings settings = getActiveSettings();
        assertVersionIsCurrent(form.getVersion(), settings);
        applyOrder(source, settings);
        prepareForSave(settings);
        return saveSection(settings, "Order");
    }

    public GlobalSettings updatePolicySettings(PolicySettingsForm form) {
        requireFormData(form, "Policy settings form data is required.");
        GlobalSettings source = toGlobalSettings(form);

        GlobalSettings settings = getActiveSettings();
        assertVersionIsCurrent(form.getVersion(), settings);
        applyPolicy(source, settings);
        prepareForSave(settings);
        return saveSection(settings, "Policy");
    }

    public GlobalSettings updateMaintenanceSettings(MaintenanceSettingsForm form) {
        requireFormData(form, "Maintenance settings form data is required.");
        GlobalSettings source = toGlobalSettings(form);
        validateSection(source, SettingsSection.MAINTENANCE);

        GlobalSettings settings = getActiveSettings();
        assertVersionIsCurrent(form.getVersion(), settings);
        applyMaintenance(source, settings);
        prepareForSave(settings);
        return saveSection(settings, "Maintenance");
    }

    public GlobalSettings updateSocialSettings(SocialSettingsForm form) {
        requireFormData(form, "Social settings form data is required.");
        GlobalSettings source = toGlobalSettings(form);
        validateSection(source, SettingsSection.SOCIAL);

        GlobalSettings settings = getActiveSettings();
        assertVersionIsCurrent(form.getVersion(), settings);
        applySocial(source, settings);
        prepareForSave(settings);
        return saveSection(settings, "Social");
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
            cachedActiveSettings = saved;
            LOGGER.info("Global settings image deleted. type={}, settingsId={}", imageType.name(), saved.getId());
            return saved;
        } catch (DataAccessException ex) {
            LOGGER.error("Database failure while deleting global settings image. type={}", imageType.name(), ex);
            throw new SettingsOperationException("Database error while deleting " + imageType.getLabel().toLowerCase(Locale.ROOT) + ".", ex);
        }
    }

    private GlobalSettings saveSection(GlobalSettings settings, String sectionName) {
        try {
            GlobalSettings saved = globalSettingsRepository.save(settings);
            cachedActiveSettings = saved;
            LOGGER.info("Global settings {} section updated. settingsId={}, version={}",
                    sectionName, saved.getId(), saved.getVersion());
            return saved;
        } catch (DataAccessException ex) {
            LOGGER.error("Database failure while saving {} settings section. settingsId={}",
                    sectionName, settings.getId(), ex);
            throw new SettingsOperationException("Database error while saving " + sectionName.toLowerCase(Locale.ROOT) + " settings.", ex);
        }
    }

    private GlobalSettings toGlobalSettings(BasicSiteSettingsForm form) {
        GlobalSettings settings = new GlobalSettings();
        settings.setVersion(form.getVersion());
        settings.setSiteName(form.getSiteName());
        settings.setSiteTitle(form.getSiteTitle());
        settings.setSiteTagline(form.getSiteTagline());
        settings.setSiteUrl(form.getSiteUrl());
        settings.setAdminEmail(form.getAdminEmail());
        settings.setSupportEmail(form.getSupportEmail());
        settings.setSupportPhone(form.getSupportPhone());
        settings.setAddress(form.getAddress());
        settings.setTimezone(form.getTimezone());
        settings.setCurrency(form.getCurrency());
        settings.setLanguage(form.getLanguage());
        return settings;
    }

    private GlobalSettings toGlobalSettings(StoreSettingsForm form) {
        GlobalSettings settings = new GlobalSettings();
        settings.setVersion(form.getVersion());
        settings.setDefaultCurrency(form.getDefaultCurrency());
        settings.setTaxEnabled(form.getTaxEnabled());
        settings.setTaxPercentage(form.getTaxPercentage());
        settings.setStockManagementEnabled(form.getStockManagementEnabled());
        settings.setLowStockAlertQty(form.getLowStockAlertQty());
        settings.setSecureCheckoutEnabled(form.getSecureCheckoutEnabled());
        settings.setAllowGuestCheckout(form.getAllowGuestCheckout());
        settings.setGuestMobileRequired(Boolean.TRUE.equals(form.getAllowGuestCheckout()));
        settings.setGuestMobileOtpVerificationEnabled(form.getGuestMobileOtpVerificationEnabled());
        settings.setGuestOtpExpiryMinutes(form.getGuestOtpExpiryMinutes());
        settings.setGuestOtpMaximumAttempts(form.getGuestOtpMaximumAttempts());
        settings.setGuestOtpResendCooldownSeconds(form.getGuestOtpResendCooldownSeconds());
        settings.setGuestOtpDailySendLimit(form.getGuestOtpDailySendLimit());
        settings.setGuestAutoCreateCustomerAccount(form.getGuestAutoCreateCustomerAccount());
        settings.setStoreMode(form.getStoreMode());
        settings.setPrimaryVendorId(form.getPrimaryVendorId());
        settings.setMinimumOrderAmount(form.getMinimumOrderAmount());
        settings.setMaximumOrderAmount(form.getMaximumOrderAmount());
        return settings;
    }

    private GlobalSettings toGlobalSettings(PaymentSettingsForm form) {
        GlobalSettings settings = new GlobalSettings();
        settings.setVersion(form.getVersion());
        settings.setCodEnabled(form.getCodEnabled());
        settings.setOnlinePaymentEnabled(form.getOnlinePaymentEnabled());
        settings.setPartialPaymentEnabled(form.getPartialPaymentEnabled());
        settings.setEmiEnabled(form.getEmiEnabled());
        return settings;
    }

    private GlobalSettings toGlobalSettings(DeliverySettingsForm form) {
        GlobalSettings settings = new GlobalSettings();
        settings.setVersion(form.getVersion());
        settings.setDeliveryEnabled(form.getDeliveryEnabled());
        settings.setFreeDeliveryEnabled(form.getFreeDeliveryEnabled());
        settings.setFreeDeliveryMinAmount(form.getFreeDeliveryMinAmount());
        settings.setInsideDhakaDeliveryCharge(form.getInsideDhakaDeliveryCharge());
        settings.setOutsideDhakaDeliveryCharge(form.getOutsideDhakaDeliveryCharge());
        settings.setDeliveryTimeText(form.getDeliveryTimeText());
        settings.setCashOnDeliveryCharge(form.getCashOnDeliveryCharge());
        return settings;
    }

    private GlobalSettings toGlobalSettings(OrderSettingsForm form) {
        GlobalSettings settings = new GlobalSettings();
        settings.setVersion(form.getVersion());
        settings.setOrderPrefix(form.getOrderPrefix());
        settings.setInvoicePrefix(form.getInvoicePrefix());
        settings.setAutoConfirmOrder(form.getAutoConfirmOrder());
        settings.setAutoCancelUnpaidOrder(form.getAutoCancelUnpaidOrder());
        settings.setSalesOrderMode(form.getSalesOrderMode());
        settings.setCancelOrderAfterMinutes(form.getCancelOrderAfterMinutes());
        settings.setReturnAllowedDays(form.getReturnAllowedDays());
        settings.setRefundAllowedDays(form.getRefundAllowedDays());
        return settings;
    }

    private GlobalSettings toGlobalSettings(PolicySettingsForm form) {
        GlobalSettings settings = new GlobalSettings();
        settings.setVersion(form.getVersion());
        settings.setAboutUs(form.getAboutUs());
        settings.setContactUsContent(form.getContactUsContent());
        settings.setHelpPageContent(form.getHelpPageContent());
        settings.setTermsOfUseContent(form.getTermsOfUseContent());
        settings.setTermsAndConditions(form.getTermsAndConditions());
        settings.setPrivacyPolicy(form.getPrivacyPolicy());
        settings.setPaymentMethodsContent(form.getPaymentMethodsContent());
        settings.setReturnPolicy(form.getReturnPolicy());
        settings.setRefundPolicy(form.getRefundPolicy());
        settings.setShippingPolicy(form.getShippingPolicy());
        return settings;
    }

    private GlobalSettings toGlobalSettings(MaintenanceSettingsForm form) {
        GlobalSettings settings = new GlobalSettings();
        settings.setVersion(form.getVersion());
        settings.setMaintenanceMode(form.getMaintenanceMode());
        settings.setMaintenanceMessage(form.getMaintenanceMessage());
        settings.setRegistrationEnabled(form.getRegistrationEnabled());
        settings.setVendorRegistrationEnabled(form.getVendorRegistrationEnabled());
        return settings;
    }

    private GlobalSettings toGlobalSettings(SeoSettingsForm form) {
        GlobalSettings settings = new GlobalSettings();
        settings.setVersion(form.getVersion());
        settings.setMetaTitle(form.getMetaTitle());
        settings.setMetaDescription(form.getMetaDescription());
        settings.setMetaKeywords(form.getMetaKeywords());
        settings.setOgTitle(form.getOgTitle());
        settings.setOgDescription(form.getOgDescription());
        settings.setGoogleAnalyticsId(form.getGoogleAnalyticsId());
        settings.setFacebookPixelId(form.getFacebookPixelId());
        settings.setOpenGraphEnabled(form.getOpenGraphEnabled());
        settings.setOgSiteName(form.getOgSiteName());
        settings.setPublicBaseUrl(form.getPublicBaseUrl());
        settings.setFacebookAppId(form.getFacebookAppId());
        settings.setTwitterCardType(form.getTwitterCardType());
        settings.setSocialSharingEnabled(form.getSocialSharingEnabled());
        settings.setFacebookSharingEnabled(form.getFacebookSharingEnabled());
        settings.setMessengerSharingEnabled(form.getMessengerSharingEnabled());
        settings.setWhatsappSharingEnabled(form.getWhatsappSharingEnabled());
        settings.setLinkedinSharingEnabled(form.getLinkedinSharingEnabled());
        settings.setTwitterSharingEnabled(form.getTwitterSharingEnabled());
        settings.setEmailSharingEnabled(form.getEmailSharingEnabled());
        settings.setCopyLinkSharingEnabled(form.getCopyLinkSharingEnabled());
        settings.setNativeShareEnabled(form.getNativeShareEnabled());
        settings.setReferralLinksEnabled(form.getReferralLinksEnabled());
        settings.setReferralCookieExpiryDays(form.getReferralCookieExpiryDays());
        settings.setFacebookPixelEnabled(form.getFacebookPixelEnabled());
        settings.setFacebookBrowserTrackingEnabled(form.getFacebookBrowserTrackingEnabled());
        settings.setFacebookConversionApiEnabled(form.getFacebookConversionApiEnabled());
        settings.setFacebookConversionApiAccessToken(form.getFacebookConversionApiAccessToken());
        settings.setFacebookTestEventCode(form.getFacebookTestEventCode());
        settings.setFacebookDebugMode(form.getFacebookDebugMode());
        settings.setGoogleAnalyticsEnabled(form.getGoogleAnalyticsEnabled());
        settings.setGa4EnhancedEcommerceEnabled(form.getGa4EnhancedEcommerceEnabled());
        settings.setGa4DebugMode(form.getGa4DebugMode());
        settings.setGoogleConsentModeEnabled(form.getGoogleConsentModeEnabled());
        settings.setGoogleTagManagerEnabled(form.getGoogleTagManagerEnabled());
        settings.setGtmContainerId(form.getGtmContainerId());
        settings.setServerSideGtmUrl(form.getServerSideGtmUrl());
        settings.setTrackingImplementationMode(form.getTrackingImplementationMode());
        settings.setCookieConsentEnabled(form.getCookieConsentEnabled());
        return settings;
    }

    private GlobalSettings toGlobalSettings(SocialSettingsForm form) {
        GlobalSettings settings = new GlobalSettings();
        settings.setVersion(form.getVersion());
        settings.setFacebookUrl(form.getFacebookUrl());
        settings.setYoutubeUrl(form.getYoutubeUrl());
        settings.setInstagramUrl(form.getInstagramUrl());
        settings.setLinkedinUrl(form.getLinkedinUrl());
        settings.setTwitterUrl(form.getTwitterUrl());
        settings.setWhatsappNumber(form.getWhatsappNumber());
        return settings;
    }

    private GlobalSettings createDefaultSettings() {
        GlobalSettings settings = defaultSettings();
        try {
            GlobalSettings saved = globalSettingsRepository.save(settings);
            cachedActiveSettings = saved;
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
        settings.setSecureCheckoutEnabled(true);
        settings.setAllowGuestCheckout(true);
        settings.setGuestMobileRequired(true);
        settings.setGuestMobileOtpVerificationEnabled(true);
        settings.setGuestOtpExpiryMinutes(5);
        settings.setGuestOtpMaximumAttempts(5);
        settings.setGuestOtpResendCooldownSeconds(60);
        settings.setGuestOtpDailySendLimit(5);
        settings.setGuestAutoCreateCustomerAccount(true);
        settings.setStoreMode(StoreMode.MARKETPLACE);
        settings.setSalesOrderMode(SalesOrderMode.SPLIT_BY_VENDOR);
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
        max(source.getOgSiteName(), 150, "Open Graph site name", errors);
        max(source.getPublicBaseUrl(), 500, "Public website base URL", errors);
        max(source.getFacebookAppId(), 100, "Facebook App ID", errors);
        max(source.getTwitterCardType(), 50, "Twitter card type", errors);
        httpsUrl(source.getPublicBaseUrl(), "Public website base URL", errors);
        httpsUrl(source.getServerSideGtmUrl(), "Server-side GTM URL", errors);
        max(source.getGtmContainerId(), 50, "GTM container ID", errors);
        max(source.getFacebookConversionApiAccessToken(), 500, "Conversion API access token", errors);
        max(source.getFacebookTestEventCode(), 100, "Facebook test event code", errors);
        positive(source.getReferralCookieExpiryDays(), "Referral cookie expiry days", errors);
        trackingMode(source, errors);
        measurementId(source.getGoogleAnalyticsId(), source.getGoogleAnalyticsEnabled(), errors);
        gtmId(source.getGtmContainerId(), source.getGoogleTagManagerEnabled(), errors);
        facebookPixel(source.getFacebookPixelId(), source.getFacebookPixelEnabled(), errors);
        facebookAppId(source.getFacebookAppId(), errors);
    }

    private void validateStore(GlobalSettings source, List<String> errors) {
        max(source.getDefaultCurrency(), 10, "Default currency", errors);
        range(source.getTaxPercentage(), BigDecimal.ZERO, BigDecimal.valueOf(100), "Tax percentage", errors);
        nonNegative(source.getMinimumOrderAmount(), "Minimum order amount", errors);
        nonNegative(source.getMaximumOrderAmount(), "Maximum order amount", errors);
        nonNegative(source.getLowStockAlertQty(), "Low stock alert quantity", errors);
        if (Boolean.TRUE.equals(source.getAllowGuestCheckout())) {
            source.setGuestMobileRequired(true);
            positive(source.getGuestOtpExpiryMinutes(), "Guest OTP expiry minutes", errors);
            positive(source.getGuestOtpMaximumAttempts(), "Guest OTP maximum attempts", errors);
            positive(source.getGuestOtpDailySendLimit(), "Guest OTP daily send limit", errors);
            nonNegative(source.getGuestOtpResendCooldownSeconds(), "Guest OTP resend cooldown seconds", errors);
        }
        if (source.getStoreMode() == null) {
            errors.add("Store mode is required.");
        }
        if (source.getStoreMode() == StoreMode.SINGLE_VENDOR) {
            if (source.getPrimaryVendorId() == null) {
                errors.add("Primary vendor is required when store mode is single vendor.");
            } else if (!vendorprofileRepository.existsById(source.getPrimaryVendorId())) {
                errors.add("Selected primary vendor does not exist.");
            }
        }
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
        if (source.getSalesOrderMode() == null) {
            errors.add("Sales order mode is required.");
        }
        nonNegative(source.getCancelOrderAfterMinutes(), "Cancel order after minutes", errors);
        nonNegative(source.getReturnAllowedDays(), "Return allowed days", errors);
        nonNegative(source.getRefundAllowedDays(), "Refund allowed days", errors);
    }

    private void validateSocial(GlobalSettings source, List<String> errors) {
        httpsUrl(source.getFacebookUrl(), "Facebook URL", errors);
        httpsUrl(source.getYoutubeUrl(), "YouTube URL", errors);
        httpsUrl(source.getInstagramUrl(), "Instagram URL", errors);
        httpsUrl(source.getLinkedinUrl(), "LinkedIn URL", errors);
        httpsUrl(source.getTwitterUrl(), "Twitter/X URL", errors);
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
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null) {
            int extensionStart = originalFilename.lastIndexOf('.');
            if (extensionStart >= 0 && extensionStart < originalFilename.length() - 1) {
                extension = originalFilename.substring(extensionStart + 1).trim().toLowerCase(Locale.ROOT);
            }
        }
        if (!SETTINGS_IMAGE_EXTENSIONS.contains(extension)) {
            errors.add(label + " must use a .jpg, .jpeg, .png, or .webp extension.");
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
        target.setOpenGraphEnabled(checked(source.getOpenGraphEnabled()));
        target.setOgSiteName(cleanText(source.getOgSiteName()));
        target.setPublicBaseUrl(normalizeBaseUrl(source.getPublicBaseUrl()));
        target.setFacebookAppId(cleanText(source.getFacebookAppId()));
        target.setTwitterCardType(defaultText(source.getTwitterCardType(), "summary_large_image"));
        target.setSocialSharingEnabled(checked(source.getSocialSharingEnabled()));
        target.setFacebookSharingEnabled(checked(source.getFacebookSharingEnabled()));
        target.setMessengerSharingEnabled(checked(source.getMessengerSharingEnabled()));
        target.setWhatsappSharingEnabled(checked(source.getWhatsappSharingEnabled()));
        target.setLinkedinSharingEnabled(checked(source.getLinkedinSharingEnabled()));
        target.setTwitterSharingEnabled(checked(source.getTwitterSharingEnabled()));
        target.setEmailSharingEnabled(checked(source.getEmailSharingEnabled()));
        target.setCopyLinkSharingEnabled(checked(source.getCopyLinkSharingEnabled()));
        target.setNativeShareEnabled(checked(source.getNativeShareEnabled()));
        target.setReferralLinksEnabled(checked(source.getReferralLinksEnabled()));
        target.setReferralCookieExpiryDays(positiveInteger(source.getReferralCookieExpiryDays(), 30));
        target.setFacebookPixelEnabled(checked(source.getFacebookPixelEnabled()));
        target.setFacebookBrowserTrackingEnabled(checked(source.getFacebookBrowserTrackingEnabled()));
        target.setFacebookConversionApiEnabled(checked(source.getFacebookConversionApiEnabled()));
        target.setFacebookConversionApiAccessToken(resolveSecret(source.getFacebookConversionApiAccessToken(), target.getFacebookConversionApiAccessToken()));
        target.setFacebookTestEventCode(cleanText(source.getFacebookTestEventCode()));
        target.setFacebookDebugMode(checked(source.getFacebookDebugMode()));
        target.setGoogleAnalyticsEnabled(checked(source.getGoogleAnalyticsEnabled()));
        target.setGa4EnhancedEcommerceEnabled(checked(source.getGa4EnhancedEcommerceEnabled()));
        target.setGa4DebugMode(checked(source.getGa4DebugMode()));
        target.setGoogleConsentModeEnabled(checked(source.getGoogleConsentModeEnabled()));
        target.setGoogleTagManagerEnabled(checked(source.getGoogleTagManagerEnabled()));
        target.setGtmContainerId(cleanText(source.getGtmContainerId()));
        target.setServerSideGtmUrl(normalizeBaseUrl(source.getServerSideGtmUrl()));
        target.setTrackingImplementationMode(source.getTrackingImplementationMode() == null
                ? TrackingImplementationMode.DIRECT
                : source.getTrackingImplementationMode());
        target.setCookieConsentEnabled(checked(source.getCookieConsentEnabled()));
    }

    private void applyStore(GlobalSettings source, GlobalSettings target) {
        target.setDefaultCurrency(defaultText(source.getDefaultCurrency(), DEFAULT_CURRENCY));
        target.setTaxEnabled(checked(source.getTaxEnabled()));
        target.setTaxPercentage(nonNegativeAmount(source.getTaxPercentage(), BigDecimal.ZERO));
        target.setStockManagementEnabled(checked(source.getStockManagementEnabled()));
        target.setLowStockAlertQty(nonNegativeInteger(source.getLowStockAlertQty(), 0));
        target.setSecureCheckoutEnabled(checked(source.getSecureCheckoutEnabled()));
        target.setAllowGuestCheckout(checked(source.getAllowGuestCheckout()));
        target.setGuestMobileRequired(Boolean.TRUE.equals(target.getAllowGuestCheckout()));
        target.setGuestMobileOtpVerificationEnabled(checked(source.getGuestMobileOtpVerificationEnabled()));
        target.setGuestOtpExpiryMinutes(positiveInteger(source.getGuestOtpExpiryMinutes(), 5));
        target.setGuestOtpMaximumAttempts(positiveInteger(source.getGuestOtpMaximumAttempts(), 5));
        target.setGuestOtpResendCooldownSeconds(nonNegativeInteger(source.getGuestOtpResendCooldownSeconds(), 60));
        target.setGuestOtpDailySendLimit(positiveInteger(source.getGuestOtpDailySendLimit(), 5));
        target.setGuestAutoCreateCustomerAccount(checked(source.getGuestAutoCreateCustomerAccount()));
        target.setStoreMode(source.getStoreMode() != null ? source.getStoreMode() : StoreMode.MARKETPLACE);
        target.setPrimaryVendorId(target.getStoreMode() == StoreMode.SINGLE_VENDOR ? source.getPrimaryVendorId() : null);
        enforceSingleVendorAccessRules(target);
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
        target.setSalesOrderMode(source.getSalesOrderMode() != null ? source.getSalesOrderMode() : SalesOrderMode.SPLIT_BY_VENDOR);
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
        enforceSingleVendorAccessRules(target);
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
        } catch (IOException | RuntimeException ex) {
            LOGGER.error("Failed to process global settings image upload", ex);
            throw settingsImageUploadFailure("Image upload", ex);
        }
    }

    private void applyBasicSiteImages(
            GlobalSettings target,
            MultipartFile siteLogoFile,
            MultipartFile faviconFile
    ) {
        try {
            if (hasFile(siteLogoFile)) {
                replaceSiteLogo(target, siteLogoFile);
            }
            if (hasFile(faviconFile)) {
                replaceFavicon(target, faviconFile);
            }
        } catch (IOException | RuntimeException ex) {
            LOGGER.error("Failed to process basic site settings image upload", ex);
            throw settingsImageUploadFailure("Image upload", ex);
        }
    }

    private void applyOgImage(GlobalSettings target, MultipartFile ogImageFile) {
        try {
            if (hasFile(ogImageFile)) {
                replaceOgImage(target, ogImageFile);
            }
        } catch (IOException | RuntimeException ex) {
            LOGGER.error("Failed to process SEO OG image upload", ex);
            throw settingsImageUploadFailure("OG image upload", ex);
        }
    }

    private void replaceSiteLogo(GlobalSettings target, MultipartFile file) throws IOException {
        String previousSiteLogo = target.getSiteLogo();
        String previousDesktopLogo = target.getSiteLogoDesktop();
        String previousMobileLogo = target.getSiteLogoMobile();
        String previousSquareLogo = target.getSiteLogoSquare();

        String fileName = imageService.validateAndRename(file, settingsImageUploadPolicy());
        imageService.resizeAndUploadHighQualityWebp(file, SITE_LOGO_DESKTOP_WIDTH, SITE_LOGO_DESKTOP_HEIGHT, SETTINGS_LOGO_DESKTOP_DIR, fileName);
        imageService.resizeAndUploadHighQualityWebp(file, SITE_LOGO_MOBILE_WIDTH, SITE_LOGO_MOBILE_HEIGHT, SETTINGS_LOGO_MOBILE_DIR, fileName);
        imageService.resizeAndUploadHighQualityWebp(file, SITE_LOGO_SQUARE_SIZE, SITE_LOGO_SQUARE_SIZE, SETTINGS_LOGO_SQUARE_DIR, fileName);

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
        String fileName = imageService.validateAndRename(file, settingsImageUploadPolicy());
        imageService.resizeAndUploadHighQualityWebp(file, width, height, SETTINGS_IMAGE_DIR, fileName);
        return SETTINGS_IMAGE_URL_PREFIX + fileName;
    }

    private ImageUploadPolicy settingsImageUploadPolicy() {
        return new ImageUploadPolicy(
                SETTINGS_IMAGE_CONTENT_TYPES,
                SETTINGS_IMAGE_EXTENSIONS,
                SETTINGS_IMAGE_FORMATS,
                MAX_IMAGE_SIZE_BYTES,
                MAX_IMAGE_WIDTH,
                MAX_IMAGE_HEIGHT,
                "JPG, PNG, or WEBP images"
        );
    }

    private SettingsOperationException settingsImageUploadFailure(String label, Exception ex) {
        String detail = ex.getMessage();
        if (detail == null || detail.isBlank()) {
            detail = "Please verify the file type, image dimensions, and storage permission.";
        }
        return new SettingsOperationException(label + " failed: " + detail, ex);
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
        settings.setStoreMode(settings.getStoreMode() != null ? settings.getStoreMode() : StoreMode.MARKETPLACE);
        settings.setSalesOrderMode(settings.getSalesOrderMode() != null ? settings.getSalesOrderMode() : SalesOrderMode.SPLIT_BY_VENDOR);
        settings.setOpenGraphEnabled(checked(settings.getOpenGraphEnabled()));
        settings.setTwitterCardType(defaultText(settings.getTwitterCardType(), "summary_large_image"));
        settings.setSocialSharingEnabled(checked(settings.getSocialSharingEnabled()));
        settings.setFacebookSharingEnabled(checked(settings.getFacebookSharingEnabled()));
        settings.setMessengerSharingEnabled(checked(settings.getMessengerSharingEnabled()));
        settings.setWhatsappSharingEnabled(checked(settings.getWhatsappSharingEnabled()));
        settings.setLinkedinSharingEnabled(checked(settings.getLinkedinSharingEnabled()));
        settings.setTwitterSharingEnabled(checked(settings.getTwitterSharingEnabled()));
        settings.setEmailSharingEnabled(checked(settings.getEmailSharingEnabled()));
        settings.setCopyLinkSharingEnabled(checked(settings.getCopyLinkSharingEnabled()));
        settings.setNativeShareEnabled(checked(settings.getNativeShareEnabled()));
        settings.setReferralLinksEnabled(checked(settings.getReferralLinksEnabled()));
        settings.setReferralCookieExpiryDays(positiveInteger(settings.getReferralCookieExpiryDays(), 30));
        settings.setFacebookPixelEnabled(checked(settings.getFacebookPixelEnabled()));
        settings.setFacebookBrowserTrackingEnabled(checked(settings.getFacebookBrowserTrackingEnabled()));
        settings.setFacebookConversionApiEnabled(checked(settings.getFacebookConversionApiEnabled()));
        settings.setFacebookDebugMode(checked(settings.getFacebookDebugMode()));
        settings.setGoogleAnalyticsEnabled(checked(settings.getGoogleAnalyticsEnabled()));
        settings.setGa4EnhancedEcommerceEnabled(checked(settings.getGa4EnhancedEcommerceEnabled()));
        settings.setGa4DebugMode(checked(settings.getGa4DebugMode()));
        settings.setGoogleConsentModeEnabled(checked(settings.getGoogleConsentModeEnabled()));
        settings.setGoogleTagManagerEnabled(checked(settings.getGoogleTagManagerEnabled()));
        settings.setTrackingImplementationMode(settings.getTrackingImplementationMode() == null
                ? TrackingImplementationMode.DIRECT
                : settings.getTrackingImplementationMode());
        settings.setCookieConsentEnabled(checked(settings.getCookieConsentEnabled()));
        settings.setFreeDeliveryMinAmount(nonNegativeAmount(settings.getFreeDeliveryMinAmount(), BigDecimal.ZERO));
        settings.setInsideDhakaDeliveryCharge(nonNegativeAmount(settings.getInsideDhakaDeliveryCharge(), BigDecimal.ZERO));
        settings.setOutsideDhakaDeliveryCharge(nonNegativeAmount(settings.getOutsideDhakaDeliveryCharge(), BigDecimal.ZERO));
        settings.setCashOnDeliveryCharge(nonNegativeAmount(settings.getCashOnDeliveryCharge(), BigDecimal.ZERO));
        settings.setOrderPrefix(defaultText(settings.getOrderPrefix(), DEFAULT_ORDER_PREFIX));
        settings.setInvoicePrefix(defaultText(settings.getInvoicePrefix(), DEFAULT_INVOICE_PREFIX));
        settings.setCancelOrderAfterMinutes(nonNegativeInteger(settings.getCancelOrderAfterMinutes(), 0));
        settings.setReturnAllowedDays(nonNegativeInteger(settings.getReturnAllowedDays(), 0));
        settings.setRefundAllowedDays(nonNegativeInteger(settings.getRefundAllowedDays(), 0));
        enforceSingleVendorAccessRules(settings);
        settings.setActive(true);
    }

    private void enforceSingleVendorAccessRules(GlobalSettings settings) {
        if (settings != null && settings.getStoreMode() == StoreMode.SINGLE_VENDOR) {
            settings.setVendorRegistrationEnabled(false);
        }
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
        assertVersionIsCurrent(formData.getVersion(), settings);
    }

    private void assertVersionIsCurrent(Long submittedVersion, GlobalSettings settings) {
        if (settings.getVersion() == null) {
            throw new SettingsOperationException(
                    "Settings version is missing in the database. Please repair the global_settings.version column before saving."
            );
        }
        if (submittedVersion == null) {
            throw new OptimisticLockingFailureException(
                    "Settings version is missing from the form. Please reload the settings page and try again."
            );
        }
        if (!Objects.equals(submittedVersion, settings.getVersion())) {
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

    private void requireFormData(Object formData, String message) {
        if (formData == null) {
            throw new SettingsValidationException(message);
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

    private void httpsUrl(String value, String label, List<String> errors) {
        String cleanValue = cleanText(value);
        if (cleanValue == null) {
            return;
        }
        if (!cleanValue.startsWith("https://")) {
            errors.add(label + " must be a valid HTTPS URL.");
            return;
        }
        String lowerValue = cleanValue.toLowerCase(Locale.ROOT);
        if (lowerValue.contains("localhost")
                || lowerValue.contains("127.0.0.1")
                || lowerValue.contains("0.0.0.0")
                || lowerValue.matches("https://10\\..*")
                || lowerValue.matches("https://172\\.(1[6-9]|2[0-9]|3[0-1])\\..*")
                || lowerValue.matches("https://192\\.168\\..*")) {
            errors.add(label + " cannot point to localhost, private IP addresses, or internal ports.");
        }
    }

    private void measurementId(String value, Boolean enabled, List<String> errors) {
        String cleanValue = cleanText(value);
        if (Boolean.TRUE.equals(enabled) && cleanValue == null) {
            errors.add("GA4 Measurement ID is required when Google Analytics is enabled.");
            return;
        }
        if (cleanValue != null && !GA4_MEASUREMENT_ID_PATTERN.matcher(cleanValue).matches()) {
            errors.add("GA4 Measurement ID must match G-XXXXXXXXXX.");
        }
    }

    private void gtmId(String value, Boolean enabled, List<String> errors) {
        String cleanValue = cleanText(value);
        if (Boolean.TRUE.equals(enabled) && cleanValue == null) {
            errors.add("GTM Container ID is required when Google Tag Manager is enabled.");
            return;
        }
        if (cleanValue != null && !GTM_CONTAINER_ID_PATTERN.matcher(cleanValue).matches()) {
            errors.add("GTM Container ID must match GTM-XXXXXXX.");
        }
    }

    private void facebookPixel(String value, Boolean enabled, List<String> errors) {
        String cleanValue = cleanText(value);
        if (Boolean.TRUE.equals(enabled) && cleanValue == null) {
            errors.add("Facebook Pixel ID is required when Facebook Pixel is enabled.");
            return;
        }
        if (cleanValue != null && !FACEBOOK_PIXEL_ID_PATTERN.matcher(cleanValue).matches()) {
            errors.add("Facebook Pixel ID must be numeric.");
        }
    }

    private void facebookAppId(String value, List<String> errors) {
        String cleanValue = cleanText(value);
        if (cleanValue != null && !FACEBOOK_APP_ID_PATTERN.matcher(cleanValue).matches()) {
            errors.add("Facebook App ID must be numeric.");
        }
    }

    private void trackingMode(GlobalSettings source, List<String> errors) {
        if (source.getTrackingImplementationMode() == null) {
            errors.add("Tracking implementation mode is required.");
            return;
        }
        if (source.getTrackingImplementationMode() == TrackingImplementationMode.GOOGLE_TAG_MANAGER
                && Boolean.TRUE.equals(source.getGoogleTagManagerEnabled())
                && cleanText(source.getGtmContainerId()) == null) {
            errors.add("GTM Container ID is required when tracking mode is Google Tag Manager.");
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

    private void positive(Integer value, String label, List<String> errors) {
        if (value == null || value < 1) {
            errors.add(label + " must be at least 1.");
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

    private String normalizeBaseUrl(String value) {
        String cleanValue = cleanText(value);
        if (cleanValue == null) {
            return null;
        }
        while (cleanValue.endsWith("/")) {
            cleanValue = cleanValue.substring(0, cleanValue.length() - 1);
        }
        return cleanValue;
    }

    private String resolveSecret(String submittedValue, String existingValue) {
        String cleanValue = cleanText(submittedValue);
        if (cleanValue == null || cleanValue.matches("\\*{4,}")) {
            return existingValue;
        }
        return cleanValue;
    }

    private Boolean checked(Boolean value) {
        return Boolean.TRUE.equals(value);
    }

    private Integer nonNegativeInteger(Integer value, Integer fallback) {
        Integer resolvedValue = value != null ? value : fallback;
        return resolvedValue != null && resolvedValue < 0 ? fallback : resolvedValue;
    }

    private Integer positiveInteger(Integer value, Integer fallback) {
        Integer resolvedValue = value != null ? value : fallback;
        return resolvedValue != null && resolvedValue > 0 ? resolvedValue : fallback;
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
