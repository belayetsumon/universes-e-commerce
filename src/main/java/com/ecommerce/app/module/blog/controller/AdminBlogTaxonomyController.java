package com.ecommerce.app.module.blog.controller;

import com.ecommerce.app.module.blog.model.BlogCategory;
import com.ecommerce.app.module.blog.model.BlogSeries;
import com.ecommerce.app.module.blog.repository.BlogCategoryRepository;
import com.ecommerce.app.module.blog.repository.BlogSeriesRepository;
import com.ecommerce.app.module.blog.services.BlogTaxonomyService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/blog")
@PreAuthorize("hasAuthority('admin')")
public class AdminBlogTaxonomyController {

    private final BlogTaxonomyService taxonomyService;
    private final BlogCategoryRepository categoryRepository;
    private final BlogSeriesRepository seriesRepository;

    public AdminBlogTaxonomyController(
            BlogTaxonomyService taxonomyService,
            BlogCategoryRepository categoryRepository,
            BlogSeriesRepository seriesRepository) {
        this.taxonomyService = taxonomyService;
        this.categoryRepository = categoryRepository;
        this.seriesRepository = seriesRepository;
    }

    @GetMapping("/categories")
    public String categories(@RequestParam(defaultValue = "0") int page, Model model) {
        model.addAttribute("categories", taxonomyService.categories(PageRequest.of(Math.max(page, 0), 50, Sort.by("sortOrder").ascending().and(Sort.by("name")))));
        return "admin/blog/categories";
    }

    @GetMapping("/categories/new")
    public String categoryCreate(Model model) {
        model.addAttribute("category", new BlogCategory());
        return "admin/blog/category-form";
    }

    @GetMapping("/categories/{id}/edit")
    public String categoryEdit(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        BlogCategory category = categoryRepository.findById(id).orElse(null);
        if (category == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Category was not found.");
            return "redirect:/admin/blog/categories";
        }
        model.addAttribute("category", category);
        return "admin/blog/category-form";
    }

    @PostMapping("/categories/save")
    public String categorySave(@Valid @ModelAttribute("category") BlogCategory category, BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "admin/blog/category-form";
        }
        taxonomyService.saveCategory(category);
        redirectAttributes.addFlashAttribute("successMessage", "Blog category saved.");
        return "redirect:/admin/blog/categories";
    }

    @GetMapping("/series")
    public String series(@RequestParam(defaultValue = "0") int page, Model model) {
        model.addAttribute("seriesPage", taxonomyService.series(PageRequest.of(Math.max(page, 0), 50, Sort.by("sortOrder").ascending().and(Sort.by("title")))));
        return "admin/blog/series";
    }

    @GetMapping("/series/new")
    public String seriesCreate(Model model) {
        model.addAttribute("series", new BlogSeries());
        return "admin/blog/series-form";
    }

    @GetMapping("/series/{id}/edit")
    public String seriesEdit(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        BlogSeries series = seriesRepository.findById(id).orElse(null);
        if (series == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Series was not found.");
            return "redirect:/admin/blog/series";
        }
        model.addAttribute("series", series);
        return "admin/blog/series-form";
    }

    @PostMapping("/series/save")
    public String seriesSave(@Valid @ModelAttribute("series") BlogSeries series, BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "admin/blog/series-form";
        }
        taxonomyService.saveSeries(series);
        redirectAttributes.addFlashAttribute("successMessage", "Blog series saved.");
        return "redirect:/admin/blog/series";
    }
}
