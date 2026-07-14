package com.ecommerce.app.module.blog.validator;

import com.ecommerce.app.module.blog.dto.BlogForm;
import com.ecommerce.app.module.blog.model.BlogPublicationStatus;
import com.ecommerce.app.module.blog.repository.BlogRepository;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class BlogValidator implements Validator {

    private final BlogRepository blogRepository;

    public BlogValidator(BlogRepository blogRepository) {
        this.blogRepository = blogRepository;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return BlogForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        BlogForm form = (BlogForm) target;
        String languageCode = form.getLanguageCode() == null || form.getLanguageCode().isBlank() ? "en" : form.getLanguageCode().trim();
        if (form.getSlug() != null && !form.getSlug().isBlank()) {
            boolean duplicate = form.getId() == null
                    ? blogRepository.existsBySlugIgnoreCaseAndLanguageCodeIgnoreCaseAndDeletedFlagFalse(form.getSlug().trim(), languageCode)
                    : blogRepository.existsBySlugIgnoreCaseAndLanguageCodeIgnoreCaseAndIdNotAndDeletedFlagFalse(form.getSlug().trim(), languageCode, form.getId());
            if (duplicate) {
                errors.rejectValue("slug", "blog.slug.duplicate", "Another blog already uses this slug for the selected language.");
            }
        }
        if (form.getStatus() == BlogPublicationStatus.SCHEDULED && form.getScheduledAt() == null) {
            errors.rejectValue("scheduledAt", "blog.schedule.required", "Scheduled publishing needs a date and time.");
        }
        if (form.getScheduledAt() != null && form.getScheduledAt().isBefore(LocalDateTime.now().minusMinutes(1))
                && form.getStatus() == BlogPublicationStatus.SCHEDULED) {
            errors.rejectValue("scheduledAt", "blog.schedule.past", "Scheduled publishing time cannot be in the past.");
        }
        if (form.getExpiresAt() != null && form.getScheduledAt() != null && form.getExpiresAt().isBefore(form.getScheduledAt())) {
            errors.rejectValue("expiresAt", "blog.expiry.before.schedule", "Expiration date must be after scheduled publishing date.");
        }
        rejectLength(errors, "seoTitle", form.getSeoTitle(), 180, "SEO title cannot exceed 180 characters.");
        rejectLength(errors, "metaDescription", form.getMetaDescription(), 320, "Meta description cannot exceed 320 characters.");
        rejectLength(errors, "canonicalUrl", form.getCanonicalUrl(), 500, "Canonical URL cannot exceed 500 characters.");
    }

    private void rejectLength(Errors errors, String field, String value, int limit, String message) {
        if (value != null && value.length() > limit) {
            errors.rejectValue(field, "blog.length", message);
        }
    }
}
