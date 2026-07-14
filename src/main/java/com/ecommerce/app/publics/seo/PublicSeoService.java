package com.ecommerce.app.publics.seo;

import com.ecommerce.app.module.blog.model.Blog;
import com.ecommerce.app.module.blog.model.BlogCategory;
import com.ecommerce.app.module.settings.model.GlobalSettings;
import com.ecommerce.app.module.settings.services.GlobalSettingsService;
import com.ecommerce.app.product.model.Product;
import com.ecommerce.app.product.model.Productcategory;
import com.ecommerce.app.vendor.model.Vendorprofile;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.HtmlUtils;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class PublicSeoService {

    public static final String ROBOTS_INDEX_FOLLOW = "index,follow";
    public static final String ROBOTS_NOINDEX_FOLLOW = "noindex,follow";
    public static final String ROBOTS_NOINDEX_NOFOLLOW = "noindex,nofollow";

    private final GlobalSettingsService globalSettingsService;
    private final ObjectMapper objectMapper;

    public PublicSeoService(GlobalSettingsService globalSettingsService, ObjectMapper objectMapper) {
        this.globalSettingsService = globalSettingsService;
        this.objectMapper = objectMapper;
    }

    public void apply(Model model, PageSeoMetadata metadata) {
        if (model == null || metadata == null) {
            return;
        }
        model.addAttribute("pageTitle", metadata.getTitle());
        model.addAttribute("pageDescription", metadata.getDescription());
        model.addAttribute("pageCanonicalUrl", metadata.getCanonicalUrl());
        model.addAttribute("pageOgType", metadata.getOgType());
        model.addAttribute("pageOgTitle", metadata.getTitle());
        model.addAttribute("pageOgDescription", metadata.getDescription());
        model.addAttribute("pageOgImageUrl", metadata.getOgImageUrl());
        model.addAttribute("pageRobots", metadata.getRobots());
        if (metadata.getCanonicalUrl() != null && !ROBOTS_NOINDEX_NOFOLLOW.equals(metadata.getRobots()) && !model.containsAttribute("shareUrl")) {
            model.addAttribute("shareUrl", metadata.getCanonicalUrl());
            model.addAttribute("shareTitle", metadata.getTitle());
            model.addAttribute("shareDescription", metadata.getDescription());
            model.addAttribute("sharePageType", metadata.getSharePageType());
            model.addAttribute("shareEntityReference", metadata.getShareEntityReference());
        }
        String jsonLd = toJsonLd(metadata.getJsonLd());
        if (jsonLd != null) {
            model.addAttribute("pageJsonLd", jsonLd);
        }
    }

    public PageSeoMetadata home(HttpServletRequest request, List<Map<String, Object>> featuredProducts) {
        GlobalSettings settings = settings();
        String canonicalUrl = publicUrl(request, "/");
        String title = first(settings.getOgTitle(), settings.getMetaTitle(), settings.getSiteTitle(), settings.getSiteName(), "Online Shopping");
        String description = first(settings.getOgDescription(), settings.getMetaDescription(), "Shop featured products, new arrivals, and top deals.");
        String imageUrl = defaultImage(settings);
        return new PageSeoMetadata(title, description, canonicalUrl, "website", imageUrl, ROBOTS_INDEX_FOLLOW, "HOME", "HOME",
                List.of(organization(settings, canonicalUrl, imageUrl), website(settings, canonicalUrl), productItemList(featuredProducts, canonicalUrl)));
    }

    public PageSeoMetadata productList(HttpServletRequest request, List<Map<String, Object>> products, Vendorprofile vendor, String query, String sort, int page) {
        GlobalSettings settings = settings();
        boolean filtered = hasText(query) || vendor != null || (hasText(sort) && !"latest".equalsIgnoreCase(sort)) || page > 1;
        String title = vendor == null ? "Shop Products" : first(vendor.getCompanyName(), "Vendor") + " Products";
        String description = vendor == null ? "Browse products, deals, categories, and new arrivals." : "Browse products from " + first(vendor.getCompanyName(), "this vendor") + ".";
        String canonicalUrl = publicUrl(request, "/public/product");
        if (vendor != null && hasText(vendor.getUuid())) {
            canonicalUrl = UriComponentsBuilder.fromUriString(canonicalUrl).queryParam("vendor", vendor.getUuid()).build().toUriString();
        }
        return new PageSeoMetadata(title, description, canonicalUrl, "website", defaultImage(settings),
                filtered ? ROBOTS_NOINDEX_FOLLOW : ROBOTS_INDEX_FOLLOW, "PRODUCT_LIST", vendor == null ? "ALL_PRODUCTS" : vendor.getUuid(),
                List.of(productItemList(products, canonicalUrl)));
    }

    public PageSeoMetadata product(HttpServletRequest request, Product product, Map<String, Object> details) {
        GlobalSettings settings = settings();
        String title = first(mapText(details, "metaTitle"), product.getMetaTitle(), mapText(details, "title"), product.getTitle(), "Product");
        String description = first(plainText(mapText(details, "metaDescription")), plainText(product.getMetaDescription()),
                plainText(mapText(details, "shortDescription")), plainText(product.getShortDescription()), "View product details, price, availability, and delivery information.");
        String canonicalUrl = publicUrl(request, "/public/single-product/" + product.getUuid());
        String imageUrl = imageUrl(settings, first(mapText(details, "imageName"), product.getImageName()));
        String productCode = product.getSku() > 0 ? "SKU-" + product.getSku() : product.getUuid();
        List<Map<String, Object>> graph = List.of(
                productJsonLd(settings, product, details, canonicalUrl, imageUrl, productCode),
                breadcrumb(List.of(crumb("Home", publicUrl(request, "/")), crumb("Products", publicUrl(request, "/public/product")), crumb(title, canonicalUrl)))
        );
        return new PageSeoMetadata(title, description, canonicalUrl, "product", imageUrl, ROBOTS_INDEX_FOLLOW, "PRODUCT", productCode, graph);
    }

    public PageSeoMetadata category(HttpServletRequest request, Productcategory category, int productCount) {
        GlobalSettings settings = settings();
        String categoryName = first(category.getName(), "Products");
        String canonicalUrl = publicUrl(request, "/public/product-by-category/" + category.getUuid());
        String description = first(plainText(category.getDescription()), "Browse " + productCount + " products in " + categoryName + ".");
        return new PageSeoMetadata(categoryName + " Products", description, canonicalUrl, "website", imageUrl(settings, category.getImageName()),
                ROBOTS_INDEX_FOLLOW, "CATEGORY", category.getUuid(),
                List.of(breadcrumb(List.of(crumb("Home", publicUrl(request, "/")), crumb("Products", publicUrl(request, "/public/product")), crumb(categoryName, canonicalUrl)))));
    }

    public PageSeoMetadata staticPage(HttpServletRequest request, String path, String title, String description) {
        String canonicalUrl = publicUrl(request, path);
        return new PageSeoMetadata(title, description, canonicalUrl, "website", defaultImage(settings()), ROBOTS_INDEX_FOLLOW, "PAGE", path,
                List.of(webPage(title, description, canonicalUrl)));
    }

    public PageSeoMetadata noIndexPage(HttpServletRequest request, String path, String title, String description) {
        return new PageSeoMetadata(title, description, publicUrl(request, path), "website", defaultImage(settings()), ROBOTS_NOINDEX_FOLLOW, "PRIVATE_PAGE", path, List.of());
    }

    public PageSeoMetadata blogList(HttpServletRequest request, List<Blog> blogs) {
        String canonicalUrl = publicUrl(request, "/public/blog");
        return new PageSeoMetadata("Blog", "Read articles, buying guides, updates, and marketplace news.", canonicalUrl, "website", defaultImage(settings()),
                ROBOTS_INDEX_FOLLOW, "BLOG", "BLOG", List.of(blogItemList(blogs, canonicalUrl)));
    }

    public PageSeoMetadata blogModuleList(HttpServletRequest request, List<Blog> blogs) {
        return blogList(request, blogs);
    }

    public PageSeoMetadata blogCategory(HttpServletRequest request, BlogCategory category, List<Blog> blogs) {
        String title = first(category.getSeoTitle(), category.getName(), "Blog Category");
        String canonicalUrl = publicUrl(request, "/public/blog/category/" + category.getSlug());
        String description = first(plainText(category.getMetaDescription()), plainText(category.getDescription()), "Read articles in " + title + ".");
        return new PageSeoMetadata(title, description, canonicalUrl, "website", defaultImage(settings()), ROBOTS_INDEX_FOLLOW,
                "BLOG_CATEGORY", category.getUuid(), List.of(blogItemList(blogs, canonicalUrl)));
    }

    public PageSeoMetadata blogArticle(HttpServletRequest request, Blog blog) {
        String canonicalUrl = publicUrl(request, "/public/blog/" + blog.getSlug());
        String title = first(blog.getSeo() == null ? null : blog.getSeo().getSeoTitle(), blog.getTitle(), "Article");
        String description = first(
                truncate(plainText(blog.getSeo() == null ? null : blog.getSeo().getMetaDescription()), 180),
                truncate(plainText(blog.getExcerpt()), 180),
                truncate(plainText(blog.getContentPlainText()), 180),
                "Read " + title + ".");
        String imageUrl = imageUrl(settings(), first(blog.getSeo() == null ? null : blog.getSeo().getOpenGraphImage(), blog.getFeaturedImageUrl()));
        return new PageSeoMetadata(title, description, canonicalUrl, "article", imageUrl, ROBOTS_INDEX_FOLLOW, "BLOG_ARTICLE", blog.getUuid(),
                List.of(blogPosting(blog, canonicalUrl, imageUrl, description), breadcrumb(List.of(crumb("Home", publicUrl(request, "/")), crumb("Blog", publicUrl(request, "/public/blog")), crumb(title, canonicalUrl)))));
    }
    public String publicUrl(HttpServletRequest request, String path) {
        String normalizedPath = path == null || path.isBlank() ? "/" : (path.startsWith("/") ? path : "/" + path);
        String baseUrl = safeBaseUrl(settings().getPublicBaseUrl());
        if (baseUrl != null) {
            return baseUrl + normalizedPath;
        }
        if (request == null) {
            request = currentRequest();
        }
        if (request == null) {
            return normalizedPath;
        }
        return ServletUriComponentsBuilder.fromRequest(request)
                .replacePath(request.getContextPath() + normalizedPath)
                .replaceQuery(null)
                .build()
                .toUriString();
    }

    public String plainText(String value) {
        String cleanValue = clean(value);
        if (cleanValue == null) {
            return null;
        }
        return HtmlUtils.htmlUnescape(cleanValue.replaceAll("<[^>]+>", " ")).replaceAll("\\s+", " ").trim();
    }

    private Map<String, Object> organization(GlobalSettings settings, String canonicalUrl, String imageUrl) {
        Map<String, Object> data = map("@context", "https://schema.org", "@type", "Organization", "name", first(settings.getSiteName(), settings.getSiteTitle(), "Online Store"), "url", canonicalUrl);
        if (imageUrl != null) {
            data.put("logo", imageUrl);
        }
        return data;
    }

    private Map<String, Object> website(GlobalSettings settings, String canonicalUrl) {
        Map<String, Object> action = map("@type", "SearchAction", "target", publicUrl(null, "/public/product") + "?q={search_term_string}", "query-input", "required name=search_term_string");
        Map<String, Object> data = map("@context", "https://schema.org", "@type", "WebSite", "name", first(settings.getSiteName(), settings.getSiteTitle(), "Online Store"), "url", canonicalUrl);
        data.put("potentialAction", action);
        return data;
    }

    private Map<String, Object> webPage(String title, String description, String canonicalUrl) {
        return map("@context", "https://schema.org", "@type", "WebPage", "name", title, "description", description, "url", canonicalUrl);
    }

    private Map<String, Object> productJsonLd(GlobalSettings settings, Product product, Map<String, Object> details, String canonicalUrl, String imageUrl, String productCode) {
        Map<String, Object> offer = map("@type", "Offer", "url", canonicalUrl, "priceCurrency", first(settings.getCurrency(), "BDT"),
                "price", money(details == null ? null : details.get("afterDiscountRemainingAmount"), product.getSalesPrice()),
                "availability", available(product, details) ? "https://schema.org/InStock" : "https://schema.org/OutOfStock",
                "itemCondition", "https://schema.org/NewCondition");
        Map<String, Object> data = map("@context", "https://schema.org", "@type", "Product", "name", first(mapText(details, "title"), product.getTitle()),
                "description", first(plainText(mapText(details, "shortDescription")), plainText(product.getShortDescription())), "sku", productCode, "url", canonicalUrl, "offers", offer);
        if (imageUrl != null) {
            data.put("image", List.of(imageUrl));
        }
        String brand = first(mapText(details, "manufacturerName"), product.getManufacturer() == null ? null : product.getManufacturer().getName());
        if (brand != null) {
            data.put("brand", map("@type", "Brand", "name", brand));
        }
        return data;
    }

    private Map<String, Object> blogPosting(Blog blog, String canonicalUrl, String imageUrl, String description) {
        Map<String, Object> data = map("@context", "https://schema.org", "@type", "BlogPosting", "headline", first(blog.getTitle(), "Article"), "description", description, "url", canonicalUrl);
        if (imageUrl != null) {
            data.put("image", List.of(imageUrl));
        }
        if (blog.getPublishedAt() != null) {
            data.put("datePublished", blog.getPublishedAt().toString());
        } else if (blog.getCreatedAt() != null) {
            data.put("datePublished", blog.getCreatedAt().toString());
        }
        if (blog.getUpdatedAt() != null) {
            data.put("dateModified", blog.getUpdatedAt().toString());
        }
        if (blog.getCreatedBy() != null && !blog.getCreatedBy().isBlank()) {
            data.put("author", map("@type", "Person", "name", blog.getCreatedBy()));
        }
        return data;
    }

    private Map<String, Object> productItemList(List<Map<String, Object>> products, String canonicalUrl) {
        List<Map<String, Object>> items = new ArrayList<>();
        if (products != null) {
            int position = 1;
            for (Map<String, Object> product : products.stream().limit(24).toList()) {
                String uuid = mapText(product, "uuid");
                String name = mapText(product, "title");
                if (uuid != null && name != null) {
                    items.add(map("@type", "ListItem", "position", position++, "name", name, "url", publicUrl(null, "/public/single-product/" + uuid)));
                }
            }
        }
        Map<String, Object> data = map("@context", "https://schema.org", "@type", "ItemList", "url", canonicalUrl);
        data.put("itemListElement", items);
        return data;
    }

    private Map<String, Object> blogItemList(List<Blog> blogs, String canonicalUrl) {
        List<Map<String, Object>> items = new ArrayList<>();
        if (blogs != null) {
            int position = 1;
            for (Blog blog : blogs.stream().limit(24).toList()) {
                items.add(map("@type", "ListItem", "position", position++, "name", first(blog.getTitle(), "Article"), "url", publicUrl(null, "/public/blog/" + blog.getSlug())));
            }
        }
        Map<String, Object> data = map("@context", "https://schema.org", "@type", "ItemList", "url", canonicalUrl);
        data.put("itemListElement", items);
        return data;
    }

    private Map<String, Object> breadcrumb(List<Map<String, Object>> crumbs) {
        List<Map<String, Object>> items = new ArrayList<>();
        int position = 1;
        for (Map<String, Object> crumb : crumbs) {
            items.add(map("@type", "ListItem", "position", position++, "name", crumb.get("name"), "item", crumb.get("url")));
        }
        Map<String, Object> data = map("@context", "https://schema.org", "@type", "BreadcrumbList");
        data.put("itemListElement", items);
        return data;
    }

    private Map<String, Object> crumb(String name, String url) {
        return map("name", name, "url", url);
    }

    private String toJsonLd(List<Map<String, Object>> graph) {
        if (graph == null || graph.isEmpty()) {
            return null;
        }
        try {
            Object payload = graph.size() == 1 ? graph.get(0) : Map.of("@context", "https://schema.org", "@graph", graph);
            return objectMapper.writeValueAsString(payload).replace("&", "\\u0026").replace("<", "\\u003c").replace(">", "\\u003e").replace("</", "<\\/");
        } catch (JsonProcessingException ex) {
            return null;
        }
    }

    private String imageUrl(GlobalSettings settings, String imageName) {
        String image = clean(imageName);
        if (image != null && !image.startsWith("/")) {
            image = "/files/" + image;
        }
        String imageUrl = absoluteUrl(settings, image);
        return imageUrl == null ? defaultImage(settings) : imageUrl;
    }

    private String defaultImage(GlobalSettings settings) {
        return absoluteUrl(settings, settings.getOgImage());
    }

    private String absoluteUrl(GlobalSettings settings, String value) {
        String cleanValue = clean(value);
        String baseUrl = safeBaseUrl(settings == null ? null : settings.getPublicBaseUrl());
        if (cleanValue == null) {
            return null;
        }
        if (safeBaseUrl(cleanValue) != null) {
            return cleanValue;
        }
        return cleanValue.startsWith("/") && baseUrl != null ? baseUrl + cleanValue : null;
    }

    private boolean available(Product product, Map<String, Object> details) {
        String label = mapText(details, "availabilityLabel");
        if (label != null && label.toLowerCase(Locale.ROOT).contains("out")) {
            return false;
        }
        return !Boolean.TRUE.equals(product.getManageStock()) || product.getStockAvailableQuantity() == null || product.getStockAvailableQuantity().compareTo(BigDecimal.ZERO) > 0;
    }

    private BigDecimal money(Object preferred, BigDecimal fallback) {
        if (preferred instanceof BigDecimal value) {
            return value;
        }
        if (preferred instanceof Number value) {
            return BigDecimal.valueOf(value.doubleValue());
        }
        if (preferred != null) {
            try {
                return new BigDecimal(preferred.toString().trim());
            } catch (NumberFormatException ignored) {
                return fallback == null ? BigDecimal.ZERO : fallback;
            }
        }
        return fallback == null ? BigDecimal.ZERO : fallback;
    }

    private String safeBaseUrl(String value) {
        String cleanValue = clean(value);
        if (cleanValue == null || !cleanValue.startsWith("https://")) {
            return null;
        }
        String lowerValue = cleanValue.toLowerCase(Locale.ROOT);
        if (lowerValue.contains("localhost") || lowerValue.contains("127.0.0.1") || lowerValue.contains("0.0.0.0")
                || lowerValue.matches("https://10\\..*") || lowerValue.matches("https://172\\.(1[6-9]|2[0-9]|3[0-1])\\..*")
                || lowerValue.matches("https://192\\.168\\..*")) {
            return null;
        }
        while (cleanValue.endsWith("/")) {
            cleanValue = cleanValue.substring(0, cleanValue.length() - 1);
        }
        return cleanValue;
    }

    private GlobalSettings settings() {
        return globalSettingsService.getActiveSettings();
    }

    private HttpServletRequest currentRequest() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            return attributes.getRequest();
        }
        return null;
    }

    private String mapText(Map<String, Object> data, String key) {
        Object value = data == null ? null : data.get(key);
        return value == null ? null : value.toString();
    }

    private String truncate(String value, int maxLength) {
        String cleanValue = clean(value);
        return cleanValue == null || cleanValue.length() <= maxLength ? cleanValue : cleanValue.substring(0, Math.max(0, maxLength - 1)).trim() + "...";
    }

    private String first(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            String cleanValue = clean(value);
            if (cleanValue != null) {
                return cleanValue;
            }
        }
        return null;
    }

    private boolean hasText(String value) {
        return clean(value) != null;
    }

    private String clean(String value) {
        if (value == null) {
            return null;
        }
        String cleanValue = value.trim();
        return cleanValue.isEmpty() ? null : cleanValue;
    }

    private Map<String, Object> map(Object... entries) {
        Map<String, Object> data = new LinkedHashMap<>();
        for (int i = 0; i + 1 < entries.length; i += 2) {
            if (entries[i + 1] != null) {
                data.put(String.valueOf(entries[i]), entries[i + 1]);
            }
        }
        return data;
    }
}
