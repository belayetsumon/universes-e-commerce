/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecommerce.app.module.user.controller;

import com.ecommerce.app.exception.ForeignKeyConstraintException;
import com.ecommerce.app.module.ReferralRewards.services.ReferralService;
import com.ecommerce.app.module.ReferralRewards.model.Referral;
import com.ecommerce.app.module.ReferralRewards.model.Wallet;
import com.ecommerce.app.module.ReferralRewards.repository.ReferralRepository;
import com.ecommerce.app.module.ReferralRewards.repository.WalletRepository;
import com.ecommerce.app.module.user.componant.UserValidator;
import com.ecommerce.app.module.user.dto.AdminUserPasswordForm;
import com.ecommerce.app.module.user.model.LoginHistory;
import com.ecommerce.app.module.user.model.LoginStatus;
import com.ecommerce.app.module.user.model.Role;
import com.ecommerce.app.module.user.model.Status;
import com.ecommerce.app.module.user.model.UserType;
import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.module.user.ripository.LoginHistoryRepository;
import com.ecommerce.app.module.user.ripository.RoleRepository;
import com.ecommerce.app.module.user.ripository.UsersRepository;
import com.ecommerce.app.module.user.services.LoginEventService;
import com.ecommerce.app.module.user.services.UsersService;
import jakarta.servlet.http.*;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 *
 * @author Md Belayet Hossin
 */
@Controller
@RequestMapping("/users")
//@PreAuthorize("hasAuthority('users')")
public class UsersController {

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    UsersRepository usersRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    LoginHistoryRepository loginHistoryRepository;

    @Autowired
    UserValidator userValidator;

    @Autowired
    LoginEventService loginEventService;

    @Autowired
    private ReferralRepository referralRepository;

    @Autowired
    UsersService usersService;

    @Autowired
    WalletRepository walletRepository;

    @Autowired
    ReferralService referralService;

