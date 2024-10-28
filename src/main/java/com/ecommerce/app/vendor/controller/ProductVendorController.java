/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecommerce.app.vendor.controller;

import com.ecommerce.app.model.enumvalue.Status;
import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.module.user.services.LoggedUserService;
import com.ecommerce.app.product.model.Product;

import com.ecommerce.app.product.ripository.ProductcategoryRepository;
import com.ecommerce.app.product.ripository.ProductsubcategoryRepository;
import com.ecommerce.app.services.StorageProperties;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.ecommerce.app.product.ripository.ProductRepository;
import jakarta.validation.Valid;

/**
 *
 * @author User
 */
@Controller
@RequestMapping("/productvendor")
//@PreAuthorize("hasAuthority('exam')")
public class ProductVendorController {

    @Autowired
    StorageProperties properties;

    @Autowired
    LoggedUserService loggedUserService;

    @Autowired
    ProductcategoryRepository productcategoryRepository;

    @Autowired
    ProductsubcategoryRepository productsubcategoryRepository;

    @Autowired
    ProductRepository productRepository;

    @RequestMapping(value = {"", "/", "/index"})
    public String index(Model model) {
        model.addAttribute("productlist", productRepository.findAll(Sort.by(Sort.Direction.DESC, "id")));
        return "vendor/product/index";
    }

    @RequestMapping("/create")
    public String create(Model model, Product product) {
        model.addAttribute("statuslist", Status.values());
        model.addAttribute("productcategorylist", productcategoryRepository.findByStatus(Status.Active));

        Users userss = new Users();
        userss.setId(loggedUserService.activeUserid());
        product.setUserId(userss);

        return "vendor/product/add";
    }

    @RequestMapping("/save")
    public String create(Model model, @Valid Product product, BindingResult bindingResult, RedirectAttributes redirectAttributes,
            @RequestParam("pic") MultipartFile pic
    ) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("statuslist", Status.values());
            model.addAttribute("productcategorylist", productcategoryRepository.findByStatus(Status.Active));

            Users userss = new Users();
            userss.setId(loggedUserService.activeUserid());
            product.setUserId(userss);

            return "vendor/product/add";
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

//                BufferedOutputStream stream = new BufferedOutputStream(
//                        new FileOutputStream(serverFile));
//                stream.write(bytes);
//                stream.close();
                BufferedImage originalImage;

                originalImage = ImageIO.read(pic.getInputStream());

                Thumbnails.of(originalImage).forceSize(800, 600).toFile(serverFile);

                model.addAttribute("message", "You successfully uploaded");

                product.setImageName(filename);

                productRepository.save(product);

                redirectAttributes.addFlashAttribute("message", "Successfully saved.");
                return "redirect:/productvendor/index";
            } catch (Exception e) {

                model.addAttribute("statuslist", Status.values());
                model.addAttribute("productcategorylist", productcategoryRepository.findByStatus(Status.Active));

                Users userss = new Users();
                userss.setId(loggedUserService.activeUserid());
                product.setUserId(userss);

                redirectAttributes.addFlashAttribute("message", pic.getOriginalFilename() + " => " + e.getMessage());
                return "redirect:/productvendor/index";
            }
        } else if (pic.isEmpty() && product.getId() != null) {

            Product products = productRepository.findById(product.getId()).orElse(null);

            products.setImageName(product.getImageName());

            productRepository.save(products);

            redirectAttributes.addFlashAttribute("message", "Successfully saved.");

            return "redirect:/productvendor/index";

        } else {
            productRepository.save(product);
            redirectAttributes.addFlashAttribute("message", "File empty");
            return "redirect:/productvendor/index";
        }
//        newsRepository.save(news);
//        return "redirect:/news/index";
    }

    @RequestMapping("/details/{id}")
    public String create(Model model, @PathVariable Long id, Product product) {

        model.addAttribute("product_details", productRepository.getOne(id));

        return "vendor/product/product_details";

    }

    @RequestMapping("/edit/{id}")
    public String edit(Model model, @PathVariable Long id, Product product) {
        model.addAttribute("ourproduct", productRepository.getOne(id));
        model.addAttribute("statuslist", Status.values());
        model.addAttribute("productcategorylist", productcategoryRepository.findByStatus(Status.Active));
         Users userss = new Users();
        userss.setId(loggedUserService.activeUserid());
        product.setUserId(userss);
        return "vendor/product/add";
    }

    @RequestMapping("/delete/{id}")

    public String delete(Model model, @PathVariable Long id, Product product, RedirectAttributes redirectAttributes) {

        product = productRepository.getOne(id);
        File file = new File(properties.getRootPath() + File.separator + product.getImageName());

        file.delete();

        productRepository.deleteById(id);

        redirectAttributes.addFlashAttribute("message", "Deleted successfully.");

        return "redirect:/productvendor/index";
    }

}
