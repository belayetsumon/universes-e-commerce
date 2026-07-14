(function () {
    "use strict";

    var config = window.AppTrackingConfig || {};
    var firedEvents = new Set();
    var loadedScripts = new Set();
    var consentKey = "app_tracking_consent";
    window.dataLayer = window.dataLayer || [];

    function debugLog() {
        if (config.debug && window.console && typeof window.console.debug === "function") {
            window.console.debug.apply(window.console, arguments);
        }
    }

    function readConsent() {
        if (!config.cookieConsentEnabled) {
            return {necessary: true, analytics: true, marketing: true, preferences: true};
        }
        try {
            var stored = JSON.parse(window.localStorage.getItem(consentKey) || "{}");
            return {
                necessary: true,
                analytics: stored.analytics === true,
                marketing: stored.marketing === true,
                preferences: stored.preferences === true
            };
        } catch (error) {
            return {necessary: true, analytics: false, marketing: false, preferences: false};
        }
    }

    function hasConsent(category) {
        var consent = readConsent();
        return category === "necessary" || consent[category] === true;
    }

    function writeConsent(nextConsent) {
        var consent = {
            necessary: true,
            analytics: nextConsent && nextConsent.analytics === true,
            marketing: nextConsent && nextConsent.marketing === true,
            preferences: nextConsent && nextConsent.preferences === true
        };
        window.localStorage.setItem(consentKey, JSON.stringify(consent));
        pushConsentUpdate(consent);
        bootstrapProviders();
    }

    function hasStoredConsent() {
        if (!config.cookieConsentEnabled) {
            return true;
        }
        try {
            return window.localStorage.getItem(consentKey) !== null;
        } catch (error) {
            return false;
        }
    }

    function showConsentBanner() {
        if (!config.cookieConsentEnabled || hasStoredConsent() || document.querySelector("[data-consent-banner]")) {
            return;
        }
        var banner = document.createElement("div");
        banner.className = "tracking-consent";
        banner.setAttribute("data-consent-banner", "true");
        banner.setAttribute("role", "dialog");
        banner.setAttribute("aria-label", "Cookie consent");
        banner.innerHTML = [
            "<div class=\"tracking-consent__copy\">",
            "<strong>Privacy preferences</strong>",
            "<span>Necessary cookies stay on. Analytics and marketing tracking wait for your consent.</span>",
            "</div>",
            "<div class=\"tracking-consent__actions\">",
            "<button type=\"button\" class=\"btn btn-outline-secondary btn-sm\" data-consent-choice=\"necessary\">Necessary only</button>",
            "<button type=\"button\" class=\"btn btn-outline-primary btn-sm\" data-consent-choice=\"analytics\">Analytics only</button>",
            "<button type=\"button\" class=\"btn btn-primary btn-sm\" data-consent-choice=\"all\">Allow all</button>",
            "</div>"
        ].join("");
        document.body.appendChild(banner);
        banner.addEventListener("click", function (event) {
            var choice = event.target && event.target.getAttribute("data-consent-choice");
            if (!choice) {
                return;
            }
            if (choice === "all") {
                writeConsent({analytics: true, marketing: true, preferences: true});
            } else if (choice === "analytics") {
                writeConsent({analytics: true, marketing: false, preferences: false});
            } else {
                writeConsent({analytics: false, marketing: false, preferences: false});
            }
            banner.remove();
        });
    }

    function pushConsentUpdate(consent) {
        if (!config.googleConsentModeEnabled) {
            return;
        }
        window.dataLayer.push({
            event: "consent_update",
            consent: {
                analytics_storage: consent.analytics ? "granted" : "denied",
                ad_storage: consent.marketing ? "granted" : "denied",
                ad_user_data: consent.marketing ? "granted" : "denied",
                ad_personalization: consent.marketing ? "granted" : "denied",
                functionality_storage: consent.preferences ? "granted" : "denied"
            }
        });
    }

    function loadScript(id, src, onload) {
        if (!src || loadedScripts.has(id) || document.getElementById(id)) {
            if (typeof onload === "function") {
                onload();
            }
            return;
        }
        loadedScripts.add(id);
        var script = document.createElement("script");
        script.id = id;
        script.async = true;
        script.src = src;
        if (typeof onload === "function") {
            script.onload = onload;
        }
        document.head.appendChild(script);
    }

    function bootstrapProviders() {
        if (config.googleTagManagerEnabled && hasConsent("analytics")) {
            loadScript("app-gtm-script", "https://www.googletagmanager.com/gtm.js?id=" + encodeURIComponent(config.gtmContainerId || ""));
        }
        if (config.googleAnalyticsEnabled && hasConsent("analytics")) {
            loadScript("app-ga4-script", "https://www.googletagmanager.com/gtag/js?id=" + encodeURIComponent(config.ga4MeasurementId || ""), function () {
                window.dataLayer = window.dataLayer || [];
                window.gtag = window.gtag || function () { window.dataLayer.push(arguments); };
                window.gtag("js", new Date());
                window.gtag("config", config.ga4MeasurementId, {send_page_view: false, debug_mode: config.ga4DebugMode === true});
            });
        }
        if (config.facebookPixelEnabled && config.facebookBrowserTrackingEnabled && hasConsent("marketing")) {
            window.fbq = window.fbq || function () {
                window.fbq.callMethod ? window.fbq.callMethod.apply(window.fbq, arguments) : window.fbq.queue.push(arguments);
            };
            if (!window.fbq.loaded) {
                window.fbq.queue = [];
                window.fbq.loaded = true;
                window.fbq.version = "2.0";
                loadScript("app-facebook-pixel-script", "https://connect.facebook.net/en_US/fbevents.js", function () {
                    window.fbq("init", config.facebookPixelId);
                });
            }
        }
    }

    function eventKey(name, payload) {
        return name + ":" + (payload && (payload.event_id || payload.transaction_id || payload.item_id || payload.search_term || payload.url) || "default");
    }

    function normalizePayload(payload) {
        return payload && typeof payload === "object" ? payload : {};
    }

    function pushEvent(name, payload, options) {
        payload = normalizePayload(payload);
        options = options || {};
        var key = options.dedupeKey || eventKey(name, payload);
        if (firedEvents.has(key)) {
            debugLog("Tracking duplicate skipped", name, key);
            return;
        }
        firedEvents.add(key);

        var ecommerce = payload.ecommerce || undefined;
        window.dataLayer.push(Object.assign({event: name}, ecommerce ? {ecommerce: ecommerce} : {}, payload));

        if (config.googleAnalyticsEnabled && hasConsent("analytics") && typeof window.gtag === "function") {
            window.gtag("event", name, payload);
        }
        if (config.facebookPixelEnabled && hasConsent("marketing") && typeof window.fbq === "function") {
            var facebookEvent = facebookEventName(name);
            if (facebookEvent) {
                window.fbq("track", facebookEvent, payload, payload.event_id ? {eventID: payload.event_id} : undefined);
            }
        }
        debugLog("Tracking event", name, payload);
    }

    function facebookEventName(name) {
        return {
            page_view: "PageView",
            view_item: "ViewContent",
            search: "Search",
            add_to_cart: "AddToCart",
            add_to_wishlist: "AddToWishlist",
            begin_checkout: "InitiateCheckout",
            add_payment_info: "AddPaymentInfo",
            purchase: "Purchase",
            sign_up: "CompleteRegistration",
            generate_lead: "Lead",
            share: "Lead",
            referral_visit: "Lead"
        }[name];
    }

    function trackShare(button) {
        var payload = {
            method: button.dataset.sharePlatform,
            content_type: button.dataset.sharePageType || "PAGE",
            item_id: button.dataset.shareEntity || undefined,
            url: button.dataset.shareUrl
        };
        pushEvent("share", payload, {dedupeKey: "share:" + payload.method + ":" + payload.url + ":" + Date.now()});
        sendShareEvent(button);
    }

    function sendShareEvent(button) {
        try {
            if (!window.fetch || !button.dataset.shareUrl) {
                return;
            }
            var csrfToken = document.querySelector("meta[name='_csrf']") && document.querySelector("meta[name='_csrf']").content;
            var csrfHeader = document.querySelector("meta[name='_csrf_header']") && document.querySelector("meta[name='_csrf_header']").content;
            var headers = {"Content-Type": "application/json"};
            if (csrfToken && csrfHeader) {
                headers[csrfHeader] = csrfToken;
            }
            window.fetch("/public/share-track", {
                method: "POST",
                credentials: "same-origin",
                headers: headers,
                body: JSON.stringify({
                    platform: button.dataset.sharePlatform,
                    pageType: button.dataset.sharePageType || "PAGE",
                    publicEntityReference: button.dataset.shareEntity || null,
                    publicUrl: button.dataset.shareUrl,
                    campaignSource: "social_share"
                })
            }).catch(function () {});
        } catch (error) {
            debugLog("Share track failed", error);
        }
    }

    function bindSharing() {
        document.addEventListener("click", function (event) {
            var button = event.target.closest("[data-share-platform]");
            if (!button) {
                return;
            }
            trackShare(button);
            if (button.dataset.sharePlatform === "COPY_LINK") {
                event.preventDefault();
                navigator.clipboard.writeText(button.dataset.shareUrl || window.location.href).then(function () {
                    var feedback = button.closest(".social-share").querySelector(".social-share__feedback");
                    if (feedback) {
                        feedback.textContent = "Link copied";
                        window.setTimeout(function () { feedback.textContent = ""; }, 2200);
                    }
                }).catch(function () {});
            }
            if (button.dataset.sharePlatform === "NATIVE_SHARE") {
                event.preventDefault();
                if (navigator.share) {
                    navigator.share({
                        title: button.dataset.shareTitle || document.title,
                        text: button.dataset.shareDescription || "",
                        url: button.dataset.shareUrl || window.location.href
                    }).catch(function () {});
                }
            }
        });
    }

    window.AppTracking = {
        consent: writeConsent,
        pageView: function (data) { pushEvent("page_view", data || {page_location: window.location.href, page_title: document.title}); },
        viewProduct: function (data) { pushEvent("view_item", data); },
        viewItemList: function (data) { pushEvent("view_item_list", data); },
        search: function (data) { pushEvent("search", data); },
        addToCart: function (data) { pushEvent("add_to_cart", data); },
        removeFromCart: function (data) { pushEvent("remove_from_cart", data); },
        beginCheckout: function (data) { pushEvent("begin_checkout", data); },
        addShippingInfo: function (data) { pushEvent("add_shipping_info", data); },
        addPaymentInfo: function (data) { pushEvent("add_payment_info", data); },
        purchase: function (data) { pushEvent("purchase", data); },
        refund: function (data) { pushEvent("refund", data); },
        registration: function (data) { pushEvent("sign_up", data); },
        login: function (data) { pushEvent("login", data); },
        share: function (data) { pushEvent("share", data); },
        referralVisit: function (data) { pushEvent("referral_visit", data); }
    };

    function drainQueue() {
        var queue = window.AppTrackingQueue || [];
        while (queue.length > 0) {
            var item = queue.shift();
            if (Array.isArray(item) && item.length >= 1 && typeof window.AppTracking[item[0]] === "function") {
                try {
                    window.AppTracking[item[0]](item[1] || {});
                } catch (error) {
                    debugLog("Queued tracking event failed", error);
                }
            }
        }
    }

    pushConsentUpdate(readConsent());
    bootstrapProviders();
    bindSharing();
    drainQueue();
    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", showConsentBanner);
    } else {
        showConsentBanner();
    }
    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", function () { window.AppTracking.pageView(); });
    } else {
        window.AppTracking.pageView();
    }
}());
