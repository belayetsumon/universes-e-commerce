/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecommerce.app.globalcontroller;

import com.ecommerce.app.globalComponant.EntityNameResolver;
import com.ecommerce.app.module.shipping.model.ShippingLocation;
import com.ecommerce.app.module.shipping.model.ShippingLocationType;
import com.ecommerce.app.module.shipping.services.ShippingLocationService;
import com.ecommerce.app.module.user.ripository.UsersRepository;
import com.ecommerce.app.module.wishlist.service.WishlistService;
import com.ecommerce.app.product.model.ProductStatusEnum;
import com.ecommerce.app.product.model.Productcategory;
import com.ecommerce.app.product.ripository.ProductcategoryRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.Collections;
import java.util.Set;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 *
 * @author User
 */
@ControllerAdvice
public class GlobalController2 {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalController2.class);

    @Autowired
    private EntityNameResolver entityNameResolver;

    @Autowired
    UsersRepository usersRepository;

    @Autowired
    ProductcategoryRepository productcategoryRepository;

    @Autowired
    WishlistService wishlistService;

    @Autowired
    ShippingLocationService shippingLocationService;

    @ModelAttribute
    public void addAttributes(Model model) {

//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        if(!auth==null){
//        Users users = usersRepository.findByEmail(auth.getName());
//
//        model.addAttribute("username", users.getName());
    }

    @ModelAttribute
    public void addWishlistAttributes(Model model, Principal principal, HttpServletRequest request) {
        if (isBackOfficeRequest(request)) {
            addEmptyWishlistAttributes(model);
            return;
        }

        try {
            Set<String> wishlistProductUuids = wishlistService.getWishlistProductUuids(principal);
            Set<String> wishlistProductKeys = wishlistProductUuids.stream()
                    .filter(uuid -> uuid != null && !uuid.isBlank())
                    .collect(Collectors.toSet());
            model.addAttribute("wishlistProductIds", wishlistProductUuids);
            model.addAttribute("wishlistProductUuids", wishlistProductUuids);
            model.addAttribute("wishlistProductKeys", wishlistProductKeys);
            model.addAttribute("wishlistCount", wishlistProductUuids.size());
        } catch (RuntimeException ex) {
            LOGGER.warn("Unable to populate wishlist model attributes for {}", requestPath(request), ex);
            addEmptyWishlistAttributes(model);
        }
    }

//    @ModelAttribute
//    public void shopingcart(Model model, HttpSession session) {
//
//        if (session.getAttribute("sessioncart") != null) {
//
//            List<CartItem> cartitem = (List<CartItem>) session.getAttribute("sessioncart");
//
//            model.addAttribute("totaltest", cartitem.size());
//
//        } else {
//
//            model.addAttribute("totaltest", "0");
//        }
//
//    }
//
    @ModelAttribute("maincategory")
    public List<Productcategory> populateCategories(HttpServletRequest request) {
        if (isBackOfficeRequest(request)) {
            return Collections.emptyList();
        }

        try {
            return productcategoryRepository.findByStatusAndParentIsNull(ProductStatusEnum.Active);
        } catch (RuntimeException ex) {
            LOGGER.warn("Unable to populate category model attributes for {}", requestPath(request), ex);
            return Collections.emptyList();
        }
    }

    @ModelAttribute("shippinglocations")
    public List<ShippingLocation> shippingLocations(HttpServletRequest request) {
        if (isBackOfficeRequest(request)) {
            return Collections.emptyList();
        }

        try {
            return shippingLocationService.getActiveLocations();
        } catch (RuntimeException ex) {
            LOGGER.warn("Unable to populate shipping location model attributes for {}", requestPath(request), ex);
            return Collections.emptyList();
        }
    }

    @ModelAttribute("shippingDistricts")
    public List<ShippingLocation> shippingDistricts(HttpServletRequest request) {
        if (isBackOfficeRequest(request)) {
            return Collections.emptyList();
        }

        try {
            return shippingLocationService.getActiveDistricts();
        } catch (RuntimeException ex) {
            LOGGER.warn("Unable to populate shipping district model attributes for {}", requestPath(request), ex);
            return Collections.emptyList();
        }
    }

    @ModelAttribute("currentShippingLocation")
    public ShippingLocation currentShippingLocation(HttpSession session, HttpServletRequest request) {
        if (isBackOfficeRequest(request)) {
            return null;
        }

        Object obj = session.getAttribute("shippingLocation");
        if (obj == null) {
            obj = session.getAttribute("shippingLocationId");
        }
        if (obj == null) {
            return null;
        }
        if (obj instanceof ShippingLocation location) {
            return location;
        }
        try {
            if (obj instanceof Long locationId) {
                return shippingLocationService.getById(locationId);
            }
            if (obj instanceof String value) {
                return resolveLocation(value);
            }
        } catch (RuntimeException ex) {
            LOGGER.warn("Unable to resolve current shipping location for {}", requestPath(request), ex);
        }
        return null;
    }

    private ShippingLocation resolveLocation(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return shippingLocationService.getById(Long.valueOf(value.trim()));
        } catch (NumberFormatException ignored) {
            String normalized = value.trim();
            return shippingLocationService.getActiveLocations().stream()
                    .filter(location -> normalized.equalsIgnoreCase(location.getCode())
                    || normalized.equalsIgnoreCase(location.getName())
                    || normalized.equalsIgnoreCase(location.getDisplayLabel()))
                    .findFirst()
                    .orElse(null);
        }
    }

    @ModelAttribute("currentShippingDistrictId")
    public Long currentShippingDistrictId(HttpSession session, HttpServletRequest request) {
        ShippingLocation location = currentShippingLocation(session, request);
        if (location == null) {
            return null;
        }
        if (location.getType() == ShippingLocationType.DISTRICT) {
            return location.getId();
        }
        if (location.getType() == ShippingLocationType.THANA && location.getParent() != null) {
            return location.getParent().getId();
        }
        return null;
    }

    @ModelAttribute("autoShowLocationPicker")
    public boolean autoShowLocationPicker(HttpSession session, HttpServletRequest request) {
        if (isBackOfficeRequest(request) || currentShippingLocation(session, request) != null) {
            return false;
        }

        String path = requestPath(request);
        return !path.startsWith("/cart")
                && !path.startsWith("/carts")
                && !path.startsWith("/district");
    }

    private void addEmptyWishlistAttributes(Model model) {
        Set<String> emptySet = Collections.emptySet();
        model.addAttribute("wishlistProductIds", emptySet);
        model.addAttribute("wishlistProductUuids", emptySet);
        model.addAttribute("wishlistProductKeys", emptySet);
        model.addAttribute("wishlistCount", 0);
    }

    private boolean isBackOfficeRequest(HttpServletRequest request) {
        String path = requestPath(request);
        return path.startsWith("/admin")
                || path.startsWith("/vendor")
                || path.startsWith("/api")
                || path.startsWith("/actuator")
                || path.startsWith("/error");
    }

    private String requestPath(HttpServletRequest request) {
        if (request == null || request.getRequestURI() == null) {
            return "";
        }
        String contextPath = request.getContextPath();
        String requestUri = request.getRequestURI();
        if (contextPath != null && !contextPath.isBlank() && requestUri.startsWith(contextPath)) {
            return requestUri.substring(contextPath.length());
        }
        return requestUri;
    }
}
