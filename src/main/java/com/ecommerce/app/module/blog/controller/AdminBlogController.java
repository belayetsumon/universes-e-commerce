package com.ecommerce.app.module.blog.controller;

import com.ecommerce.app.module.blog.dto.BlogCommentForm;
import com.ecommerce.app.module.blog.dto.BlogForm;
import com.ecommerce.app.module.blog.dto.BlogSearchCriteria;
import com.ecommerce.app.module.blog.mapper.BlogMapper;
import com.ecommerce.app.module.blog.model.BlogModerationStatus;
import com.ecommerce.app.module.blog.model.BlogPublicationStatus;
import com.ecommerce.app.module.blog.model.BlogVisibility;
import com.ecommerce.app.module.blog.repository.BlogApprovalRepository;
import com.ecommerce.app.module.blog.repository.BlogCategoryRepository;
import com.ecommerce.app.module.blog.repository.BlogCommentRepository;
import com.ecommerce.app.module.blog.repository.BlogRevisionRepository;
import com.ecommerce.app.module.blog.repository.BlogSeriesRepository;
import com.ecommerce.app.module.blog.repository.BlogSubscriberRepository;
import com.ecommerce.app.module.blog.services.BlogAdminService;
import com.ecommerce.app.module.blog.services.BlogImageUploadException;
import com.ecommerce.app.module.blog.services.BlogPublicService;
import com.ecommerce.app.module.blog.validator.BlogValidator;
import jakarta.validation.Valid;
import java.util.Set;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/blog")
@PreAuthorize("hasAuthority('admin')")
public class AdminBlogController {

    private static final Set<String> ALLOWED_SORTS = Set.of("updatedAt", "publishedAt", "title", "status", "viewCount", "sortOrder");

    private final BlogAdminService blogAdminService;
    private final BlogPublicService blogPublicService;
    private final BlogMapper blogMapper;
    private final BlogValidator blogValidator;
    private final BlogCategoryRepository categoryRepository;
    private final BlogSeriesRepository seriesRepository;
    private final BlogRevisionRepository revisionRepository;
    private final BlogCommentRepository commentRepository;
    private final BlogApprovalRepository approvalRepository;
    private final BlogSubscriberRepository subscriberRepository;

    public AdminBlogController(
            BlogAdminService blogAdminService,
            BlogPublicService blogPublicService,
            BlogMapper blogMapper,
            BlogValidator blogValidator,
            BlogCategoryRepository categoryRepository,
            BlogSeriesRepository seriesRepository,
            BlogRevisionRepository revisionRepository,
            BlogCommentRepository commentRepository,
            BlogApprovalRepository approvalRepository,
            BlogSubscriberRepository subscriberRepository) {
        this.blogAdminService = blogAdminService;
        this.blogPublicService = blogPublicService;
        this.blogMapper = blogMapper;
        this.blogValidator = blogValidator;
        this.categoryRepository = categoryRepository;
        this.seriesRepository = seriesRepository;
        this.revisionRepository = revisionRepository;
        this.commentRepository = commentRepository;
        this.approvalRepository = approvalRepository;
        this.subscriberRepository = subscriberRepository;
    }

    @InitBinder("blogForm")
    public void initBinder(WebDataBinder binder) {
        binder.addValidators(blogValidator);
    }

    @ModelAttribute
    public void dropdowns(Model model) {
        model.addAttribute("blogStatuses", BlogPublicationStatus.values());
        model.addAttribute("blogVisibilities", BlogVisibility.values());
        model.addAttribute("blogCategories", categoryRepository.findByDeletedFlagFalseAndActiveFlagTrueOrderBySortOrderAscNameAsc());
        model.addAttribute("blogSeriesList", seriesRepository.findByDeletedFlagFalseAndActiveFlagTrueOrderBySortOrderAscTitleAsc());
    }

