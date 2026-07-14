package com.ecommerce.app.module.blog.controller;

import com.ecommerce.app.module.blog.dto.BlogCommentForm;
import com.ecommerce.app.module.blog.dto.BlogSubscriberForm;
import com.ecommerce.app.module.blog.repository.BlogCategoryRepository;
import com.ecommerce.app.module.blog.repository.BlogTagRepository;
import com.ecommerce.app.module.blog.services.BlogEngagementService;
import com.ecommerce.app.module.blog.services.BlogPublicService;
import com.ecommerce.app.publics.seo.PublicSeoService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
@RequestMapping("/public/blog")
public class PublicBlogController {

    private final BlogPublicService blogPublicService;
    private final BlogEngagementService engagementService;
    private final BlogCategoryRepository categoryRepository;
    private final BlogTagRepository tagRepository;
    private final PublicSeoService publicSeoService;

    public PublicBlogController(
            BlogPublicService blogPublicService,
            BlogEngagementService engagementService,
            BlogCategoryRepository categoryRepository,
            BlogTagRepository tagRepository,
            PublicSeoService publicSeoService) {
        this.blogPublicService = blogPublicService;
        this.engagementService = engagementService;
        this.categoryRepository = categoryRepository;
        this.tagRepository = tagRepository;
        this.publicSeoService = publicSeoService;
    }

    @ModelAttribute
    public void common(Model model) {
        model.addAttribute("subscriberForm", new BlogSubscriberForm());
    }

    @GetMapping
    public String landing(@RequestParam(required = false) String q, @RequestParam(defaultValue = "0") int page, HttpServletRequest request, Model model) {
        var articles = blogPublicService.publicPosts(q, null, PageRequest.of(Math.max(page, 0), 12, Sort.by(Sort.Direction.DESC, "publishedAt")));
        model.addAttribute("articles", articles);
        model.addAttribute("query", q);
        blogPublicService.enrichListingModel(model);
        publicSeoService.apply(model, publicSeoService.blogModuleList(request, articles.getContent()));
        return "fronttheme/blog/list";
    }

    @GetMapping("/search")
    public String search(@RequestParam(required = false) String q, @RequestParam(defaultValue = "0") int page, HttpServletRequest request, Model model) {
        return landing(q, page, request, model);
    }

    @GetMapping("/category/{slug}")
    public String category(@PathVariable String slug, @RequestParam(defaultValue = "0") int page, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {
        var category = categoryRepository.findBySlugAndDeletedFlagFalse(slug).orElse(null);
        if (category == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Blog category was not found.");
            return "redirect:/public/blog";
        }
        var articles = blogPublicService.publicPosts(null, slug, PageRequest.of(Math.max(page, 0), 12, Sort.by(Sort.Direction.DESC, "publishedAt")));
        model.addAttribute("articles", articles);
        model.addAttribute("activeCategorySlug", slug);
        blogPublicService.enrichListingModel(model);
        publicSeoService.apply(model, publicSeoService.blogCategory(request, category, articles.getContent()));
        return "fronttheme/blog/list";
    }

    @GetMapping("/tag/{slug}")
    public String tag(@PathVariable String slug, @RequestParam(defaultValue = "0") int page, Model model, RedirectAttributes redirectAttributes) {
        if (tagRepository.findBySlugAndDeletedFlagFalse(slug).isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Blog tag was not found.");
            return "redirect:/public/blog";
        }
        model.addAttribute("articles", blogPublicService.publicPosts(slug.replace("-", " "), null, PageRequest.of(Math.max(page, 0), 12, Sort.by(Sort.Direction.DESC, "publishedAt"))));
        model.addAttribute("activeTagSlug", slug);
        blogPublicService.enrichListingModel(model);
        return "fronttheme/blog/list";
    }

    @GetMapping("/{slug}")
    public String detail(@PathVariable String slug, @RequestParam(defaultValue = "en") String lang, HttpServletRequest request, Model model) {
        var blog = blogPublicService.findPublishedBySlug(slug, lang, request).orElse(null);
        if (blog == null) {
            return "redirect:/public/blog";
        }
        model.addAttribute("blog", blog);
        model.addAttribute("commentForm", new BlogCommentForm());
        blogPublicService.enrichArticleModel(model, blog);
        publicSeoService.apply(model, publicSeoService.blogArticle(request, blog));
        return "fronttheme/blog/detail";
    }

    @PostMapping("/{slug}/comments")
    public String comment(
            @PathVariable String slug,
            @Valid @ModelAttribute("commentForm") BlogCommentForm form,
            BindingResult result,
            HttpServletRequest request,
            Model model,
            RedirectAttributes redirectAttributes) {
        var blog = blogPublicService.findPublishedBySlug(slug, "en", request).orElse(null);
        if (blog == null) {
            return "redirect:/public/blog";
        }
        if (result.hasErrors()) {
            model.addAttribute("blog", blog);
            blogPublicService.enrichArticleModel(model, blog);
            return "fronttheme/blog/detail";
        }
        engagementService.submitComment(blog, form, request);
        redirectAttributes.addFlashAttribute("successMessage", "Comment submitted for moderation.");
        return "redirect:/public/blog/" + slug;
    }

    @PostMapping("/subscribe")
    public String subscribe(@Valid @ModelAttribute("subscriberForm") BlogSubscriberForm form, BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Enter a valid email address to subscribe.");
            return "redirect:/public/blog";
        }
        engagementService.subscribe(form);
        redirectAttributes.addFlashAttribute("successMessage", "Subscription saved.");
        return "redirect:/public/blog";
    }
}
