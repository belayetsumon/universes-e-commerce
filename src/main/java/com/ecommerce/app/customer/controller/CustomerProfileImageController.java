/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecommerce.app.customer.controller;

import com.ecommerce.app.model.ProfileImage;
import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.module.user.services.LoggedUserService;
import com.ecommerce.app.ripository.ProfileImageRepository;
import com.ecommerce.app.services.StorageProperties;
import jakarta.validation.Valid;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 *
 * @author User
 */
@Controller
@RequestMapping("/customerprofileimage")
public class CustomerProfileImageController {

    @Autowired
    ProfileImageRepository profileImageRepository;

    @Autowired
    LoggedUserService loggedUserService;

    @Autowired
    StorageProperties properties;

    @RequestMapping(value = {"", "/", "/index"})
    public String add(Model model, ProfileImage profileImage) {

        Users userss = new Users();
        userss.setId(loggedUserService.activeUserid());
        profileImage.setUserId(userss);

        model.addAttribute("customerprofileimage", profileImageRepository.findByUserId(userss));

        return "customer/profileimage_add";
    }

    @RequestMapping("/save")
    //@Transactional
    public String save(Model model, @Valid ProfileImage profileImage, BindingResult bindingResult, RedirectAttributes redirectAttributes,
            @RequestParam("pic") MultipartFile pic) {

        if (bindingResult.hasErrors()) {
            Users userss = new Users();
            userss.setId(loggedUserService.activeUserid());
            profileImage.setUserId(userss);
            return "customer/profileimage_add";
        }

        if (!pic.isEmpty()) {
            try {
                byte[] bytes = pic.getBytes();

                // Creating the directory to store file
                File dir = new File(properties.getRootPath());
                if (!dir.exists()) {
                    dir.mkdirs();
                }

                long datenow = System.currentTimeMillis();
                String filename = datenow + "_" + pic.getOriginalFilename();
                // Create the file on server
                File serverFile = new File(dir.getAbsolutePath()
                        + File.separator + filename);

                BufferedImage originalImage;
                originalImage = ImageIO.read(pic.getInputStream());

                Thumbnails.of(originalImage)
                        .forceSize(300, 225)
                        .toFile(serverFile);

                model.addAttribute("message", "You successfully uploaded");

                profileImage.setImageName(filename);

                profileImageRepository.save(profileImage);
                return "redirect:/customerprofileimage/index";
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("message", pic.getOriginalFilename() + " => " + e.getMessage());
                return "redirect:/customerprofileimage/index";
            }
        } else if (pic.isEmpty() && profileImage.getId() != null) {

            ProfileImage profileImages = profileImageRepository.getReferenceById(profileImage.getId());

            profileImage.setImageName(profileImages.getImageName());
            profileImageRepository.save(profileImage);

            redirectAttributes.addFlashAttribute("message", "Successfully saved.");

            return "redirect:/customerprofileimage/index";

        } else {
            redirectAttributes.addFlashAttribute("message", "File empty");
            return "redirect:/customerprofileimage/index";
        }
    }

    @RequestMapping("/delete/{id}")

    public String delete(Model model, @PathVariable Long id, ProfileImage profileImage, RedirectAttributes redirectAttributes) {

        profileImageRepository.deleteById(id);

        redirectAttributes.addFlashAttribute("message", "Deleted successfully.");

        return "redirect:/customerprofileimage/index";
    }

}
