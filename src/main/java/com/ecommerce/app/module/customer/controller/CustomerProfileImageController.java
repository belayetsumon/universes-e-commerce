package com.ecommerce.app.module.customer.controller;

import com.ecommerce.app.model.ProfileImage;
import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.module.user.ripository.UsersRepository;
import com.ecommerce.app.module.user.services.LoggedUserService;
import com.ecommerce.app.ripository.ProfileImageRepository;
import com.ecommerce.app.services.StorageProperties;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import javax.imageio.ImageIO;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/customerprofileimage")
@PreAuthorize("hasAuthority('customer')")
public class CustomerProfileImageController {

    private static final String PROFILE_IMAGE_VIEW = "customer/profileimage_add";

    @Autowired
    private ProfileImageRepository profileImageRepository;

    @Autowired
    private LoggedUserService loggedUserService;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private StorageProperties properties;

    @GetMapping(value = {"", "/", "/index"})
    public String add(Model model) {
        Users currentUser = currentUser();
        ProfileImage currentProfileImage = profileImageRepository.findByUserId(currentUser);

        ProfileImage form = currentProfileImage != null ? currentProfileImage : new ProfileImage();
        form.setUserId(currentUser);

        populatePage(model, currentUser, form, currentProfileImage);
        return PROFILE_IMAGE_VIEW;
    }

    @PostMapping("/save")
    public String save(
            Model model,
            @ModelAttribute("profileImage") ProfileImage profileImage,
            RedirectAttributes redirectAttributes,
            @RequestParam("pic") MultipartFile pic) {

        Users currentUser = currentUser();
        ProfileImage existingProfileImage = profileImageRepository.findByUserId(currentUser);
        profileImage.setUserId(currentUser);

        if (pic == null || pic.isEmpty()) {
            model.addAttribute("uploadError", "Please choose an image before submitting.");
            populatePage(model, currentUser, profileImage, existingProfileImage);
            return PROFILE_IMAGE_VIEW;
        }

        try {
            File dir = new File(properties.getRootPath());
            if (!dir.exists()) {
                dir.mkdirs();
            }

            BufferedImage originalImage = ImageIO.read(pic.getInputStream());
            if (originalImage == null) {
                model.addAttribute("uploadError", "The selected file is not a supported image.");
                populatePage(model, currentUser, profileImage, existingProfileImage);
                return PROFILE_IMAGE_VIEW;
            }

            long timestamp = System.currentTimeMillis();
            String originalFileName = pic.getOriginalFilename() != null ? pic.getOriginalFilename().replaceAll("\\s+", "_") : "profile-image";
            String fileName = timestamp + "_" + originalFileName;
            File serverFile = new File(dir.getAbsolutePath() + File.separator + fileName);

            Thumbnails.of(originalImage)
                    .size(300, 300)
                    .crop(net.coobird.thumbnailator.geometry.Positions.CENTER)
                    .toFile(serverFile);

            ProfileImage target = existingProfileImage != null ? existingProfileImage : new ProfileImage();
            target.setUserId(currentUser);
            String previousImageName = target.getImageName();
            target.setImageName(fileName);
            profileImageRepository.save(target);

            deleteStoredImage(previousImageName);

            redirectAttributes.addFlashAttribute("successMessage", existingProfileImage != null
                    ? "Your profile image has been updated successfully."
                    : "Your profile image has been uploaded successfully.");
            return "redirect:/customerprofileimage/index";
        } catch (IOException e) {
            model.addAttribute("uploadError", "Image upload failed: " + e.getMessage());
            populatePage(model, currentUser, profileImage, existingProfileImage);
            return PROFILE_IMAGE_VIEW;
        }
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Users currentUser = currentUser();
        ProfileImage profileImage = profileImageRepository.findByUserId(currentUser);

        if (profileImage == null || !profileImage.getId().equals(id)) {
            redirectAttributes.addFlashAttribute("uploadError", "Profile image not found for this account.");
            return "redirect:/customerprofileimage/index";
        }

        deleteStoredImage(profileImage.getImageName());
        profileImageRepository.delete(profileImage);
        redirectAttributes.addFlashAttribute("successMessage", "Your profile image has been removed successfully.");
        return "redirect:/customerprofileimage/index";
    }

    private void populatePage(Model model, Users currentUser, ProfileImage formProfileImage, ProfileImage storedProfileImage) {
        model.addAttribute("pageTitle", "Profile Image");
        model.addAttribute("username", loggedUserService.activeUserName());
        model.addAttribute("customerUser", currentUser);
        model.addAttribute("profileImage", formProfileImage);
        model.addAttribute("customerprofileimage", storedProfileImage);
    }

    private Users currentUser() {
        Long activeUserId = loggedUserService.activeUserid();
        Optional<Users> user = usersRepository.findById(activeUserId);
        return user.orElseThrow(() -> new IllegalStateException("Authenticated customer account was not found."));
    }

    private void deleteStoredImage(String imageName) {
        if (imageName == null || imageName.trim().isEmpty()) {
            return;
        }

        File file = new File(properties.getRootPath() + File.separator + imageName);
        if (file.exists()) {
            file.delete();
        }
    }
}
