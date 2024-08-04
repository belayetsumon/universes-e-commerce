/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecommerce.app.customer.controller;

import com.ecommerce.app.model.Blog;
import com.ecommerce.app.model.enumvalue.Status;
import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.module.user.services.LoggedUserService;
import com.ecommerce.app.ripository.BlogCategoryRepository;
import com.ecommerce.app.ripository.BlogRepository;
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
@RequestMapping("/customerblog")
public class CustomerBlogController {

    @Autowired
    BlogCategoryRepository blogCategoryRepository;
    @Autowired
    BlogRepository blogRepository;

    @Autowired
    StorageProperties properties;

    @Autowired
    LoggedUserService loggedUserService;

    @RequestMapping("/index")
    public String index(Model model) {

        Users userss = new Users();
        userss.setId(loggedUserService.activeUserid());

        model.addAttribute("bloglist", blogRepository.findByUserIdOrderByIdDesc(userss));
        return "student/blog/index";
    }

    @RequestMapping("/create")
    public String create(Model model, Blog blog) {

        Users userss = new Users();
        userss.setId(loggedUserService.activeUserid());
        blog.setUserId(userss);
        model.addAttribute("blogcategorylist", blogCategoryRepository.findAll());
        model.addAttribute("statuslist", Status.Panding);
        return "student/blog/add";
    }

    @RequestMapping("/save")
    public String create(Model model, @Valid Blog blog, BindingResult bindingResult, RedirectAttributes redirectAttributes,
            @RequestParam("pic") MultipartFile pic
    ) {
        if (bindingResult.hasErrors()) {
            Users userss = new Users();
            userss.setId(loggedUserService.activeUserid());
            blog.setUserId(userss);
            model.addAttribute("blogcategory", blogCategoryRepository.findAll());
            model.addAttribute("statuslist", Status.Panding);
            return "student/blog/add";
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

                blog.setImageName(filename);
                blog.setStatus(Status.Panding);
                blogRepository.save(blog);

                redirectAttributes.addFlashAttribute("message", "Successfully saved.");
                return "redirect:/customerblog/index";
            } catch (Exception e) {

                Users userss = new Users();
                userss.setId(loggedUserService.activeUserid());
                blog.setUserId(userss);

                model.addAttribute("blogcategorylist", blogCategoryRepository.findAll());
                model.addAttribute("statuslist", Status.Panding);

                redirectAttributes.addFlashAttribute("message", pic.getOriginalFilename() + " => " + e.getMessage());
                return "redirect:/customerblog/index";
            }
        } else if (pic.isEmpty() && blog.getId() != null) {

            Blog blogs = blogRepository.getReferenceById(blog.getId());

            blog.setImageName(blogs.getImageName());
            blog.setStatus(Status.Panding);
            blogRepository.save(blog);

            redirectAttributes.addFlashAttribute("message", "Successfully saved.");

            return "redirect:/customerblog/index";

        } else {
            blog.setStatus(Status.Panding);
            blogRepository.save(blog);
            redirectAttributes.addFlashAttribute("message", "Successfully saved but File empty");
            return "redirect:/customerblog/index";
        }

    }

    @RequestMapping("/details/{id}")
    public String details(Model model, @PathVariable Long id, Blog blog) {
        blog = blogRepository.getReferenceById(id);
        model.addAttribute("blog_details", blog);
        blog.setViewcount(blog.getViewcount() + 1);
        blogRepository.save(blog);
        return "student/blog/blog_details";
    }

    @RequestMapping("/edit/{id}")
    public String edit(Model model, @PathVariable Long id, Blog blog) {
        model.addAttribute("blog", blogRepository.getReferenceById(id));

        model.addAttribute("blogcategorylist", blogCategoryRepository.findAll());
        model.addAttribute("statuslist", Status.Panding);
        return "student/blog/add";
    }

    @RequestMapping("/delete/{id}")
    public String delete(Model model, @PathVariable Long id, Blog blog, RedirectAttributes redirectAttributes) {
        blogRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("message", "Deleted successfully.");
        return "redirect:/customerblog/index";
    }
}
