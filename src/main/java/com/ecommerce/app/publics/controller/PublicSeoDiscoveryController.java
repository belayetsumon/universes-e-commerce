package com.ecommerce.app.publics.controller;

import com.ecommerce.app.module.blog.model.Blog;
import com.ecommerce.app.module.blog.model.BlogPublicationStatus;
import com.ecommerce.app.module.blog.repository.BlogRepository;
import com.ecommerce.app.product.model.Product;
import com.ecommerce.app.product.model.ProductStatusEnum;
import com.ecommerce.app.product.model.Productcategory;
import com.ecommerce.app.product.ripository.ProductRepository;
import com.ecommerce.app.product.ripository.ProductcategoryRepository;
import com.ecommerce.app.publics.seo.PublicSeoService;
import jakarta.servlet.http.HttpServletRequest;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class PublicSeoDiscoveryController {

    private final PublicSeoService publicSeoService;
    private final ProductRepository productRepository;
    private final ProductcategoryRepository productcategoryRepository;
    private final BlogRepository blogRepository;

    public PublicSeoDiscoveryController(
            PublicSeoService publicSeoService,
            ProductRepository productRepository,
            ProductcategoryRepository productcategoryRepository,
            BlogRepository blogRepository) {
        this.publicSeoService = publicSeoService;
        this.productRepository = productRepository;
        this.productcategoryRepository = productcategoryRepository;
        this.blogRepository = blogRepository;
    }

    @GetMapping(value = "/robots.txt", produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    public String robots(HttpServletRequest request) {
        return String.join("\n",
                "User-agent: *",
                "Allow: /",
                "Disallow: /admin/",
                "Disallow: /vendor/",
                "Disallow: /customer/",
                "Disallow: /cart/",
                "Disallow: /order/",
                "Disallow: /wishlist/",
                "Disallow: /payment/",
                "Disallow: /*?ref=",
                "Sitemap: " + publicSeoService.publicUrl(request, "/sitemap.xml"),
                "LLMs: " + publicSeoService.publicUrl(request, "/llms.txt"),
                ""
        );
    }

    @GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseBody
    public String sitemap(HttpServletRequest request) {
        List<String> urls = new ArrayList<>();
        addUrl(urls, publicSeoService.publicUrl(request, "/"), "daily", "1.0", null);
        addUrl(urls, publicSeoService.publicUrl(request, "/public/product"), "daily", "0.9", null);
        addUrl(urls, publicSeoService.publicUrl(request, "/public/blog"), "weekly", "0.7", null);
        for (String path : staticPaths()) {
            addUrl(urls, publicSeoService.publicUrl(request, path), "monthly", "0.6", null);
        }
        for (Productcategory category : productcategoryRepository.findByStatus(ProductStatusEnum.Active)) {
            if (category.getUuid() != null && !category.getUuid().isBlank()) {
                addUrl(urls, publicSeoService.publicUrl(request, "/public/product-by-category/" + category.getUuid()), "weekly", "0.8", modified(category.getModified()));
            }
        }
        for (Product product : productRepository.findByStatusOrderByIdDesc(ProductStatusEnum.Active, PageRequest.of(0, 1000))) {
            if (product.getUuid() != null && !product.getUuid().isBlank()) {
                addUrl(urls, publicSeoService.publicUrl(request, "/public/single-product/" + product.getUuid()), "weekly", "0.9", modified(product.getModified()));
            }
        }
        for (Blog blog : blogRepository.findByStatusAndDeletedFlagFalseAndActiveFlagTrue(BlogPublicationStatus.PUBLISHED, PageRequest.of(0, 500)).getContent()) {
            if (blog.getSlug() != null && !blog.getSlug().isBlank()) {
                addUrl(urls, publicSeoService.publicUrl(request, "/public/blog/" + blog.getSlug()), "weekly", "0.7", modified(blog.getUpdatedAt()));
            }
        }
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n"
                + String.join("", urls)
                + "</urlset>\n";
    }
    @GetMapping(value = "/llms.txt", produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    public String llms(HttpServletRequest request) {
        return String.join("\n",
                "# " + publicSeoService.publicUrl(request, "/"),
                "",
                "This is a public eCommerce storefront with product discovery, product detail pages, category pages, blog articles, and customer policy pages.",
                "",
                "## Important public routes",
                "- Home: " + publicSeoService.publicUrl(request, "/"),
                "- Products: " + publicSeoService.publicUrl(request, "/public/product"),
                "- Blog: " + publicSeoService.publicUrl(request, "/public/blog"),
                "- Help: " + publicSeoService.publicUrl(request, "/public/help"),
                "- Privacy Policy: " + publicSeoService.publicUrl(request, "/public/privacy-policy"),
                "- Terms: " + publicSeoService.publicUrl(request, "/public/terms-and-conditions"),
                "",
                "## Crawl guidance",
                "Use canonical URLs. Ignore referral parameters, cart, checkout, payment, admin, vendor, and customer account pages.",
                ""
        );
    }

    private List<String> staticPaths() {
        return List.of(
                "/public/about-us",
                "/public/contact-us",
                "/public/help",
                "/public/privacy-policy",
                "/public/terms-of-use",
                "/public/payment-methods",
                "/public/returns-replacements",
                "/public/refund-returns-policy",
                "/public/shipping-rates-policies",
                "/public/terms-and-conditions"
        );
    }

    private void addUrl(List<String> urls, String loc, String changeFreq, String priority, String lastModified) {
        urls.add("  <url>\n"
                + "    <loc>" + xml(loc) + "</loc>\n"
                + (lastModified == null ? "" : "    <lastmod>" + xml(lastModified) + "</lastmod>\n")
                + "    <changefreq>" + changeFreq + "</changefreq>\n"
                + "    <priority>" + priority + "</priority>\n"
                + "  </url>\n");
    }

    private String modified(java.time.LocalDateTime value) {
        return value == null ? null : value.toLocalDate().format(DateTimeFormatter.ISO_DATE);
    }

    private String xml(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
