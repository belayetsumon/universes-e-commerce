package com.ecommerce.app.module.blog.controller;

import com.ecommerce.app.module.blog.dto.BlogCommentForm;
import com.ecommerce.app.module.blog.model.Blog;
import com.ecommerce.app.module.blog.repository.BlogAuthorRepository;
import com.ecommerce.app.module.blog.repository.BlogCategoryRepository;
import com.ecommerce.app.module.blog.services.BlogEngagementService;
import com.ecommerce.app.module.blog.services.BlogPublicService;
import com.ecommerce.app.module.blog.services.CustomerBlogService;
import jakarta.servlet.http.HttpServletRequest;
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
@RequestMapping("/customer/blog")
@PreAuthorize("hasAuthority('customer')")
public class CustomerBlogController {

    private final CustomerBlogService customerBlogService;
    private final BlogPublicService blogPublicService;
    private final BlogEngagementService engagementService;
    private final BlogCategoryRepository categoryRepository;
    private final BlogAuthorRepository authorRepository;

    public CustomerBlogController(
            CustomerBlogService customerBlogService,
            BlogPublicService blogPublicService,
            BlogEngagementService engagementService,
            BlogCategoryRepository categoryRepository,
            BlogAuthorRepository authorRepository) {
        this.customerBlogService = customerBlogService;
        this.blogPublicService = blogPublicService;
        this.engagementService = engagementService;
        this.categoryRepository = categoryRepository;
        this.authorRepository = authorRepository;
    }

    @ModelAttribute
    public void dropdowns(Model model) {
        model.addAttribute("blogCategories", categoryRepository.findByDeletedFlagFalseAndActiveFlagTrueOrderBySortOrderAscNameAsc());
        model.addAttribute("blogAuthors", authorRepository.findByDeletedFlagFalseAndActiveFlagTrueOrderByDisplayNameAsc());
    }

    @GetMapping(value = {"", "/", "/index"})
    public String index(@RequestParam(required = false) String q, @RequestParam(defaultValue = "0") int page, Model model) {
        var articles = customerBlogService.customerFeed(q, PageRequest.of(Math.max(page, 0), 12, Sort.by(Sort.Direction.DESC, "publishedAt")));
        model.addAttribute("articles", articles);
        model.addAttribute("bookmarkedArticles", customerBlogService.bookmarkMap(articles));
        model.addAttribute("recentlyViewed", customerBlogService.recentlyViewed());
        model.addAttribute("query", q);
        return "customer/blog/index";
    }

    @GetMapping("/saved")
    public String saved(Model model) {
        model.addAttribute("savedArticles", customerBlogService.savedArticles());
        model.addAttribute("recentlyViewed", customerBlogService.recentlyViewed());
        return "customer/blog/saved";
    }

    @GetMapping("/{slug}")
    public String detail(@PathVariable String slug, @RequestParam(defaultValue = "en") String lang, HttpServletRequest request, Model model) {
        Blog blog = customerBlogService.findPublished(slug, lang).orElse(null);
        if (blog == null) {
            return "redirect:/customer/blog";
        }
        customerBlogService.recordCustomerView(blog, request);
        model.addAttribute("blog", blog);
        model.addAttribute("commentForm", new BlogCommentForm());
        model.addAttribute("bookmarked", customerBlogService.isBookmarked(blog));
        blogPublicService.enrichArticleModel(model, blog);
        return "customer/blog/detail";
    }

    @PostMapping("/{slug}/bookmark")
    public String bookmark(@PathVariable String slug, RedirectAttributes redirectAttributes) {
        Blog blog = customerBlogService.findPublished(slug, "en").orElse(null);
        if (blog == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Article was not found.");
            return "redirect:/customer/blog";
        }
        customerBlogService.bookmark(blog);
        redirectAttributes.addFlashAttribute("successMessage", "Article saved to your reading list.");
        return "redirect:/customer/blog/" + slug;
    }

    @PostMapping("/{slug}/bookmark/remove")
    public String removeBookmark(@PathVariable String slug, RedirectAttributes redirectAttributes) {
        Blog blog = customerBlogService.findPublished(slug, "en").orElse(null);
        if (blog == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Article was not found.");
            return "redirect:/customer/blog";
        }
        customerBlogService.removeBookmark(blog);
        redirectAttributes.addFlashAttribute("successMessage", "Article removed from your reading list.");
        return "redirect:/customer/blog/" + slug;
    }

    @PostMapping("/{slug}/comments")
    public String comment(
            @PathVariable String slug,
            @Valid @ModelAttribute("commentForm") BlogCommentForm form,
            BindingResult result,
            HttpServletRequest request,
            Model model,
            RedirectAttributes redirectAttributes) {
        Blog blog = customerBlogService.findPublished(slug, "en").orElse(null);
        if (blog == null) {
            return "redirect:/customer/blog";
        }
        if (result.hasErrors()) {
            model.addAttribute("blog", blog);
            model.addAttribute("bookmarked", customerBlogService.isBookmarked(blog));
            blogPublicService.enrichArticleModel(model, blog);
            return "customer/blog/detail";
        }
        engagementService.submitComment(blog, form, request);
        redirectAttributes.addFlashAttribute("successMessage", "Comment submitted for moderation.");
        return "redirect:/customer/blog/" + slug;
    }
}