    @GetMapping
    public String index(
            @ModelAttribute BlogSearchCriteria criteria,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 5), 100), sort(criteria));
        model.addAttribute("blogs", blogAdminService.search(criteria, pageable));
        model.addAttribute("criteria", criteria);
        model.addAttribute("metrics", blogAdminService.dashboardMetrics());
        return "admin/blog/index";
    }

    @GetMapping("/new")
    public String create(Model model) {
        BlogForm form = new BlogForm();
        model.addAttribute("blogForm", form);
        return "admin/blog/form";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("blogForm", blogMapper.toForm(blogAdminService.findRequired(id)));
        return "admin/blog/form";
    }

    @PostMapping("/save")
    public String save(
            @Valid @ModelAttribute("blogForm") BlogForm form,
            BindingResult result,
            @RequestParam(value = "featuredImageFile", required = false) MultipartFile featuredImageFile,
            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "admin/blog/form";
        }
        try {
            var saved = blogAdminService.save(form, featuredImageFile);
            redirectAttributes.addFlashAttribute("successMessage", "Blog post saved successfully.");
            return "redirect:/admin/blog/" + saved.getId() + "/edit";
        } catch (OptimisticLockingFailureException ex) {
            result.reject("blog.optimistic.lock", ex.getMessage());
            return "admin/blog/form";
        } catch (BlogImageUploadException ex) {
            result.rejectValue("featuredImageUrl", "blog.featured.image.upload", ex.getMessage());
            return "admin/blog/form";
        }
    }

    @GetMapping("/{id}/preview")
    public String preview(@PathVariable Long id, Model model) {
        var blog = blogAdminService.findRequired(id);
        model.addAttribute("blog", blog);
        model.addAttribute("commentForm", new BlogCommentForm());
        model.addAttribute("previewMode", true);
        blogPublicService.enrichArticleModel(model, blog);
        return "fronttheme/blog/detail";
    }

    @PostMapping("/{id}/status")
    public String status(@PathVariable Long id, @RequestParam BlogPublicationStatus status, RedirectAttributes redirectAttributes) {
        blogAdminService.changeStatus(id, status);
        redirectAttributes.addFlashAttribute("successMessage", "Blog status updated to " + status + ".");
        return "redirect:/admin/blog";
    }

    @PostMapping("/{id}/duplicate")
    public String duplicate(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        var copy = blogAdminService.duplicate(id);
        redirectAttributes.addFlashAttribute("successMessage", "Blog post duplicated as draft.");
        return "redirect:/admin/blog/" + copy.getId() + "/edit";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        blogAdminService.softDelete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Blog post moved to deleted records.");
        return "redirect:/admin/blog";
    }

    @GetMapping("/{id}/revisions")
    public String revisions(@PathVariable Long id, Model model) {
        var blog = blogAdminService.findRequired(id);
        model.addAttribute("blog", blog);
        model.addAttribute("revisions", revisionRepository.findByBlogOrderByRevisionNumberDesc(blog));
        return "admin/blog/revisions";
    }

    @GetMapping("/approval-queue")
    public String approvalQueue(@RequestParam(defaultValue = "0") int page, Model model) {
        model.addAttribute("blogs", blogAdminService.search(queueCriteria(BlogPublicationStatus.IN_REVIEW), PageRequest.of(Math.max(page, 0), 50, Sort.by(Sort.Direction.DESC, "updatedAt"))));
        return "admin/blog/approval-queue";
    }

    @PostMapping("/{id}/approve")
    public String approve(@PathVariable Long id, @RequestParam(required = false) String remarks, RedirectAttributes redirectAttributes) {
        blogAdminService.approve(id, remarks);
        redirectAttributes.addFlashAttribute("successMessage", "Blog post approved.");
        return "redirect:/admin/blog/approval-queue";
    }

    @PostMapping("/{id}/reject")
    public String reject(@PathVariable Long id, @RequestParam(required = false) String remarks, RedirectAttributes redirectAttributes) {
        blogAdminService.reject(id, remarks);
        redirectAttributes.addFlashAttribute("successMessage", "Blog post returned for changes.");
        return "redirect:/admin/blog/approval-queue";
    }

    @GetMapping("/publishing-queue")
    public String publishingQueue(@RequestParam(defaultValue = "0") int page, Model model) {
        model.addAttribute("blogs", blogAdminService.search(queueCriteria(BlogPublicationStatus.SCHEDULED), PageRequest.of(Math.max(page, 0), 50, Sort.by(Sort.Direction.ASC, "scheduledAt"))));
        return "admin/blog/publishing-queue";
    }

    @PostMapping("/publishing-queue/run")
    public String runPublishingQueue(RedirectAttributes redirectAttributes) {
        int published = blogAdminService.publishScheduledPosts();
        redirectAttributes.addFlashAttribute("successMessage", published + " scheduled post(s) published.");
        return "redirect:/admin/blog/publishing-queue";
    }

    @GetMapping("/comments")
    public String comments(@RequestParam(required = false) BlogModerationStatus status, @RequestParam(defaultValue = "0") int page, Model model) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), 50, Sort.by(Sort.Direction.DESC, "createdAt"));
        model.addAttribute("comments", status == null
                ? commentRepository.findByDeletedFlagFalseOrderByCreatedAtDesc(pageable)
                : commentRepository.findByModerationStatusAndDeletedFlagFalseOrderByCreatedAtDesc(status, pageable));
        model.addAttribute("selectedStatus", status);
        model.addAttribute("moderationStatuses", BlogModerationStatus.values());
        return "admin/blog/comments";
    }

    @PostMapping("/comments/{id}/moderate")
    public String moderateComment(@PathVariable Long id, @RequestParam BlogModerationStatus status, RedirectAttributes redirectAttributes) {
        blogAdminService.moderateComment(id, status);
        redirectAttributes.addFlashAttribute("successMessage", "Comment moderation updated.");
        return "redirect:/admin/blog/comments";
    }

    @GetMapping("/subscribers")
    public String subscribers(@RequestParam(defaultValue = "0") int page, Model model) {
        model.addAttribute("subscribers", subscriberRepository.findByDeletedFlagFalseOrderByCreatedAtDesc(PageRequest.of(Math.max(page, 0), 50)));
        return "admin/blog/subscribers";
    }

    @GetMapping("/import")
    public String importPage() {
        return "admin/blog/import";
    }

    @GetMapping("/export")
    public String exportPage(Model model) {
        model.addAttribute("message", "Streaming CSV/Excel export is reserved for the next import/export phase.");
        return "admin/blog/export";
    }

    private BlogSearchCriteria queueCriteria(BlogPublicationStatus status) {
        BlogSearchCriteria criteria = new BlogSearchCriteria();
        criteria.setStatus(status);
        return criteria;
    }

    private Sort sort(BlogSearchCriteria criteria) {
        String sortField = ALLOWED_SORTS.contains(criteria.getSort()) ? criteria.getSort() : "updatedAt";
        Sort.Direction direction = "asc".equalsIgnoreCase(criteria.getDirection()) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(direction, sortField).and(Sort.by(Sort.Direction.DESC, "id"));
    }
}
