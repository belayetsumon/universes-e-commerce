package com.ecommerce.app.module.customer.controller;

import com.ecommerce.app.module.customer.dto.CustomerAccountForm;
import com.ecommerce.app.module.customer.dto.CustomerBillingAddressForm;
import com.ecommerce.app.module.customer.dto.CustomerPasswordForm;
import com.ecommerce.app.module.ReferralRewards.model.Referral;
import com.ecommerce.app.module.ReferralRewards.repository.ReferralRepository;
import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.module.user.ripository.UsersRepository;
import com.ecommerce.app.module.user.services.LoggedUserService;
import com.ecommerce.app.order.model.BillingAddress;
import com.ecommerce.app.order.repository.BillingAddressRepository;
import com.ecommerce.app.services.BarcodeService;
import jakarta.validation.Valid;
import com.google.zxing.BarcodeFormat;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/customer-profile")
@PreAuthorize("hasAuthority('customer')")
public class CustomerProfileController {

    private static final String PROFILE_VIEW = "customer/profile";

    @Autowired
    LoggedUserService loggedUserService;

    @Autowired
    UsersRepository usersRepository;

    @Autowired
    BillingAddressRepository billingAddressRepository;

    @Autowired
    ReferralRepository referralRepository;

    @Autowired
    BarcodeService barcodeService;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @GetMapping(value = {"", "/", "/index"})
    public String index(Model model) {
        Users currentUser = currentUser();
        BillingAddress currentBillingAddress = latestBillingAddress(currentUser);
        String referralCode = referralCode(currentUser);
        populateProfilePage(
                model,
                currentUser,
                CustomerAccountForm.fromUser(currentUser),
                new CustomerPasswordForm(),
                CustomerBillingAddressForm.from(currentUser, currentBillingAddress),
                currentBillingAddress,
                referralCode);
        return PROFILE_VIEW;
    }

    @PostMapping("/update")
    public String updateAccount(
            @Valid @ModelAttribute("accountForm") CustomerAccountForm accountForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        Users currentUser = currentUser();

        Users duplicateMobileUser = usersRepository.findByMobile(accountForm.getMobile());
        if (duplicateMobileUser != null && !duplicateMobileUser.getId().equals(currentUser.getId())) {
            bindingResult.rejectValue("mobile", "duplicate", "This mobile number is already used by another account.");
        }

        if (bindingResult.hasErrors()) {
            BillingAddress currentBillingAddress = latestBillingAddress(currentUser);
            String referralCode = referralCode(currentUser);
            populateProfilePage(
                    model,
                    currentUser,
                    accountForm,
                    new CustomerPasswordForm(),
                    CustomerBillingAddressForm.from(currentUser, currentBillingAddress),
                    currentBillingAddress,
                    referralCode);
            return PROFILE_VIEW;
        }

        currentUser.setFirstName(accountForm.getFirstName().trim());
        currentUser.setLastName(accountForm.getLastName().trim());
        currentUser.setMobile(accountForm.getMobile().trim());
        usersRepository.save(currentUser);

        redirectAttributes.addFlashAttribute("accountSuccess", "Your profile has been updated successfully.");
        return "redirect:/customer-profile/index";
    }

    @PostMapping("/update-billing")
    public String updateBillingAddress(
            @Valid @ModelAttribute("billingAddressForm") CustomerBillingAddressForm billingAddressForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        Users currentUser = currentUser();
        BillingAddress currentBillingAddress = latestBillingAddress(currentUser);
        String referralCode = referralCode(currentUser);

        if (bindingResult.hasErrors()) {
            populateProfilePage(
                    model,
                    currentUser,
                    CustomerAccountForm.fromUser(currentUser),
                    new CustomerPasswordForm(),
                    billingAddressForm,
                    currentBillingAddress,
                    referralCode);
            return PROFILE_VIEW;
        }

        BillingAddress target = currentBillingAddress != null ? currentBillingAddress : new BillingAddress();
        target.setUserId(currentUser);
        target.setFirstName(billingAddressForm.getFirstName().trim());
        target.setLastName(billingAddressForm.getLastName().trim());
        target.setEmail(billingAddressForm.getEmail().trim());
        target.setMobile(billingAddressForm.getMobile().trim());
        target.setCompany(trimToNull(billingAddressForm.getCompany()));
        target.setAddressLineOne(billingAddressForm.getAddressLineOne().trim());
        target.setAddressLinetwo(trimToNull(billingAddressForm.getAddressLinetwo()));
        target.setCity(billingAddressForm.getCity().trim());
        target.setPostCode(trimToNull(billingAddressForm.getPostCode()));
        target.setCountry(billingAddressForm.getCountry().trim());
        target.setDistrict(billingAddressForm.getDistrict().trim());
        billingAddressRepository.save(target);

        redirectAttributes.addFlashAttribute("billingSuccess", "Your billing address has been updated successfully.");
        return "redirect:/customer-profile/index";
    }