    @RequestMapping(value = {"", "/", "/index"})
    public String index(
            Model model,
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "status", required = false) Status status,
            @RequestParam(name = "userType", required = false) UserType userType,
            @RequestParam(name = "roleId", required = false) Long roleId,
            @RequestParam(name = "referralFilter", required = false) String referralFilter) {
        String keyword = normalizeKeyword(q);
        List<Users> allUsers = usersRepository.findForAdminListFilters(keyword, status, userType, roleId);
        Map<Long, String> referralCodesByUserId = buildReferralCodeMap(allUsers);
        allUsers = applyReferralFilter(allUsers, referralCodesByUserId, referralFilter);
        model.addAttribute("alluser", allUsers);
        addReferralCodeSummary(model, allUsers, referralCodesByUserId);
        addUserListSummary(model, allUsers);
        addUserFilterModel(model, q, status, userType, roleId, referralFilter);
        return "user/allusers";
    }

    @RequestMapping("/userbystatus")
    public String userByStatus(Model model, @RequestParam(value = "status", required = false) Status status) {
        model.addAttribute("status", Status.values());
        // model.addAttribute("alluser", usersRepository.findByStatus(status));
        return "user/allusers_by_status";
    }

    @RequestMapping("/view/{uid}")
    public String view(Model model, @PathVariable Long uid, RedirectAttributes redirectAttributes) {
        Users user = usersRepository.findById(uid).orElse(null);
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "User not found.");
            return "redirect:/users/index";
        }
        model.addAttribute("users", user);
        return "user/view";
    }

    @GetMapping("/login-history")
    public String loginHistory(
            Model model,
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "loginStatus", required = false) LoginStatus loginStatus,
            @RequestParam(name = "fromDate", required = false) String fromDate,
            @RequestParam(name = "toDate", required = false) String toDate) {
        List<LoginHistory> historyEntries = findLoginHistoryForAdmin(null, q, loginStatus, fromDate, toDate);
        model.addAttribute("historyEntries", historyEntries);
        model.addAttribute("historyTitle", "All User Login History");
        model.addAttribute("historySubtitle", "Recent login, logout, failed attempt, and session activity records across all users.");
        model.addAttribute("backUrl", "/users/index");
        model.addAttribute("backLabel", "Back to Users");
        model.addAttribute("selectedUser", null);
        addLoginHistoryFilterModel(model, q, loginStatus, fromDate, toDate);
        addLoginHistorySummary(model, historyEntries);
        return "user/login_history";
    }

    @GetMapping("/login-history/{uid}")
    public String userLoginHistory(
            Model model,
            @PathVariable Long uid,
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "loginStatus", required = false) LoginStatus loginStatus,
            @RequestParam(name = "fromDate", required = false) String fromDate,
            @RequestParam(name = "toDate", required = false) String toDate,
            RedirectAttributes redirectAttributes) {
        Users user = usersRepository.findById(uid).orElse(null);
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "User not found.");
            return "redirect:/users/index";
        }

        List<LoginHistory> historyEntries = findLoginHistoryForAdmin(uid, q, loginStatus, fromDate, toDate);
        model.addAttribute("historyEntries", historyEntries);
        model.addAttribute("historyTitle", "Login History");
        model.addAttribute("historySubtitle", "Detailed login activity for " + buildDisplayName(user) + ".");
        model.addAttribute("backUrl", "/users/view/" + uid);
        model.addAttribute("backLabel", "Back to Profile");
        model.addAttribute("selectedUser", user);
        addLoginHistoryFilterModel(model, q, loginStatus, fromDate, toDate);
        addLoginHistorySummary(model, historyEntries);
        return "user/login_history";
    }

    @GetMapping("/change-password/{id}")
    public String changePasswordForm(Model model, @PathVariable Long id, RedirectAttributes redirectAttributes) {
        Users user = usersRepository.findById(id).orElse(null);
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "User not found.");
            return "redirect:/users/index";
        }

        model.addAttribute("users", user);
        model.addAttribute("passwordForm", new AdminUserPasswordForm());
        return "user/change_password";
    }

    @PostMapping("/change-password/{id}")
    public String changePassword(
            Model model,
            @PathVariable Long id,
            @Valid @ModelAttribute("passwordForm") AdminUserPasswordForm passwordForm,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        Users user = usersRepository.findById(id).orElse(null);
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "User not found.");
            return "redirect:/users/index";
        }

        if (!bindingResult.hasFieldErrors("confirmPassword")
                && passwordForm.getNewPassword() != null
                && !passwordForm.getNewPassword().equals(passwordForm.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "mismatch", "New password and confirmation password must match.");
        }

        if (!bindingResult.hasFieldErrors("newPassword")
                && passwordMatches(passwordForm.getNewPassword(), user.getPassword())) {
            bindingResult.rejectValue("newPassword", "same", "Choose a new password that is different from the current one.");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("users", user);
            return "user/change_password";
        }

        user.setPassword(bCryptPasswordEncoder.encode(passwordForm.getNewPassword()));
        usersRepository.save(user);

        redirectAttributes.addFlashAttribute("success", "Password updated successfully for " + user.getFirstName() + ".");
        return "redirect:/users/view/" + user.getId();
    }

    @RequestMapping("/registrations")
    public String registrations(Model model, Users users) {
        model.addAttribute("roles", roleRepository.findAll());
        model.addAttribute("status", Status.values());
        model.addAttribute("userTypes", UserType.values());

        return "user/registrations";
    }

    @RequestMapping("/edit/{id}")
    public String edit(Model model, @PathVariable Long id, Users users) {
        model.addAttribute("users", usersRepository.findById(id).orElse(null));
        model.addAttribute("roles", roleRepository.findAll());
        model.addAttribute("status", Status.values());
        model.addAttribute("userTypes", UserType.values());
        return "user/registrations";
    }

    @RequestMapping("/save")
    //@Transactional
    public String save(Model model, @Valid Users users, BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        String submittedPassword = users.getPassword();
        boolean passwordBlank = submittedPassword == null || submittedPassword.isBlank();

        if (users.getId() == null && passwordBlank) {
            bindingResult.rejectValue("password", "required", "Password is required for new users.");
        }

        if (!passwordBlank && submittedPassword.length() < 8) {
            bindingResult.rejectValue("password", "size", "Password must be at least 8 characters.");
        }

        if (bindingResult.hasErrors()) {

            model.addAttribute("roles", roleRepository.findAll());
            model.addAttribute("status", Status.values());
            model.addAttribute("userTypes", UserType.values());
            return "user/registrations";
        }

        // users.setPassword(bCryptPasswordEncoder.encode(users.getPassword()));
        try {

            if (passwordBlank && users.getId() != null) {
                Users existingUser = usersRepository.findById(users.getId()).orElse(null);
                if (existingUser == null) {
                    redirectAttributes.addFlashAttribute("error", "User not found.");
                    return "redirect:/users/index";
                }

                users.setPassword(existingUser.getPassword());
            } else {

                users.setPassword(bCryptPasswordEncoder.encode(submittedPassword));
            }

            usersRepository.save(users);
            return "redirect:/users/index";

        } catch (Exception e) {

            model.addAttribute("roles", roleRepository.findAll());
            model.addAttribute("status", Status.values());
            model.addAttribute("userTypes", UserType.values());
            model.addAttribute("error", "Unable to save user. Please verify unique email, unique mobile number, and required access fields.");
            return "user/registrations";
        }
    }

    @RequestMapping("/delete/{id}")
    public String delete(Model model, @PathVariable Long id, Users users) {
        usersRepository.deleteById(id);
        return "redirect:/users/index";
    }

    @GetMapping("/deletewithexception/{id}")
    public String deletewithexception(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            usersService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "User deleted successfully!");
        } catch (ForeignKeyConstraintException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }

        return "redirect:/users/index";
    }

    @PostMapping("/generate-referral-code/{id}")
    public String generateReferralCode(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Referral referral = referralService.generateMissingReferralCodeForCustomer(id);
            String referralCode = referral == null ? "" : referral.getReferralCode();
            redirectAttributes.addFlashAttribute(
                    "success",
                    referralCode == null || referralCode.isBlank()
                            ? "Referral code generated successfully."
                            : "Referral code is ready: " + referralCode);
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", "Unable to generate referral code. Please try again.");
        }

        return "redirect:/users/index";
    }

    @RequestMapping("/login")
    public String login(Model model) {
        model.addAttribute("attribute", "value");
        model.addAttribute("logout", " You are successfully logout");
        return "user/login";
    }

    @RequestMapping("/logout")
    public String logout(Model model, HttpServletRequest request, HttpServletResponse response) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            new SecurityContextLogoutHandler().logout(request, response, authentication);
        }
        return "redirect:/";
    }

    @RequestMapping("/detailsinfo/{id}")
    public String details(Model model, @PathVariable Long id) {
        model.addAttribute("employee", usersRepository.findById(id));
        return "pims/details/details";
    }

    @RequestMapping("/uregistrations")
    public String uregistrations(Model model, Users users) {

        model.addAttribute("role", roleRepository.findAll());
        return "user/uregistrations";
    }

    @RequestMapping("/usave")
    public String usave(Model model, @Valid Users users, BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {

            model.addAttribute("role", roleRepository.findAll());
            return "user/uregistrations";
        }
        users.setStatus(Status.Pending);
        users.setPassword(bCryptPasswordEncoder.encode(users.getPassword()));
        usersRepository.save(users);
        redirectAttributes.addAttribute("success", " Congratulations you have successfully registered. please contact with system adminstrator.");
        return "redirect:users/uregistrations";
    }

    @RequestMapping("/frontRegistrationSave")
    public String frontUserSave(Model model, @Valid Users users, BindingResult bindingResult, RedirectAttributes redirectAttributes,
            @RequestParam(name = "parent", required = false) String parent,
            @RequestParam(name = "ref_code", required = false) String ref,
            HttpSession session
    ) {
        // System.out.println("ref_code" + ref);
        //userValidator.validate(users, bindingResult);
        //   Users parents = usersRepository.findByReferralcode(parent);
//        if (parents == null) {
//
//            ObjectError cartItemListError;
//
//            cartItemListError = new ObjectError("parent", "Your referral code is invalid");
//
//            bindingResult.addError(cartItemListError);
//        }
        if (bindingResult.hasErrors()) {

            return "frontview/front-registration";
        }

        Set<Role> customerRole = new HashSet<Role>();
        Role role = roleRepository.findBySlug("customer");
        customerRole.add(role);

        users.setRole(customerRole);

        users.setUserType(UserType.customer);

        users.setStatus(Status.Active);

        users.setPassword(bCryptPasswordEncoder.encode(users.getPassword()));

        usersRepository.save(users);

        String resolvedReferralCode = resolveRegistrationReferralCode(ref, session);
        Users referringUsers = referralService.resolveReferrerByCode(resolvedReferralCode);
        referralService.createReferralProfileAndGrantSignupReward(users, referringUsers);
        redirectAttributes.addFlashAttribute(
                "success", "Congratulations! You have successfully registered.");

        return "redirect:/public/member-login";
    }

    private String resolveRegistrationReferralCode(String submittedReferralCode, HttpSession session) {
        if (submittedReferralCode != null && !submittedReferralCode.isBlank()) {
            return submittedReferralCode.trim();
        }
        if (session == null) {
            return null;
        }
        Object sharedProductReferralCode = session.getAttribute("productShareReferralCode");
        return sharedProductReferralCode instanceof String ? ((String) sharedProductReferralCode).trim() : null;
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

    private String buildDisplayName(Users user) {
        if (user == null) {
            return "Unknown User";
        }

        String firstName = user.getFirstName() == null ? "" : user.getFirstName().trim();
        String lastName = user.getLastName() == null ? "" : user.getLastName().trim();
        String fullName = (firstName + " " + lastName).trim();
        if (!fullName.isEmpty()) {
            return fullName;
        }

        if (user.getEmail() != null && !user.getEmail().isBlank()) {
            return user.getEmail().trim();
        }

        if (user.getMobile() != null && !user.getMobile().isBlank()) {
            return user.getMobile().trim();
        }

        return "User #" + user.getId();
    }

    private List<LoginHistory> findLoginHistoryForAdmin(
            Long userId,
            String q,
            LoginStatus loginStatus,
            String fromDate,
            String toDate) {
        return loginHistoryRepository.findForAdminListFilters(
                userId,
                normalizeKeyword(q),
                loginStatus,
                parseLoginHistoryStartDate(fromDate),
                parseLoginHistoryEndDate(toDate));
    }

    private void addLoginHistoryFilterModel(
            Model model,
            String q,
            LoginStatus loginStatus,
            String fromDate,
            String toDate) {
        model.addAttribute("loginHistorySearch", q == null ? "" : q.trim());
        model.addAttribute("selectedLoginStatus", loginStatus);
        model.addAttribute("selectedFromDate", normalizeDateInput(fromDate));
        model.addAttribute("selectedToDate", normalizeDateInput(toDate));
        model.addAttribute("loginStatuses", LoginStatus.values());
    }

    private LocalDateTime parseLoginHistoryStartDate(String value) {
        LocalDate date = parseDateInput(value);
        return date == null ? null : date.atStartOfDay();
    }

    private LocalDateTime parseLoginHistoryEndDate(String value) {
        LocalDate date = parseDateInput(value);
        return date == null ? null : date.plusDays(1).atStartOfDay();
    }

    private String normalizeDateInput(String value) {
        LocalDate date = parseDateInput(value);
        return date == null ? "" : date.toString();
    }

    private LocalDate parseDateInput(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    private void addLoginHistorySummary(Model model, List<LoginHistory> historyEntries) {
        List<LoginHistory> safeEntries = historyEntries == null ? List.of() : historyEntries;
        long activeSessions = safeEntries.stream()
                .filter(entry -> entry.getLoginStatus() == LoginStatus.ACTIVE)
                .count();
        long failedAttempts = safeEntries.stream()
                .filter(entry -> entry.getLoginStatus() == LoginStatus.FAILED)
                .count();
        long expiredSessions = safeEntries.stream()
                .filter(entry -> entry.getLoginStatus() == LoginStatus.SESSION_EXPIRED)
                .count();
        long loggedOutSessions = safeEntries.stream()
                .filter(entry -> entry.getLoginStatus() == LoginStatus.LOGGED_OUT)
                .count();
        Object latestLoginTime = safeEntries.stream()
                .filter(entry -> entry.getLoginTime() != null)
                .max(Comparator.comparing(LoginHistory::getLoginTime))
                .map(LoginHistory::getLoginTime)
                .orElse(null);

        model.addAttribute("historyCount", safeEntries.size());
        model.addAttribute("activeSessionCount", activeSessions);
        model.addAttribute("failedAttemptCount", failedAttempts);
        model.addAttribute("expiredSessionCount", expiredSessions);
        model.addAttribute("loggedOutSessionCount", loggedOutSessions);
        model.addAttribute("latestLoginTime", latestLoginTime);
    }

    private void addUserListSummary(Model model, List<Users> users) {
        List<Users> safeUsers = users == null ? List.of() : users;
        long activeUsers = safeUsers.stream()
                .filter(user -> user.getStatus() == Status.Active)
                .count();
        long pendingUsers = safeUsers.stream()
                .filter(user -> user.getStatus() == Status.Pending)
                .count();
        long blockedUsers = safeUsers.stream()
                .filter(user -> user.getStatus() == Status.Block)
                .count();
        long customerUsers = safeUsers.stream()
                .filter(user -> user.getUserType() == UserType.customer)
                .count();
        long adminManagedUsers = safeUsers.stream()
                .filter(user -> user.getUserType() != UserType.customer)
                .count();

        model.addAttribute("userCount", safeUsers.size());
        model.addAttribute("activeUserCount", activeUsers);
        model.addAttribute("pendingUserCount", pendingUsers);
        model.addAttribute("blockedUserCount", blockedUsers);
        model.addAttribute("customerUserCount", customerUsers);
        model.addAttribute("adminManagedUserCount", adminManagedUsers);
    }

    private void addUserFilterModel(
            Model model,
            String q,
            Status status,
            UserType userType,
            Long roleId,
            String referralFilter) {
        model.addAttribute("userSearch", q == null ? "" : q.trim());
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedUserType", userType);
        model.addAttribute("selectedRoleId", roleId);
        model.addAttribute("selectedReferralFilter", normalizeReferralFilter(referralFilter));
        model.addAttribute("statuses", Status.values());
        model.addAttribute("userTypes", UserType.values());
        model.addAttribute("roles", roleRepository.findAll());
    }

    private void addReferralCodeSummary(Model model, List<Users> users, Map<Long, String> referralCodesByUserId) {
        List<Users> safeUsers = users == null ? List.of() : users;
        Map<Long, String> safeReferralCodesByUserId = referralCodesByUserId == null ? Map.of() : referralCodesByUserId;
        long missingCustomerReferralCodeCount = safeUsers.stream()
                .filter(user -> user != null && user.getUserType() == UserType.customer)
                .filter(user -> hasNoReferralCode(safeReferralCodesByUserId.get(user.getId())))
                .count();

        model.addAttribute("referralCodesByUserId", safeReferralCodesByUserId);
        model.addAttribute("missingCustomerReferralCodeCount", missingCustomerReferralCodeCount);
    }

    private Map<Long, String> buildReferralCodeMap(List<Users> users) {
        List<Users> safeUsers = users == null ? List.of() : users;
        Set<Long> userIds = new HashSet<>();
        for (Users user : safeUsers) {
            if (user != null && user.getId() != null) {
                userIds.add(user.getId());
            }
        }

        Map<Long, String> referralCodesByUserId = new HashMap<>();
        if (!userIds.isEmpty()) {
            for (Referral referral : referralRepository.findAllByUsers_IdIn(userIds)) {
                if (referral.getUsers() != null && referral.getUsers().getId() != null) {
                    referralCodesByUserId.put(referral.getUsers().getId(), referral.getReferralCode());
                }
            }
        }

        return referralCodesByUserId;
    }

    private List<Users> applyReferralFilter(List<Users> users, Map<Long, String> referralCodesByUserId, String referralFilter) {
        String normalizedReferralFilter = normalizeReferralFilter(referralFilter);
        if (normalizedReferralFilter == null) {
            return users;
        }

        Map<Long, String> safeReferralCodesByUserId = referralCodesByUserId == null ? Map.of() : referralCodesByUserId;
        return (users == null ? List.<Users>of() : users).stream()
                .filter(user -> user != null && user.getUserType() == UserType.customer)
                .filter(user -> {
                    boolean missing = hasNoReferralCode(safeReferralCodesByUserId.get(user.getId()));
                    return "missing".equals(normalizedReferralFilter) ? missing : !missing;
                })
                .toList();
    }

    private String normalizeKeyword(String q) {
        if (q == null || q.isBlank()) {
            return null;
        }
        return "%" + q.trim().toLowerCase(Locale.ROOT) + "%";
    }

    private String normalizeReferralFilter(String referralFilter) {
        if (referralFilter == null || referralFilter.isBlank()) {
            return null;
        }
        String normalizedReferralFilter = referralFilter.trim().toLowerCase(Locale.ROOT);
        return "missing".equals(normalizedReferralFilter) || "generated".equals(normalizedReferralFilter)
                ? normalizedReferralFilter
                : null;
    }

    private boolean hasNoReferralCode(String referralCode) {
        return referralCode == null || referralCode.isBlank();
    }

}
