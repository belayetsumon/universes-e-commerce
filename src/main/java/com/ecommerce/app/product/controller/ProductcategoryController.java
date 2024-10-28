/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecommerce.app.product.controller;

import com.ecommerce.app.model.enumvalue.Status;
import com.ecommerce.app.product.model.Productcategory;
import com.ecommerce.app.product.ripository.ProductcategoryRepository;
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
@RequestMapping("/productcategory")
//@PreAuthorize("hasAuthority('productcategory')")
public class ProductcategoryController {

    @Autowired
    StorageProperties properties;

    @Autowired
    ProductcategoryRepository productcategoryRepository;

    @RequestMapping(value = {"", "/", "/index"})
    public String index(Model model) {
        model.addAttribute("productcategorylist", productcategoryRepository.findAll(Sort.by(Sort.Direction.DESC, "id")));
        return "product/productcategory/index";
    }

    @RequestMapping("/create")
    public String create(Model model, Productcategory productcategory) {
        model.addAttribute("statuslist", Status.values());
        model.addAttribute("productcategorylist", productcategoryRepository.findAll(Sort.by(Sort.Direction.DESC, "id")));

        return "product/productcategory/add";
    }

    @RequestMapping("/save")
    public String create(Model model, @Valid Productcategory productcategory, BindingResult bindingResult, RedirectAttributes redirectAttributes,
            @RequestParam("pic") MultipartFile pic
    ) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("statuslist", Status.values());
            model.addAttribute("productcategorylist", productcategoryRepository.findAll(Sort.by(Sort.Direction.DESC, "id")));
            return "product/productcategory/add";
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

                productcategory.setImageName(filename);

                productcategoryRepository.save(productcategory);

                redirectAttributes.addFlashAttribute("message", "Successfully saved.");
                return "redirect:/productcategory/index";
            } catch (Exception e) {

                model.addAttribute("statuslist", Status.values());

                redirectAttributes.addFlashAttribute("message", pic.getOriginalFilename() + " => " + e.getMessage());
                return "redirect:/productcategory/index";
            }
        } else if (pic.isEmpty() && productcategory.getId() != null) {

            Productcategory productcategorys = productcategoryRepository.getOne(productcategory.getId());

            productcategory.setImageName(productcategorys.getImageName());

            productcategoryRepository.save(productcategory);

            redirectAttributes.addFlashAttribute("message", "Successfully saved.");

            return "redirect:/productcategory/index";

        } else {
            productcategoryRepository.save(productcategory);
            redirectAttributes.addFlashAttribute("message", "File empty");
            return "redirect:/productcategory/index";
        }
//        newsRepository.save(news);
//        return "redirect:/news/index";
    }

    @RequestMapping("/details/{id}")
    public String create(Model model, @PathVariable Long id, Productcategory productcategory) {

        model.addAttribute("productcategory_details", productcategoryRepository.getOne(id));

        return "product/productcategory/productcategory_details";

    }

    @RequestMapping("/edit/{id}")
    public String edit(Model model, @PathVariable Long id, Productcategory productcategory) {
        model.addAttribute("productcategory", productcategoryRepository.getOne(id));
        model.addAttribute("statuslist", Status.values());
        return "product/productcategory/add";
    }

    @RequestMapping("/delete/{id}")

    public String delete(Model model, @PathVariable Long id, Productcategory productcategory, RedirectAttributes redirectAttributes) {

        productcategory = productcategoryRepository.getOne(id);
        File file = new File(properties.getRootPath() + File.separator + productcategory.getImageName());

        file.delete();

        productcategoryRepository.deleteById(id);

        redirectAttributes.addFlashAttribute("message", "Deleted successfully.");

        return "redirect:/productcategory/index";
    }

}
