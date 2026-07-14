package com.ecommerce.app.module.blog.services;

import com.ecommerce.app.module.blog.mapper.BlogMapper;
import com.ecommerce.app.module.blog.model.BlogAuthor;
import com.ecommerce.app.module.blog.model.BlogCategory;
import com.ecommerce.app.module.blog.model.BlogSeries;
import com.ecommerce.app.module.blog.repository.BlogAuthorRepository;
import com.ecommerce.app.module.blog.repository.BlogCategoryRepository;
import com.ecommerce.app.module.blog.repository.BlogSeriesRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BlogTaxonomyService {

    private final BlogCategoryRepository categoryRepository;
    private final BlogAuthorRepository authorRepository;
    private final BlogSeriesRepository seriesRepository;
    private final BlogMapper mapper;

    public BlogTaxonomyService(
            BlogCategoryRepository categoryRepository,
            BlogAuthorRepository authorRepository,
            BlogSeriesRepository seriesRepository,
            BlogMapper mapper) {
        this.categoryRepository = categoryRepository;
        this.authorRepository = authorRepository;
        this.seriesRepository = seriesRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public Page<BlogCategory> categories(Pageable pageable) {
        return categoryRepository.findAll(pageable);
    }

    @Transactional
    public BlogCategory saveCategory(BlogCategory category) {
        category.setSlug(mapper.slugify(category.getSlug() == null || category.getSlug().isBlank() ? category.getName() : category.getSlug()));
        return categoryRepository.save(category);
    }

    @Transactional(readOnly = true)
    public Page<BlogAuthor> authors(Pageable pageable) {
        return authorRepository.findAll(pageable);
    }

    @Transactional
    public BlogAuthor saveAuthor(BlogAuthor author) {
        author.setSlug(mapper.slugify(author.getSlug() == null || author.getSlug().isBlank() ? author.getDisplayName() : author.getSlug()));
        return authorRepository.save(author);
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
}
