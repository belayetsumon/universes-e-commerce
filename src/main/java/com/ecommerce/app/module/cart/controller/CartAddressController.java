/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Controller.java to edit this template
 */
package com.ecommerce.app.module.cart.controller;

import com.ecommerce.app.module.checkout.availability.CheckoutAvailability;
import com.ecommerce.app.module.checkout.availability.CheckoutAvailabilityService;
import com.ecommerce.app.module.checkout.guest.services.GuestCheckoutSessionService;
import com.ecommerce.app.module.checkout.guest.session.GuestCheckoutSession;
import com.ecommerce.app.module.cart.services.CheckoutAddressValidationService;
import com.ecommerce.app.module.shipping.model.ShippingLocation;
import com.ecommerce.app.module.user.ripository.UsersRepository;
import com.ecommerce.app.module.user.services.LoggedUserService;
import com.ecommerce.app.module.order.model.BillingAddress;
import com.ecommerce.app.module.order.model.ShippingAddress;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 *
 * @author libertyerp_local
 */
@Controller
@RequestMapping("/cart_address")
public class CartAddressController {

    @InitBinder({"billingAddress", "shippingAddress"})
    void restrictCheckoutAddressBinding(WebDataBinder binder) {
        binder.setDisallowedFields(
                "id", "uuid", "userId", "order",
                "createdBy", "created", "modifiedBy", "modified"
        );
    }

    @Autowired
    LoggedUserService loggedUserService;

    @Autowired
    UsersRepository usersRepository;

    @Autowired
    GuestCheckoutSessionService guestCheckoutSessionService;

    @Autowired
    CheckoutAvailabilityService checkoutAvailabilityService;

    @Autowired
    CheckoutAddressValidationService checkoutAddressValidationService;

    @PostMapping("/add_billing_address")
    public String addBillingAddress(Model model, HttpSession session, BillingAddress billingAddress,
            @RequestParam(name = "sameAddress", required = false) Boolean sameAddress,
            RedirectAttributes redirectAttributes
    ) {
        String availabilityRedirect = checkoutAvailabilityRedirect(redirectAttributes);
        if (availabilityRedirect != null) {
            return availabilityRedirect;
        }
        BillingAddress safeBillingAddress = copySubmittedBillingAddress(billingAddress);
        boolean verifiedGuest = !loggedUserService.isAuthenticatedUser()
                && guestCheckoutSessionService.isVerified(session);
        if (verifiedGuest) {
            guestCheckoutSessionService.current(session)
                    .ifPresent(guest -> safeBillingAddress.setMobile(guest.getVerifiedMobile()));
        }
        String billingError = checkoutAddressValidationService.validateBillingAddress(safeBillingAddress);
        if (billingError != null) {
            clearSubmittedAddresses(session);
            redirectAttributes.addFlashAttribute("errorMessage", billingError);
            return "redirect:/order/create";
        }
        safeBillingAddress.setMobile(
                checkoutAddressValidationService.normalizeMobileForStorage(safeBillingAddress.getMobile()));
        if (verifiedGuest) {
            applyVerifiedGuestAddress(session, safeBillingAddress, Boolean.TRUE);
            return "redirect:/order/create";
        }

        session.setAttribute("session_Billing_address", safeBillingAddress);

        ShippingAddress shippingAddress = new ShippingAddress();

        if (Boolean.TRUE.equals(sameAddress)) {

            shippingAddress.setFirstName(safeBillingAddress.getFirstName());
            shippingAddress.setLastName(safeBillingAddress.getLastName());
            shippingAddress.setEmail(safeBillingAddress.getEmail());
            shippingAddress.setMobile(safeBillingAddress.getMobile());
            shippingAddress.setCompany(safeBillingAddress.getCompany());
            shippingAddress.setAddressLineOne(safeBillingAddress.getAddressLineOne());
            shippingAddress.setAddressLinetwo(safeBillingAddress.getAddressLinetwo());
            shippingAddress.setPostCode(safeBillingAddress.getPostCode());
            if (!applySelectedLocation(session, shippingAddress)) {
                session.removeAttribute("session_Shipping_address");
                return "redirect:/district/select-district";
            }
            String shippingError = checkoutAddressValidationService.validateShippingAddress(
                    shippingAddress,
                    currentShippingLocation(session)
            );
            if (shippingError != null) {
                session.removeAttribute("session_Shipping_address");
                redirectAttributes.addFlashAttribute("errorMessage", shippingError);
                return "redirect:/order/create";
            }
            session.setAttribute("session_Shipping_address", shippingAddress);
        } else {
            // Avoid reusing an old shipping form when billing is edited independently.
            session.removeAttribute("session_Shipping_address");
        }

        return "redirect:/order/create";
    }

