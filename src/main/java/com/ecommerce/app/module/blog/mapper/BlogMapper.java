package com.ecommerce.app.module.blog.mapper;

import com.ecommerce.app.module.blog.dto.BlogForm;
import com.ecommerce.app.module.blog.model.Blog;
import com.ecommerce.app.module.blog.model.BlogSeo;
import com.ecommerce.app.module.blog.model.BlogTag;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class BlogMapper {

    public BlogForm toForm(Blog blog) {
        BlogForm form = new BlogForm();
        form.setId(blog.getId());
        form.setVersion(blog.getVersion());
        form.setTitle(blog.getTitle());
        form.setSlug(blog.getSlug());
        form.setExcerpt(blog.getExcerpt());
        form.setContentHtml(blog.getContentHtml());
        form.setStatus(blog.getStatus());
        form.setVisibility(blog.getVisibility());
        form.setCategoryId(blog.getCategory() != null ? blog.getCategory().getId() : null);
        form.setSeriesId(blog.getSeries() != null ? blog.getSeries().getId() : null);
        form.setTags(blog.getTags() == null ? "" : blog.getTags().stream().map(BlogTag::getName).sorted().collect(Collectors.joining(", ")));
        form.setFeaturedImageUrl(blog.getFeaturedImageUrl());
        form.setFeaturedImageAlt(blog.getFeaturedImageAlt());
        form.setLanguageCode(blog.getLanguageCode());
        form.setCountryCodes(blog.getCountryCodes());
        form.setDeviceRules(blog.getDeviceRules());
        form.setCustomerSegment(blog.getCustomerSegment());
        form.setStickyPost(blog.isStickyPost());
        form.setFeaturedPost(blog.isFeaturedPost());
        form.setAllowComments(blog.isAllowComments());
        form.setScheduledAt(blog.getScheduledAt());
        form.setExpiresAt(blog.getExpiresAt());
        form.setTemplateKey(blog.getTemplateKey());
        form.setUtmCampaign(blog.getUtmCampaign());
        if (blog.getSeo() != null) {
            BlogSeo seo = blog.getSeo();
            form.setSeoTitle(seo.getSeoTitle());
            form.setMetaDescription(seo.getMetaDescription());
            form.setMetaKeywords(seo.getMetaKeywords());
            form.setCanonicalUrl(seo.getCanonicalUrl());
            form.setRobotsMeta(seo.getRobotsMeta());
            form.setOpenGraphTitle(seo.getOpenGraphTitle());
            form.setOpenGraphDescription(seo.getOpenGraphDescription());
            form.setOpenGraphImage(seo.getOpenGraphImage());
            form.setTwitterCard(seo.getTwitterCard());
            form.setJsonLd(seo.getJsonLd());
        }
        return form;
    }

    public void copyFormToBlog(BlogForm form, Blog blog) {
        blog.setTitle(trim(form.getTitle()));
        blog.setSlug(slugify(form.getSlug()));
        blog.setExcerpt(trim(form.getExcerpt()));
        blog.setContentHtml(form.getContentHtml());
        blog.setContentPlainText(toPlainText(form.getContentHtml()));
        blog.setStatus(form.getStatus());
        blog.setVisibility(form.getVisibility());
        blog.setFeaturedImageUrl(trim(form.getFeaturedImageUrl()));
        blog.setFeaturedImageAlt(trim(form.getFeaturedImageAlt()));
        blog.setLanguageCode(defaultText(form.getLanguageCode(), "en").toLowerCase());
        blog.setCountryCodes(trim(form.getCountryCodes()));
        blog.setDeviceRules(trim(form.getDeviceRules()));
        blog.setCustomerSegment(trim(form.getCustomerSegment()));
        blog.setStickyPost(form.isStickyPost());
        blog.setFeaturedPost(form.isFeaturedPost());
        blog.setAllowComments(form.isAllowComments());
        blog.setScheduledAt(form.getScheduledAt());
        blog.setExpiresAt(form.getExpiresAt());
        blog.setTemplateKey(trim(form.getTemplateKey()));
        blog.setUtmCampaign(trim(form.getUtmCampaign()));
        blog.setReadingTimeMinutes(estimateReadingTime(blog.getContentPlainText()));
    }

    public void copyFormToSeo(BlogForm form, BlogSeo seo) {
        seo.setSeoTitle(trim(form.getSeoTitle()));
        seo.setMetaDescription(trim(form.getMetaDescription()));
        seo.setMetaKeywords(trim(form.getMetaKeywords()));
        seo.setCanonicalUrl(trim(form.getCanonicalUrl()));
        seo.setRobotsMeta(defaultText(form.getRobotsMeta(), "index,follow"));
        seo.setOpenGraphTitle(trim(form.getOpenGraphTitle()));
        seo.setOpenGraphDescription(trim(form.getOpenGraphDescription()));
        seo.setOpenGraphImage(trim(form.getOpenGraphImage()));
        seo.setTwitterCard(defaultText(form.getTwitterCard(), "summary_large_image"));
        seo.setJsonLd(trim(form.getJsonLd()));
    }

    public String slugify(String value) {
        String text = defaultText(value, "");
        return text.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .trim()
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-");
    }

    public String toPlainText(String html) {
        return defaultText(html, "")
                .replaceAll("(?is)<script.*?</script>", " ")
                .replaceAll("(?is)<style.*?</style>", " ")
                .replaceAll("<[^>]+>", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    public int estimateReadingTime(String plainText) {
        int words = defaultText(plainText, "").isBlank() ? 0 : defaultText(plainText, "").trim().split("\\s+").length;
        return Math.max(1, (int) Math.ceil(words / 220.0));
    }

    public String trim(String value) {
        return value == null ? null : value.trim();
    }

    public String defaultText(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }
}
