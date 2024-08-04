/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecommerce.app.controller;

import com.ecommerce.app.model.BlogCategory;
import com.ecommerce.app.model.enumvalue.Status;
import com.ecommerce.app.ripository.BlogCategoryRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 *
 * @author User
 */
@Controller
@RequestMapping("/blogcategory")
public class BlogCategoryController {

    @Autowired
    BlogCategoryRepository blogCategoryRepository;

    @RequestMapping("/index")
    public String index(Model model) {
        model.addAttribute("blogcategorylist", blogCategoryRepository.findAll(Sort.by(Sort.Direction.DESC, "id")));
        return "blog/category/index";
    }

    @RequestMapping("/create")
    public String create(Model model, BlogCategory blogCategory) {
        model.addAttribute("statuslist", Status.values());
        return "blog/category/add";
    }

    @RequestMapping("/save")
    public String create(Model model, @Valid BlogCategory blogCategory, BindingResult bindingResult, RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return "blog/category/add";
        }
        blogCategoryRepository.save(blogCategory);
        redirectAttributes.addFlashAttribute("message", "Successfully saved.");
        return "redirect:/blogcategory/index";
    }

    @RequestMapping("/details/{id}")
    public String create(Model model, @PathVariable Long id, BlogCategory blogCategory) {
        model.addAttribute("blogcategory_details", blogCategoryRepository.findById(id));
        return "blog/category/blogcategory_details";

    }

    @RequestMapping("/edit/{id}")
    public String edit(Model model, @PathVariable Long id, BlogCategory blogCategory) {
        model.addAttribute("blogCategory", blogCategoryRepository.findById(id));
        model.addAttribute("statuslist", Status.values());
        return "blog/category/add";
    }

    @RequestMapping("/delete/{id}")
    public String delete(Model model, @PathVariable Long id, BlogCategory blogCategory, RedirectAttributes redirectAttributes) {
        blogCategoryRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("message", "Deleted successfully.");
        return "redirect:/blogcategory/index";
    }

}
