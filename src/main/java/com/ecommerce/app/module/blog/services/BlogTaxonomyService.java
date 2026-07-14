package com.ecommerce.app.module.blog.services;

import com.ecommerce.app.module.blog.mapper.BlogMapper;
import com.ecommerce.app.module.blog.model.BlogCategory;
import com.ecommerce.app.module.blog.model.BlogSeries;
import com.ecommerce.app.module.blog.repository.BlogCategoryRepository;
import com.ecommerce.app.module.blog.repository.BlogSeriesRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BlogTaxonomyService {

    private final BlogCategoryRepository categoryRepository;
    private final BlogSeriesRepository seriesRepository;
    private final BlogMapper mapper;

    public BlogTaxonomyService(
            BlogCategoryRepository categoryRepository,
            BlogSeriesRepository seriesRepository,
            BlogMapper mapper) {
        this.categoryRepository = categoryRepository;
        this.seriesRepository = seriesRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public Page<BlogCategory> categories(Pageable pageable) {
        return categoryRepository.findAll(pageable);
    }

    @Transactional
    public BlogCategory saveCategory(BlogCategory category) {
        category.setSlug(uniqueCategorySlug(category));
        return categoryRepository.save(category);
    }

    @Transactional(readOnly = true)
    public Page<BlogSeries> series(Pageable pageable) {
        return seriesRepository.findAll(pageable);
    }

    @Transactional
    public BlogSeries saveSeries(BlogSeries series) {
        series.setSlug(mapper.slugify(series.getSlug() == null || series.getSlug().isBlank() ? series.getTitle() : series.getSlug()));
        return seriesRepository.save(series);
    }

    private String uniqueCategorySlug(BlogCategory category) {
        String base = mapper.slugify(category.getSlug() == null || category.getSlug().isBlank() ? category.getName() : category.getSlug());
        if (base.isBlank()) {
            base = "blog-category";
        }
        if (base.length() > 165) {
            base = base.substring(0, 165).replaceAll("-+$", "");
        }
        String slug = base;
        int counter = 2;
        while (category.getId() == null
                ? categoryRepository.existsBySlugAndDeletedFlagFalse(slug)
                : categoryRepository.existsBySlugAndIdNotAndDeletedFlagFalse(slug, category.getId())) {
            String suffix = "-" + counter++;
            String trimmedBase = base.length() + suffix.length() > 180 ? base.substring(0, 180 - suffix.length()).replaceAll("-+$", "") : base;
            slug = trimmedBase + suffix;
        }
        return slug;
    }
}
