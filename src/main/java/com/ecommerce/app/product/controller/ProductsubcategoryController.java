/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecommerce.app.product.controller;


import com.ecommerce.app.model.enumvalue.Status;
import com.ecommerce.app.product.model.Productsubcategory;
import com.ecommerce.app.product.ripository.ProductcategoryRepository;
import com.ecommerce.app.product.ripository.ProductsubcategoryRepository;
import com.ecommerce.app.services.StorageProperties;
import jakarta.validation.Valid;
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

/**
 *
 * @author User
 */
@Controller
@RequestMapping("/productsubcategory")
//@PreAuthorize("hasAuthority('productsubcategory')")
public class ProductsubcategoryController {

    @Autowired
    StorageProperties properties;

    @Autowired
    ProductcategoryRepository productcategoryRepository;

    @Autowired
    ProductsubcategoryRepository productsubcategoryRepository;

    @RequestMapping(value = {"", "/", "/index"})
    public String index(Model model) {
        model.addAttribute("productsubcategorylist", productsubcategoryRepository.findAll(Sort.by(Sort.Direction.DESC, "id")));
        return "product/productsubcategory/index";
    }

    @RequestMapping("/create")
    public String create(Model model, Productsubcategory productsubcategory) {

        model.addAttribute("productcategorylist", productcategoryRepository.findAll());

        model.addAttribute("statuslist", Status.values());

        return "product/productsubcategory/add";
    }

    @RequestMapping("/save")
    public String create(Model model, @Valid Productsubcategory productsubcategory, BindingResult bindingResult, RedirectAttributes redirectAttributes,
            @RequestParam("pic") MultipartFile pic
    ) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("productcategorylist", productcategoryRepository.findAll());
            model.addAttribute("statuslist", Status.values());
            return "product/productsubcategory/add";
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

                Thumbnails.of(originalImage).forceSize(300, 225).toFile(serverFile);

                model.addAttribute("message", "You successfully uploaded");

                productsubcategory.setImageName(filename);

                productsubcategoryRepository.save(productsubcategory);

                redirectAttributes.addFlashAttribute("message", "Successfully saved.");
                return "redirect:/productsubcategory/index";
            } catch (Exception e) {

                model.addAttribute("productcategorylist", productcategoryRepository.findAll());

                model.addAttribute("statuslist", Status.values());

                redirectAttributes.addFlashAttribute("message", pic.getOriginalFilename() + " => " + e.getMessage());
                return "redirect:/productsubcategory/index";
            }
        } else if (pic.isEmpty() && productsubcategory.getId() != null) {

            Productsubcategory productsubcategorys = productsubcategoryRepository.getOne(productsubcategory.getId());

            productsubcategory.setImageName(productsubcategorys.getImageName());

            productsubcategoryRepository.save(productsubcategory);

            redirectAttributes.addFlashAttribute("message", "Successfully saved.");

            return "redirect:/productsubcategory/index";

        } else {
            productsubcategoryRepository.save(productsubcategory);
            redirectAttributes.addFlashAttribute("message", "File empty");
            return "redirect:/productsubcategory/index";
        }
//        newsRepository.save(news);
//        return "redirect:/news/index";
    }

    @RequestMapping("/details/{id}")
    public String create(Model model, @PathVariable Long id, Productsubcategory productsubcategory) {

        model.addAttribute("productsubcategory_details", productsubcategoryRepository.getOne(id));

        return "product/productsubcategory/productsubcategory_details";

    }

    @RequestMapping("/edit/{id}")
    public String edit(Model model, @PathVariable Long id, Productsubcategory productsubcategory) {
        model.addAttribute("productsubcategory", productsubcategoryRepository.getOne(id));
        model.addAttribute("productcategorylist", productcategoryRepository.findAll());
        model.addAttribute("statuslist", Status.values());
        return "catalog/productsubcategory/add";
    }

    @RequestMapping("/delete/{id}")
    public String delete(Model model, @PathVariable Long id, Productsubcategory productsubcategory, RedirectAttributes redirectAttributes) {

        productsubcategory = productsubcategoryRepository.getOne(id);
        File file = new File(properties.getRootPath() + File.separator + productsubcategory.getImageName());

        productsubcategoryRepository.deleteById(id);

        file.delete();
        redirectAttributes.addFlashAttribute("message", "Deleted successfully.");

        return "redirect:/productsubcategory/index";
    }

}