    @PostMapping("/add_shipping_address")
    public String addShippingAddress(Model model, HttpSession session, ShippingAddress shippingAddress, RedirectAttributes redirectAttributes) {
        String availabilityRedirect = checkoutAvailabilityRedirect(redirectAttributes);
        if (availabilityRedirect != null) {
            return availabilityRedirect;
        }

        ShippingAddress safeShippingAddress = copySubmittedShippingAddress(shippingAddress);
        if (!loggedUserService.isAuthenticatedUser()) {
            guestCheckoutSessionService.current(session)
                    .ifPresent(guest -> safeShippingAddress.setMobile(guest.getVerifiedMobile()));
        }
        if (!applySelectedLocation(session, safeShippingAddress)) {
            session.removeAttribute("session_Shipping_address");
            return "redirect:/district/select-district";
        }
        String shippingError = checkoutAddressValidationService.validateShippingAddress(
                safeShippingAddress,
                currentShippingLocation(session)
        );
        if (shippingError != null) {
            session.removeAttribute("session_Shipping_address");
            redirectAttributes.addFlashAttribute("errorMessage", shippingError);
            return "redirect:/order/create";
        }
        safeShippingAddress.setMobile(
                checkoutAddressValidationService.normalizeMobileForStorage(safeShippingAddress.getMobile()));
        session.setAttribute("session_Shipping_address", safeShippingAddress);

        return "redirect:/order/create";
    }

    @PostMapping("/guest_delivery_address")
    public String saveGuestDeliveryAddress(
            Model model,
            HttpSession session,
            @RequestParam(name = "recipientName") String recipientName,
            @RequestParam(name = "addressLineOne") String addressLineOne,
            @RequestParam(name = "addressLinetwo", required = false) String addressLinetwo,
            @RequestParam(name = "postCode", required = false) String postCode,
            RedirectAttributes redirectAttributes) {

        String availabilityRedirect = checkoutAvailabilityRedirect(redirectAttributes);
        if (availabilityRedirect != null) {
            return availabilityRedirect;
        }

        GuestCheckoutSession guestSession = guestCheckoutSessionService.current(session).orElse(null);
        if (guestSession == null) {
            return "redirect:/cart/checkout";
        }

        ShippingLocation location = currentShippingLocation(session);
        if (location == null || !location.isActive()) {
            return "redirect:/district/select-district";
        }

        String cleanedName = clean(recipientName);
        String cleanedAddress = clean(addressLineOne);
        if (cleanedName == null || cleanedAddress == null) {
            return "redirect:/order/create";
        }

        String[] names = splitName(cleanedName);
        BillingAddress billingAddress = new BillingAddress();
        billingAddress.setFirstName(names[0]);
        billingAddress.setLastName(names[1]);
        billingAddress.setMobile(guestSession.getVerifiedMobile());
        billingAddress.setCountry("Bangladesh");
        billingAddress.setDistrict(location.getDisplayLabel());
        billingAddress.setCity(location.getName());
        billingAddress.setAddressLineOne(cleanedAddress);
        billingAddress.setAddressLinetwo(clean(addressLinetwo));
        billingAddress.setPostCode(clean(postCode));

        applyVerifiedGuestAddress(session, billingAddress, Boolean.TRUE);
        return "redirect:/order/create";
    }

    private void applyVerifiedGuestAddress(HttpSession session, BillingAddress billingAddress, Boolean sameAddress) {
        GuestCheckoutSession guestSession = guestCheckoutSessionService.current(session).orElse(null);
        if (guestSession == null || billingAddress == null) {
            return;
        }

        ShippingLocation location = currentShippingLocation(session);
        billingAddress.setEmail(null);
        billingAddress.setMobile(guestSession.getVerifiedMobile());
        billingAddress.setCountry("Bangladesh");
        if (location != null) {
            billingAddress.setDistrict(location.getDisplayLabel());
            billingAddress.setCity(location.getName());
        }
        session.setAttribute("session_Billing_address", billingAddress);

        if (Boolean.TRUE.equals(sameAddress)) {
            ShippingAddress shippingAddress = new ShippingAddress();
            shippingAddress.setFirstName(billingAddress.getFirstName());
            shippingAddress.setLastName(billingAddress.getLastName());
            shippingAddress.setEmail(null);
            shippingAddress.setMobile(guestSession.getVerifiedMobile());
            shippingAddress.setCompany(billingAddress.getCompany());
            shippingAddress.setCountry(billingAddress.getCountry());
            shippingAddress.setDistrict(billingAddress.getDistrict());
            shippingAddress.setAddressLineOne(billingAddress.getAddressLineOne());
            shippingAddress.setAddressLinetwo(billingAddress.getAddressLinetwo());
            shippingAddress.setCity(billingAddress.getCity());
            shippingAddress.setPostCode(billingAddress.getPostCode());
            session.setAttribute("session_Shipping_address", shippingAddress);
        }
    }

