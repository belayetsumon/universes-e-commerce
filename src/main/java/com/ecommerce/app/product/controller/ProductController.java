/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecommerce.app.product.controller;

import com.ecommerce.app.globalComponant.SlagGenerator;
import com.ecommerce.app.globalComponant.UnixTimeComponent;
import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.module.user.services.LoggedUserService;
import com.ecommerce.app.product.model.Product;
import com.ecommerce.app.product.model.ProductStatusEnum;
import com.ecommerce.app.product.model.ProductTypeEnum;
import com.ecommerce.app.product.ripository.ManufacturerRepository;
import com.ecommerce.app.product.ripository.ProductcategoryRepository;
import com.ecommerce.app.services.StorageProperties;
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
import com.ecommerce.app.product.ripository.ProductRepository;
import com.ecommerce.app.product.services.ProductService;

import com.ecommerce.app.product.services.UnitsOfMeasureService;
import jakarta.validation.Valid;

/**
 *
 * @author User
 */
@Controller
@RequestMapping("/product")
//@PreAuthorize("hasAuthority('exam')")
public class ProductController {

    @Autowired
    StorageProperties properties;

    @Autowired
    LoggedUserService loggedUserService;

    @Autowired
    ProductcategoryRepository productcategoryRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    UnitsOfMeasureService unitsOfMeasureService;

    @Autowired
    private SlagGenerator slagGenerator;

    @Autowired
    UnixTimeComponent unixTimeComponent;


    @Autowired
    ProductService productService;

    @Autowired
    ManufacturerRepository manufacturerRepository;


    @RequestMapping(value = {"", "/", "/index"})
    public String index(Model model) {
        model.addAttribute("productlist", productService.all_Product_for_admin());
        return "product/index";
    }

    @RequestMapping("/create")
    public String create(Model model, Product product) {

        int suk = (int) unixTimeComponent.unixTimeEpochSecond();
        product.setSku(suk);

        Users userss = new Users();
        userss.setId(loggedUserService.activeUserid());
        product.setUserId(userss);

        model.addAttribute("statuslist", ProductStatusEnum.values());
        model.addAttribute("producttype", ProductTypeEnum.values());
        model.addAttribute("uoms", unitsOfMeasureService.getAllUnits());
        model.addAttribute("productcategorylist", productcategoryRepository.findByStatus(ProductStatusEnum.Active));

        model.addAttribute("manufacturerlist", manufacturerRepository.findAll());

        return "product/add";
    }

    @RequestMapping("/save")
    public String create(Model model, @Valid Product product, BindingResult bindingResult, RedirectAttributes redirectAttributes,
            @RequestParam("pic") MultipartFile pic
    ) {

        if (bindingResult.hasErrors()) {
            Users userss = new Users();
            userss.setId(loggedUserService.activeUserid());
            product.setUserId(userss);
            model.addAttribute("statuslist", ProductStatusEnum.values());
            model.addAttribute("producttype", ProductTypeEnum.values());
            model.addAttribute("uoms", unitsOfMeasureService.getAllUnits());
            model.addAttribute("productcategorylist", productcategoryRepository.findByStatus(ProductStatusEnum.Active));

            model.addAttribute("manufacturerlist", manufacturerRepository.findAll());

            return "product/add";
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

                if (product.getId() == null) {
                    String slug = slagGenerator.generateSlug(product.getTitle());
                    product.setSlug(slug);
                }

                productRepository.save(product);

                redirectAttributes.addFlashAttribute("message", "Successfully saved.");
                return "redirect:/product/index";
            } catch (Exception e) {

                model.addAttribute("statuslist", ProductStatusEnum.values());
                model.addAttribute("producttype", ProductTypeEnum.values());

                model.addAttribute("uoms", unitsOfMeasureService.getAllUnits());
                model.addAttribute("productcategorylist", productcategoryRepository.findByStatus(ProductStatusEnum.Active));
                model.addAttribute("manufacturerlist", manufacturerRepository.findAll());

              model.addAttribute("uoms", unitsOfMeasureService.getAllUnits());
                model.addAttribute("productcategorylist", productcategoryRepository.findByStatus(ProductStatusEnum.Active));


                redirectAttributes.addFlashAttribute("message", pic.getOriginalFilename() + " => " + e.getMessage());
                return "redirect:/product/index";
            }
        } else if (pic.isEmpty() && product.getId() != null) {

            Product products = productRepository.findById(product.getId()).orElse(null);

            products.setImageName(product.getImageName());

            productRepository.save(products);

            redirectAttributes.addFlashAttribute("message", "Successfully saved.");

            return "redirect:/product/index";

        } else {

            if (product.getId() == null) {
                String slug = slagGenerator.generateSlug(product.getTitle());
                product.setSlug(slug);
            }
            productRepository.save(product);
            redirectAttributes.addFlashAttribute("message", "File empty");
            return "redirect:/product/index";
        }
//        newsRepository.save(news);
//        return "redirect:/news/index";
    }

    @RequestMapping("/details/{id}")
    public String create(Model model, @PathVariable Long id, Product product) {


        model.addAttribute("product_details", productService.all_Product_for_admin_By_Id(id));

        model.addAttribute("product_details", productRepository.findById(id).orElse(null));

        model.addAttribute("product_details", productRepository.findById(id).orElse(null));


        return "product/product_details";

    }

    @RequestMapping("/edit/{id}")
    public String edit(Model model, @PathVariable Long id, Product product) {
        model.addAttribute("product", productRepository.findById(id).orElse(null));
        model.addAttribute("statuslist", ProductStatusEnum.values());
        model.addAttribute("producttype", ProductTypeEnum.values());

        model.addAttribute("uoms", unitsOfMeasureService.getAllUnits());
        model.addAttribute("productcategorylist", productcategoryRepository.findByStatus(ProductStatusEnum.Active));
        model.addAttribute("manufacturerlist", manufacturerRepository.findAll());
//        Users userss = new Users();
//        userss.setId(loggedUserService.activeUserid());
//        product.setUserId(userss);

         model.addAttribute("uoms", unitsOfMeasureService.getAllUnits());
        model.addAttribute("productcategorylist", productcategoryRepository.findByStatus(ProductStatusEnum.Active));
        Users userss = new Users();
        userss.setId(loggedUserService.activeUserid());
        product.setUserId(userss);

        return "product/add";
    }

    @RequestMapping("/delete/{id}")

    public String delete(Model model, @PathVariable Long id, Product product, RedirectAttributes redirectAttributes) {

        product = productRepository.findById(id).orElse(null);
        File file = new File(properties.getRootPath() + File.separator + product.getImageName());

        file.delete();

        productRepository.deleteById(id);

        redirectAttributes.addFlashAttribute("message", "Deleted successfully.");

        return "redirect:/product/index";
    }

}