    @PostMapping("/change-password")
    public String changePassword(
            @Valid @ModelAttribute("passwordForm") CustomerPasswordForm passwordForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        Users currentUser = currentUser();
        BillingAddress currentBillingAddress = latestBillingAddress(currentUser);
        String referralCode = referralCode(currentUser);

        if (!bindingResult.hasFieldErrors("currentPassword")
                && !passwordMatches(passwordForm.getCurrentPassword(), currentUser.getPassword())) {
            bindingResult.rejectValue("currentPassword", "mismatch", "Your current password did not match.");
        }

        if (!bindingResult.hasFieldErrors("newPassword")
                && passwordForm.getNewPassword() != null
                && passwordForm.getCurrentPassword() != null
                && passwordForm.getNewPassword().equals(passwordForm.getCurrentPassword())) {
            bindingResult.rejectValue("newPassword", "same", "Choose a new password that is different from the current one.");
        }

        if (!bindingResult.hasFieldErrors("newPassword")
                && !bindingResult.hasFieldErrors("confirmPassword")
                && passwordForm.getNewPassword() != null
                && !passwordForm.getNewPassword().equals(passwordForm.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "mismatch", "New password and confirmation password must match.");
        }

        if (bindingResult.hasErrors()) {
            populateProfilePage(
                    model,
                    currentUser,
                    CustomerAccountForm.fromUser(currentUser),
                    passwordForm,
                    CustomerBillingAddressForm.from(currentUser, currentBillingAddress),
                    currentBillingAddress,
                    referralCode);
            return PROFILE_VIEW;
        }

        currentUser.setPassword(bCryptPasswordEncoder.encode(passwordForm.getNewPassword()));
        usersRepository.save(currentUser);

        redirectAttributes.addFlashAttribute("passwordSuccess", "Your password has been changed successfully.");
        return "redirect:/customer-profile/index";
    }

    private void populateProfilePage(
            Model model,
            Users user,
            CustomerAccountForm accountForm,
            CustomerPasswordForm passwordForm,
            CustomerBillingAddressForm billingAddressForm,
            BillingAddress billingAddress,
            String referralCode) {
        String referralRegistrationUrl = referralCode == null || referralCode.isBlank()
                ? "/customerregister/register"
                : "/customerregister/register?ref=" + URLEncoder.encode(referralCode, StandardCharsets.UTF_8);
        String referralInviteMessage = referralCode == null || referralCode.isBlank()
                ? "Register on our site and start shopping: /customerregister/register"
                : "Register on our site using my referral code " + referralCode
                        + " to join and buy products: " + referralRegistrationUrl;
        String referralWhatsAppUrl = "https://wa.me/?text="
                + URLEncoder.encode(referralInviteMessage, StandardCharsets.UTF_8);
        String referralFacebookUrl = "https://www.facebook.com/sharer/sharer.php?u="
                + URLEncoder.encode(referralRegistrationUrl, StandardCharsets.UTF_8);
        String referralQrBase64 = referralCode == null || referralCode.isBlank()
                ? null
                : barcodeDataUri(referralRegistrationUrl, BarcodeFormat.QR_CODE, 160, 160);
        model.addAttribute("pageTitle", "My Profile");
        model.addAttribute("username", loggedUserService.activeUserName());
        model.addAttribute("customerUser", user);
        model.addAttribute("accountForm", accountForm);
        model.addAttribute("passwordForm", passwordForm);
        model.addAttribute("billingAddressForm", billingAddressForm);
        model.addAttribute("customerBillingAddress", billingAddress);
        model.addAttribute("referralCode", referralCode);
        model.addAttribute("referralRegistrationUrl", referralRegistrationUrl);
        model.addAttribute("referralInviteMessage", referralInviteMessage);
        model.addAttribute("referralWhatsAppUrl", referralWhatsAppUrl);
        model.addAttribute("referralFacebookUrl", referralFacebookUrl);
        model.addAttribute("referralQrBase64", referralQrBase64);
    }

    private Users currentUser() {
        Long activeUserId = loggedUserService.activeUserid();
        Optional<Users> user = usersRepository.findById(activeUserId);
        return user.orElseThrow(() -> new IllegalStateException("Authenticated customer account was not found."));
    }

    private BillingAddress latestBillingAddress(Users user) {
        return billingAddressRepository.findFirstByUserId_IdOrderByIdDesc(user.getId()).orElse(null);
    }

    private String referralCode(Users user) {
        return referralRepository.findByUsers(user)
                .map(Referral::getReferralCode)
                .orElse("");
    }

    private boolean passwordMatches(String rawPassword, String storedPassword) {
        if (rawPassword == null || storedPassword == null) {
            return false;
        }

        if (storedPassword.startsWith("$2a$") || storedPassword.startsWith("$2b$") || storedPassword.startsWith("$2y$")) {
            return bCryptPasswordEncoder.matches(rawPassword, storedPassword);
        }

        return rawPassword.equals(storedPassword);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String barcodeDataUri(String value, BarcodeFormat format, int width, int height) {
        try {
            return barcodeService.generateBarcodeBase64(value, format, width, height);
        } catch (Exception ex) {
            return null;
        }
    }
}