    private ShippingLocation currentShippingLocation(HttpSession session) {
        Object value = session.getAttribute("shippingLocation");
        return value instanceof ShippingLocation location ? location : null;
    }

    private boolean applySelectedLocation(HttpSession session, ShippingAddress shippingAddress) {
        ShippingLocation location = currentShippingLocation(session);
        if (shippingAddress == null || location == null || !location.isActive()) {
            return false;
        }
        shippingAddress.setCountry("Bangladesh");
        shippingAddress.setDistrict(location.getDisplayLabel());
        shippingAddress.setCity(location.getName());
        return true;
    }

    private BillingAddress copySubmittedBillingAddress(BillingAddress source) {
        BillingAddress safe = new BillingAddress();
        if (source == null) {
            return safe;
        }
        safe.setFirstName(cleanLimited(source.getFirstName(), 120));
        safe.setLastName(cleanLimited(source.getLastName(), 120));
        safe.setEmail(cleanLimited(source.getEmail(), 254));
        safe.setMobile(cleanLimited(source.getMobile(), 20));
        safe.setCompany(cleanLimited(source.getCompany(), 160));
        safe.setAddressLineOne(cleanLimited(source.getAddressLineOne(), 255));
        safe.setAddressLinetwo(cleanLimited(source.getAddressLinetwo(), 255));
        safe.setCity(cleanLimited(source.getCity(), 120));
        safe.setPostCode(cleanLimited(source.getPostCode(), 40));
        safe.setCountry(cleanLimited(source.getCountry(), 80));
        safe.setDistrict(cleanLimited(source.getDistrict(), 160));
        return safe;
    }

    private ShippingAddress copySubmittedShippingAddress(ShippingAddress source) {
        ShippingAddress safe = new ShippingAddress();
        if (source == null) {
            return safe;
        }
        safe.setFirstName(cleanLimited(source.getFirstName(), 120));
        safe.setLastName(cleanLimited(source.getLastName(), 120));
        safe.setEmail(cleanLimited(source.getEmail(), 254));
        safe.setMobile(cleanLimited(source.getMobile(), 20));
        safe.setCompany(cleanLimited(source.getCompany(), 160));
        safe.setAddressLineOne(cleanLimited(source.getAddressLineOne(), 255));
        safe.setAddressLinetwo(cleanLimited(source.getAddressLinetwo(), 255));
        safe.setPostCode(cleanLimited(source.getPostCode(), 40));
        return safe;
    }

    private String cleanLimited(String value, int maximumLength) {
        String cleaned = clean(value);
        if (cleaned == null || cleaned.length() <= maximumLength) {
            return cleaned;
        }
        return cleaned.substring(0, maximumLength);
    }

    private String checkoutAvailabilityRedirect(RedirectAttributes redirectAttributes) {
        boolean authenticated = loggedUserService.isAuthenticatedUser();
        CheckoutAvailability availability = checkoutAvailabilityService.availability(authenticated);
        if (!availability.isCheckoutAvailable()) {
            redirectAttributes.addFlashAttribute("errorMessage", CheckoutAvailabilityService.CHECKOUT_UNAVAILABLE_MESSAGE);
            return "redirect:/cart/index";
        }
        if (!authenticated && availability.isLoginRequired() && !availability.isGuestAllowed()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please login before checkout.");
            return "redirect:/public/member-login";
        }
        return null;
    }

    private void clearSubmittedAddresses(HttpSession session) {
        if (session == null) {
            return;
        }
        session.removeAttribute("session_Billing_address");
        session.removeAttribute("session_Shipping_address");
    }

    private String[] splitName(String recipientName) {
        String[] parts = recipientName.trim().split("\\s+", 2);
        return new String[]{parts[0], parts.length > 1 ? parts[1] : ""};
    }

    private String clean(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

}
