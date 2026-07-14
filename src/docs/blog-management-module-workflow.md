# Blog Management Module Workflow

## Goal

Build an enterprise-grade blog module for the marketplace with clean Spring MVC layers, SEO-ready public pages, marketing and product integration hooks, editorial workflow, audit fields, optimistic locking, and scalable database structure.

## Architecture

- Package: `com.ecommerce.app.module.blog`
- Layers: model, repository, dto, mapper, validator, services, controller
- Admin routes: `/admin/blog/**`
- Public routes: `/public/blog/**`
- Templates:
  - `admin/blog/**`
  - `fronttheme/blog/**`
- Database helper: `main/resources/db/blog_module_init.sql`

## Done In This Implementation

- [x] Workflow/status tracker created before implementation.
- [x] Normalized entity model with UUID, optimistic locking, audit fields, active flag, record status, soft-delete fields, indexes, and unique constraints.
- [x] Core content entities: Blog, BlogCategory, BlogTag, BlogAuthor, BlogSeries, BlogSeo, BlogRevision, BlogMedia, BlogTranslation.
- [x] Engagement entities: BlogComment, BlogCommentReaction, BlogReaction, BlogBookmark, BlogView, BlogShare, BlogSubscriber, BlogFaq, BlogPoll.
- [x] Product ecosystem entities: BlogRelatedProduct, BlogRelatedCategory, BlogRelatedBrand, BlogRelatedVendor.
- [x] Workflow and reporting entities: BlogApproval, BlogModeration, BlogNotification, BlogAnalytics.
- [x] Repository layer for all blog entities.
- [x] DTO/form/search models for admin and public flows.
- [x] Mapper layer to keep controllers and entities clean.
- [x] Validator layer for slug uniqueness, schedule rules, SEO limits, and safe publishing state.
- [x] Service layer with transactional writes, revision snapshots, duplicate post, publish/unpublish/archive, approval actions, soft delete, and public search.
- [x] Admin panel for post CRUD, advanced search/filter/sort, pagination, status actions, duplicate, preview, approval queue, publishing queue, comments, subscribers, import/export placeholders, and dashboard metrics.
- [x] Public landing, category, tag, author, article detail, search, comments, newsletter subscription, social share links, reading progress bar, related articles, and related products.
- [x] SEO-ready URLs, metadata, canonical URL field, robots meta, Open Graph/Twitter fields, JSON-LD placeholder fields, and sitemap-ready repository queries.
- [x] Admin sidebar integration.
- [x] Database initialization/migration helper SQL.

## Remaining / Next Phases

- [ ] Rich text editor file browser and secure media upload pipeline.
- [ ] WebP/background image optimization worker and CDN/Image CDN integration.
- [ ] Redis cache, edge cache, and full-page cache integration.
- [ ] Broken link crawler and redirect manager UI.
- [ ] Revision comparison visual diff UI.
- [ ] CSV/Excel import parser and export streaming implementation.
- [ ] CAPTCHA, spam ML/provider integration, and per-IP rate limiter.
- [ ] Browser/mobile push provider integration.
- [ ] AI recommendation service and personalized feed.
- [ ] Advanced analytics event ingestion for scroll depth, bounce rate, CTR, and revenue attribution.
- [ ] XML sitemap controller entries for blog routes.
- [ ] Multi-language translation workflow screens and RTL preview.
- [ ] GDPR privacy export/delete flows for blog engagement data.

## Security Notes

- Admin blog controllers are protected with `hasAuthority('admin')`.
- Public blog routes stay under `/public/blog/**` because this project currently whitelists `/public/**`.
- Templates include CSRF hidden fields when `_csrf` exists, but the current project `SecurityConfig` disables CSRF globally. Re-enabling CSRF is a project-level security task and should be handled carefully across all forms.
- HTML body content is sanitized in the service path before persistence. This is a baseline sanitizer without a dedicated HTML policy library; add a policy-based sanitizer such as OWASP Java HTML Sanitizer in a later hardening phase.

## Test Plan

1. Build: `mvn -DskipTests compile`
2. Admin smoke:
   - `/admin/blog`
   - `/admin/blog/new`
   - `/admin/blog/{id}/edit`
   - `/admin/blog/{id}/preview`
   - `/admin/blog/approval-queue`
   - `/admin/blog/comments`
3. Public smoke:
   - `/public/blog`
   - `/public/blog/search?q=...`
   - `/public/blog/{slug}`
   - `/public/blog/category/{slug}`
   - `/public/blog/tag/{slug}`
   - `/public/blog/author/{slug}`
4. Database:
   - Apply `main/resources/db/blog_module_init.sql` if Hibernate DDL auto is not enabled.
   - Verify all `blog_*` tables and indexes exist.
