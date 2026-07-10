/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Controller.java to edit this template
 */
package com.ecommerce.app.vendor.controller;

import com.ecommerce.app.globalServices.ImageService;
import com.ecommerce.app.globalServices.ImageUploadPolicy;
import com.ecommerce.app.globalServices.ImageUploadValidationException;
import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.module.user.services.LoggedUserService;
import com.ecommerce.app.services.StorageProperties;
import com.ecommerce.app.vendor.config.VendorLogoUploadProperties;
import com.ecommerce.app.vendor.model.Vendorprofile;
import com.ecommerce.app.vendor.repository.VendorprofileRepository;
import com.ecommerce.app.vendor.user.componant.VendorUserContext;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 *
 * @author libertyerp_local
 */
@Controller
@RequestMapping("/vendorlogo")
public class VendorLogoController {

    private static final Logger LOGGER = LoggerFactory.getLogger(VendorLogoController.class);
    private static final String VENDOR_LOGO_DIRECTORY = "vendor/logo";
    private static final int VENDOR_LOGO_WIDTH = 300;
    private static final int VENDOR_LOGO_HEIGHT = 300;

    @Autowired
    private LoggedUserService loggedUserService;

    @Autowired
    private VendorprofileRepository vendorprofileRepository;

    @Autowired
    private VendorUserContext vendorUserContext;

    @Autowired
    private ImageService imageService;

    @Autowired
    private StorageProperties storageProperties;

    @Autowired
    private VendorLogoUploadProperties vendorLogoUploadProperties;

    @RequestMapping(value = {"", "/", "/index"})
    public String index(Model model) {
        model.addAttribute("vendorprofile", resolveVendorProfile());
        return "vendor/logo/index";
    }

    @PostMapping("/save")
    public String save(@RequestParam("logoFile") MultipartFile logoFile, RedirectAttributes redirectAttributes) {
        Vendorprofile vendorprofile = resolveVendorProfile();

        if (vendorprofile == null) {
            redirectAttributes.addFlashAttribute("messageType", "warning");
            redirectAttributes.addFlashAttribute("message", "Create your vendor profile before uploading a logo.");
            return "redirect:/vendorprofile/create";
        }

        if (logoFile == null || logoFile.isEmpty()) {
            redirectAttributes.addFlashAttribute("messageType", "warning");
            redirectAttributes.addFlashAttribute("message", "Please select an image file to upload.");
            return "redirect:/vendorlogo/index";
        }

        String previousLogo = vendorprofile.getVendorLogo();
        String storedLogoPath = null;

        try {
            String fileName = imageService.validateAndRename(logoFile, vendorLogoUploadPolicy());
            storedLogoPath = VENDOR_LOGO_DIRECTORY + "/" + fileName;
            imageService.resizeAndUpload(logoFile, VENDOR_LOGO_WIDTH, VENDOR_LOGO_HEIGHT, VENDOR_LOGO_DIRECTORY, fileName);

            vendorprofile.setVendorLogo(storedLogoPath);

            Vendorprofile savedVendorProfile = vendorprofileRepository.save(vendorprofile);
            vendorUserContext.setActiveVendor(savedVendorProfile);

            deleteStoredLogo(previousLogo, storedLogoPath);

            redirectAttributes.addFlashAttribute("messageType", "success");
            redirectAttributes.addFlashAttribute("message", "Vendor logo uploaded successfully.");
        } catch (ImageUploadValidationException e) {
            redirectAttributes.addFlashAttribute("messageType", "warning");
            redirectAttributes.addFlashAttribute("message", e.getMessage());
        } catch (IOException e) {
            deleteStoredFile(storedLogoPath);
            LOGGER.error("Vendor logo upload failed for vendor profile {}", vendorprofile.getId(), e);
            redirectAttributes.addFlashAttribute("messageType", "danger");
            redirectAttributes.addFlashAttribute("message", "Vendor logo upload failed. Please use a valid JPG or PNG image and try again.");
        }

        return "redirect:/vendorlogo/index";
    }

    @PostMapping("/delete")
    public String delete(RedirectAttributes redirectAttributes) {
        Vendorprofile vendorprofile = resolveVendorProfile();

        if (vendorprofile == null) {
            redirectAttributes.addFlashAttribute("messageType", "warning");
            redirectAttributes.addFlashAttribute("message", "Create your vendor profile before managing a logo.");
            return "redirect:/vendorprofile/create";
        }

        if (vendorprofile.getVendorLogo() == null || vendorprofile.getVendorLogo().isBlank()) {
            redirectAttributes.addFlashAttribute("messageType", "warning");
            redirectAttributes.addFlashAttribute("message", "There is no vendor logo to delete.");
            return "redirect:/vendorlogo/index";
        }

        String previousLogo = vendorprofile.getVendorLogo();
        vendorprofile.setVendorLogo(null);

        Vendorprofile savedVendorProfile = vendorprofileRepository.save(vendorprofile);
        vendorUserContext.setActiveVendor(savedVendorProfile);
        deleteStoredFile(previousLogo);

        redirectAttributes.addFlashAttribute("messageType", "success");
        redirectAttributes.addFlashAttribute("message", "Vendor logo deleted successfully.");
        return "redirect:/vendorlogo/index";
    }

    private Vendorprofile resolveVendorProfile() {
        Vendorprofile activeVendor = vendorUserContext.getActiveVendor();

        if (activeVendor != null && activeVendor.getId() != null) {
            Optional<Vendorprofile> vendorFromSession = vendorprofileRepository.findById(activeVendor.getId());
            if (vendorFromSession.isPresent()) {
                Vendorprofile vendorprofile = vendorFromSession.get();
                vendorUserContext.setActiveVendor(vendorprofile);
                return vendorprofile;
            }
        }

        Users users = new Users();
        users.setId(loggedUserService.activeUserid());

        List<Vendorprofile> vendorprofiles = vendorprofileRepository.findByUserId(users);
        if (vendorprofiles.isEmpty()) {
            vendorUserContext.setActiveVendor(null);
            return null;
        }

        Vendorprofile vendorprofile = vendorprofiles.get(0);
        vendorUserContext.setActiveVendor(vendorprofile);
        return vendorprofile;
    }

    private void deleteStoredLogo(String previousLogo, String currentLogo) {
        if (previousLogo == null || previousLogo.isBlank() || previousLogo.equals(currentLogo)) {
            return;
        }

        deleteStoredFile(previousLogo);
    }

    private void deleteStoredFile(String storedFilePath) {
        File storedFile = resolveStoredFile(storedFilePath);
        if (storedFile != null && storedFile.exists()) {
            storedFile.delete();
        }
    }

    private File resolveStoredFile(String storedFilePath) {
        if (storedFilePath == null || storedFilePath.isBlank()) {
            return null;
        }

        String normalizedPath = storedFilePath.startsWith("/files/")
                ? storedFilePath.substring("/files/".length())
                : storedFilePath;

        return new File(storageProperties.getRootPath(), normalizedPath.replace("/", File.separator));
    }

    private ImageUploadPolicy vendorLogoUploadPolicy() {
        return new ImageUploadPolicy(
                Set.of("image/jpeg", "image/png"),
                Set.of("jpg", "jpeg", "png"),
                Set.of("jpeg", "png"),
                vendorLogoUploadProperties.getMaxFileSizeBytes(),
                vendorLogoUploadProperties.getMaxWidth(),
                vendorLogoUploadProperties.getMaxHeight(),
                "JPG or PNG images"
        );
    }

}
