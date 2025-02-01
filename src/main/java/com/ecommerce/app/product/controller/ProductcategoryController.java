/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecommerce.app.product.controller;

import com.ecommerce.app.globalComponant.SlagGenerator;
import com.ecommerce.app.model.enumvalue.Status;
import com.ecommerce.app.product.model.ProductStatusEnum;

import com.ecommerce.app.product.model.Productcategory;
import com.ecommerce.app.product.ripository.ProductcategoryRepository;
import com.ecommerce.app.product.services.ProductcategoryService;
import com.ecommerce.app.services.StorageProperties;
import jakarta.validation.Valid;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
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

    @Autowired
    private SlagGenerator slagGenerator;


    @Autowired
    ProductcategoryService productcategoryService;


    @RequestMapping(value = {"", "/", "/index"})
    public String index(Model model) {
        // model.addAttribute("productcategorylist", productcategoryRepository.findAll(Sort.by(Sort.Direction.DESC, "id")));

        // Retrieve all root-level categories (those with no parent)
        List<Productcategory> rootCategories = productcategoryRepository.findAll();
        // Add root categories to the model
        model.addAttribute("categories", rootCategories);
//        List<Productcategory> rootNodes = productcategoryService.getRootNodes();
//
//        List<String> paths = new ArrayList<>();
//        List<Productcategory> treeData = productcategoryRepository.findAll();
//
//        // Generate all paths starting from the root nodes
//        for (Productcategory rootNode : treeData) {
//            generatePaths(rootNode, "", paths);
//        }
//
//        model.addAttribute("rootNodes", paths);

        return "product/productcategory/index";
    }

    // Recursive function to generate all paths
    private void generatePaths(Productcategory node, String currentPath, List<String> paths) {
        // Add current node to the path
        String path = currentPath.isEmpty() ? node.getName() : currentPath + " → " + node.getName();
        paths.add(path);

        // If the node has children, recursively generate paths for them
        if (node.getChildren() != null) {
            for (Productcategory child : node.getChildren()) {
                generatePaths(child, path, paths);
            }
        }
    }

    @RequestMapping("/create")
    public String create(Model model, Productcategory productcategory) {


        model.addAttribute("statuslist", ProductStatusEnum.values());

        model.addAttribute("statuslist",  ProductStatusEnum.values());

        model.addAttribute("statuslist",  ProductStatusEnum.values());

        model.addAttribute("productcategorylist", productcategoryRepository.findAll(Sort.by(Sort.Direction.DESC, "id")));

        List<Productcategory> rootCategories = productcategoryRepository.findByParentIsNull();
        model.addAttribute("categories", rootCategories);

        return "product/productcategory/add";
    }

    @RequestMapping("/save")
    public String create(Model model, @Valid Productcategory productcategory, BindingResult bindingResult, RedirectAttributes redirectAttributes,
            @RequestParam("pic") MultipartFile pic
    ) {

        if (bindingResult.hasErrors()) {

            model.addAttribute("statuslist", ProductStatusEnum.values());

          model.addAttribute("statuslist",  ProductStatusEnum.values());

          model.addAttribute("statuslist",  ProductStatusEnum.values());

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

                if (productcategory.getId() == null) {
                    String slug = slagGenerator.generateSlug(productcategory.getName());
                    productcategory.setSlug(slug);
                }
                productcategoryRepository.save(productcategory);
                redirectAttributes.addFlashAttribute("message", "Successfully saved.");
                return "redirect:/productcategory/index";
            } catch (Exception e) {

                model.addAttribute("statuslist", Status.values());
                redirectAttributes.addFlashAttribute("message", pic.getOriginalFilename() + " => " + e.getMessage());
                return "redirect:/productcategory/index";
            }
        } else if (pic.isEmpty() && productcategory.getId() != null) {
            Productcategory productcategorys = productcategoryRepository.findById(productcategory.getId()).orElse(null);
            productcategory.setImageName(productcategorys.getImageName());
            productcategoryRepository.save(productcategory);
            redirectAttributes.addFlashAttribute("message", "Successfully saved.");
            return "redirect:/productcategory/index";

        } else {
            if (productcategory.getId() == null) {
                String slug = slagGenerator.generateSlug(productcategory.getName());
                productcategory.setSlug(slug);
            }
            productcategoryRepository.save(productcategory);
            redirectAttributes.addFlashAttribute("message", "File empty");
            return "redirect:/productcategory/index";
        }
//        newsRepository.save(news);
//        return "redirect:/news/index";
    }

    @RequestMapping("/details/{id}")
    public String create(Model model, @PathVariable Long id, Productcategory productcategory) {
        model.addAttribute("productcategory_details", productcategoryRepository.findById(id).orElse(null));
        return "product/productcategory/productcategory_details";

    }

    @RequestMapping("/edit/{id}")
    public String edit(Model model, @PathVariable Long id, Productcategory productcategory) {


        model.addAttribute("productcategory", productcategoryRepository.findById(id).orElse(null));
        model.addAttribute("productcategorylist", productcategoryRepository.findAll(Sort.by(Sort.Direction.DESC, "id")));
        model.addAttribute("statuslist", ProductStatusEnum.values());

        
        model.addAttribute("productcategory", productcategoryRepository.findById(id).orElse(null));
        model.addAttribute("productcategorylist", productcategoryRepository.findAll(Sort.by(Sort.Direction.DESC, "id")));
        model.addAttribute("statuslist",  ProductStatusEnum.values());

        return "product/productcategory/add";
    }

    @RequestMapping("/delete/{id}")
    public String delete(Model model, @PathVariable Long id, Productcategory productcategory, RedirectAttributes redirectAttributes) {
        productcategory = productcategoryRepository.findById(id).orElse(null);
        File file = new File(properties.getRootPath() + File.separator + productcategory.getImageName());
        file.delete();
        productcategoryRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("message", "Deleted successfully.");
        return "redirect:/productcategory/index";
    }

    public List<Productcategory> getAllParents(Productcategory category) {
        List<Productcategory> parents = new ArrayList<>();
        Productcategory currentCategory = category;

        // Traverse up the parent hierarchy
        while (currentCategory != null && currentCategory.getParent() != null) {
            currentCategory = currentCategory.getParent(); // Move to the parent category
            parents.add(currentCategory); // Add the parent to the list
        }

        return parents;
    }

}
