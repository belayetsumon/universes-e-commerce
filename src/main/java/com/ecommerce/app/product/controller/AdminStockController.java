package com.ecommerce.app.product.controller;

import com.ecommerce.app.product.dto.ProductStockReportRow;
import com.ecommerce.app.product.dto.ProductStockReportSummary;
import com.ecommerce.app.product.model.Product;
import com.ecommerce.app.product.model.ProductVariant;
import com.ecommerce.app.product.model.StockTransaction;
import com.ecommerce.app.product.model.StockTransactionTypeEnum;
import com.ecommerce.app.product.ripository.ProductcategoryRepository;
import com.ecommerce.app.product.ripository.ProductRepository;
import com.ecommerce.app.product.ripository.ProductVariantRepository;
import com.ecommerce.app.product.ripository.StockTransactionRepository;
import com.ecommerce.app.product.services.ProductStockReportPdfService;
import com.ecommerce.app.product.services.StockInventoryReportService;
import com.ecommerce.app.product.services.StockLedgerService;
import com.ecommerce.app.vendor.repository.VendorprofileRepository;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/stock")
//@PreAuthorize("hasAuthority('admin')")
public class AdminStockController {

    @Autowired
    private StockTransactionRepository stockTransactionRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @Autowired
    private StockLedgerService stockLedgerService;

    @Autowired
    private StockInventoryReportService stockInventoryReportService;

    @Autowired
    private ProductStockReportPdfService productStockReportPdfService;

    @Autowired
    private ProductcategoryRepository productcategoryRepository;

    @Autowired
    private VendorprofileRepository vendorprofileRepository;

    @GetMapping("/current")
    public String currentStock(
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "vendorId", required = false) Long vendorId,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "stockFilter", required = false, defaultValue = "all") String stockFilter,
            @RequestParam(value = "lowStockThreshold", required = false, defaultValue = "5") BigDecimal lowStockThreshold,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "25") int size,
            Model model
    ) {
        Page<ProductStockReportRow> stockPage = Page.empty(currentStockPageable(page, size));
        ProductStockReportSummary summary = new ProductStockReportSummary();
        try {
            List<ProductStockReportRow> rows = stockInventoryReportService.findCurrentStock(
                    vendorId, categoryId, q, stockFilter, lowStockThreshold);
            summary = stockInventoryReportService.summarize(rows);
            stockPage = currentStockPage(rows, page, size);
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Runtime error while loading current stock: " + userFacingMessage(e));
        }
        model.addAttribute("rows", stockPage.getContent());
        model.addAttribute("stockPage", stockPage);
        model.addAttribute("pageNumbers", pageNumbers(stockPage));
        model.addAttribute("totalRows", stockPage.getTotalElements());
        model.addAttribute("summary", summary);
        model.addAttribute("q", q);
        model.addAttribute("selectedVendorId", vendorId);
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("stockFilter", stockFilter);
        model.addAttribute("lowStockThreshold", lowStockThreshold);
        model.addAttribute("size", normalizePageSize(size));
        model.addAttribute("vendors", vendorprofileRepository.findAll(Sort.by(Sort.Direction.ASC, "companyName", "id")));
        model.addAttribute("categories", productcategoryRepository.findAll(Sort.by(Sort.Direction.ASC, "name", "id")));
        return "admin/stock/current";
    }

    @GetMapping("/current/pdf")
    public ResponseEntity<byte[]> currentStockPdf(
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "vendorId", required = false) Long vendorId,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "stockFilter", required = false, defaultValue = "all") String stockFilter,
            @RequestParam(value = "lowStockThreshold", required = false, defaultValue = "5") BigDecimal lowStockThreshold
    ) {
        try {
            List<ProductStockReportRow> rows = stockInventoryReportService.findCurrentStock(
                    vendorId, categoryId, q, stockFilter, lowStockThreshold);
            ProductStockReportSummary summary = stockInventoryReportService.summarize(rows);
            byte[] pdfBytes = productStockReportPdfService.generateAdminCurrentStockReport(
                    rows,
                    summary,
                    currentStockPdfFilters(q, vendorId, categoryId, stockFilter, lowStockThreshold)
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", productStockReportPdfService.currentStockFilename());
            headers.setContentLength(pdfBytes.length);
            return ResponseEntity.ok().headers(headers).body(pdfBytes);
        } catch (Exception e) {
            String message = "Unable to generate current stock PDF: " + userFacingMessage(e);
            return ResponseEntity.internalServerError()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(message.getBytes(StandardCharsets.UTF_8));
        }
    }

    @GetMapping("/transactions")
    public String transactions(
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "vendorId", required = false) Long vendorId,
            @RequestParam(value = "type", required = false) StockTransactionTypeEnum type,
            @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "25") int size,
            Model model
    ) {
        Pageable pageable = transactionPageable(page, size);
        Page<StockTransaction> transactions = Page.empty(pageable);
        String search = normalizeSearch(q);
        String searchPattern = searchPattern(search);
        Integer productSku = parseProductSku(search);
        try {
            transactions = stockTransactionRepository.searchTransactions(
                    vendorId,
                    type,
                    startOfDay(from),
                    startOfNextDay(to),
                    searchPattern,
                    productSku,
                    pageable
            );
        } catch (Exception e) {
            // 2026-04-22: Keep stock history page usable even when inventory loading fails.
            model.addAttribute("errorMessage", "Runtime error while loading stock transactions: " + userFacingMessage(e));
        }
        populateTransactionListModel(model, transactions, q, vendorId, type, from, to, size);
        return "admin/stock/transactions";
    }

    @GetMapping("/receive")
    public String receiveForm(
            @RequestParam(value = "productId", required = false) Long productId,
            @RequestParam(value = "catalogVariantUuid", required = false) String catalogVariantUuid,
            Model model
    ) {
        populateStockFormModel(model, "Receive Stock", "/admin/stock/receive", "RECEIVE", productId, catalogVariantUuid);
        return "admin/stock/form";
    }

    @PostMapping("/receive")
    public String receiveStock(
            @RequestParam("productId") Long productId,
            @RequestParam(value = "catalogVariantUuid", required = false) String catalogVariantUuid,
            @RequestParam("quantity") BigDecimal quantity,
            @RequestParam(value = "note", required = false) String note,
            RedirectAttributes redirectAttributes
    ) {
        try {
            String idempotencyKey = UUID.randomUUID().toString();
            stockLedgerService.receiveStock(productId, catalogVariantUuid, quantity, idempotencyKey, note);
            redirectAttributes.addFlashAttribute("successMessage", "Stock received successfully.");
            return "redirect:/admin/stock/current";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Runtime error while receiving stock: " + userFacingMessage(e));
            return "redirect:/admin/stock/current";
        }
    }

    @GetMapping("/adjust")
    public String adjustForm(
            @RequestParam(value = "productId", required = false) Long productId,
            @RequestParam(value = "catalogVariantUuid", required = false) String catalogVariantUuid,
            Model model
    ) {
        populateStockFormModel(model, "Adjust Stock", "/admin/stock/adjust", "ADJUST", productId, catalogVariantUuid);
        return "admin/stock/form";
    }

    @PostMapping("/adjust")
    public String adjustStock(
            @RequestParam("productId") Long productId,
            @RequestParam(value = "catalogVariantUuid", required = false) String catalogVariantUuid,
            @RequestParam("quantityDelta") BigDecimal quantityDelta,
            @RequestParam(value = "note", required = false) String note,
            RedirectAttributes redirectAttributes
    ) {
        try {
            String idempotencyKey = UUID.randomUUID().toString();
            stockLedgerService.adjustAvailableStock(productId, catalogVariantUuid, quantityDelta, idempotencyKey, note);
            redirectAttributes.addFlashAttribute("successMessage", "Stock adjusted successfully.");
            return "redirect:/admin/stock/current";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Runtime error while adjusting stock: " + userFacingMessage(e));
            return "redirect:/admin/stock/current";
        }
    }

    private void populateStockFormModel(Model model, String pageTitle, String actionUrl, String mode) {
        populateStockFormModel(model, pageTitle, actionUrl, mode, null, null);
    }

    private void populateStockFormModel(
            Model model,
            String pageTitle,
            String actionUrl,
            String mode,
            Long selectedProductId,
            String selectedCatalogVariantUuid
    ) {
        List<Product> products = productRepository.findAll(Sort.by(Sort.Direction.ASC, "title", "id"));
        List<ProductVariant> catalogVariants = productVariantRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
        model.addAttribute("products", products);
        model.addAttribute("catalogVariants", catalogVariants);
        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("stockAction", actionUrl);
        model.addAttribute("stockMode", mode);
        model.addAttribute("selectedProductId", selectedProductId);
        model.addAttribute("selectedCatalogVariantUuid", selectedCatalogVariantUuid);
    }

    private String userFacingMessage(Exception e) {
        Throwable rootCause = NestedExceptionUtils.getMostSpecificCause(e);
        String message = rootCause == null ? e.getMessage() : rootCause.getMessage();
        return message == null || message.isBlank() ? e.getClass().getSimpleName() : message;
    }

    private void populateTransactionListModel(
            Model model,
            Page<StockTransaction> transactions,
            String q,
            Long vendorId,
            StockTransactionTypeEnum type,
            LocalDate from,
            LocalDate to,
            int size
    ) {
        model.addAttribute("transactions", transactions);
        model.addAttribute("pageNumbers", pageNumbers(transactions));
        model.addAttribute("q", q);
        model.addAttribute("selectedVendorId", vendorId);
        model.addAttribute("selectedType", type);
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        model.addAttribute("size", normalizePageSize(size));
        model.addAttribute("transactionTypes", StockTransactionTypeEnum.values());
        model.addAttribute("vendors", vendorprofileRepository.findAll(Sort.by(Sort.Direction.ASC, "companyName", "id")));
    }

    private Pageable transactionPageable(int page, int size) {
        return PageRequest.of(Math.max(page, 0), normalizePageSize(size), Sort.by(Sort.Order.desc("created"), Sort.Order.desc("id")));
    }

    private Pageable currentStockPageable(int page, int size) {
        return PageRequest.of(Math.max(page, 0), normalizePageSize(size));
    }

    private Page<ProductStockReportRow> currentStockPage(List<ProductStockReportRow> rows, int page, int size) {
        List<ProductStockReportRow> safeRows = rows == null ? List.of() : rows;
        Pageable pageable = currentStockPageable(page, size);
        if (safeRows.isEmpty()) {
            return Page.empty(pageable);
        }
        int pageSize = pageable.getPageSize();
        int requestedPage = pageable.getPageNumber();
        int lastPage = Math.max(0, (safeRows.size() - 1) / pageSize);
        int pageNumber = Math.min(requestedPage, lastPage);
        Pageable adjustedPageable = PageRequest.of(pageNumber, pageSize);
        int start = Math.min((int) adjustedPageable.getOffset(), safeRows.size());
        int end = Math.min(start + pageSize, safeRows.size());
        return new PageImpl<>(safeRows.subList(start, end), adjustedPageable, safeRows.size());
    }

    private Map<String, String> currentStockPdfFilters(
            String q,
            Long vendorId,
            Long categoryId,
            String stockFilter,
            BigDecimal lowStockThreshold
    ) {
        Map<String, String> filters = new LinkedHashMap<>();
        filters.put("Search", safeText(q, "All products"));
        filters.put("Vendor", selectedVendorLabel(vendorId));
        filters.put("Category", selectedCategoryLabel(categoryId));
        filters.put("Stock", stockFilterLabel(stockFilter));
        filters.put("Low threshold", lowStockThreshold == null ? "5" : lowStockThreshold.toPlainString());
        return filters;
    }

    private String selectedVendorLabel(Long vendorId) {
        if (vendorId == null) {
            return "All vendors";
        }
        return vendorprofileRepository.findById(vendorId)
                .map(vendor -> safeText(vendor.getCompanyName(), safeText(vendor.getVendorCode(), "Vendor ID " + vendorId)))
                .orElse("Vendor ID " + vendorId);
    }

    private String selectedCategoryLabel(Long categoryId) {
        if (categoryId == null) {
            return "All categories";
        }
        return productcategoryRepository.findById(categoryId)
                .map(category -> safeText(category.getName(), "Category ID " + categoryId))
                .orElse("Category ID " + categoryId);
    }

    private String stockFilterLabel(String stockFilter) {
        if (stockFilter == null || stockFilter.isBlank()) {
            return "All stock";
        }
        return switch (stockFilter) {
            case "in_stock" -> "In stock";
            case "low_stock" -> "Low stock";
            case "out_of_stock" -> "Out of stock";
            case "reserved" -> "Has reserved";
            case "sold" -> "Has sold";
            case "product" -> "Product rows";
            case "variant" -> "Variant rows";
            default -> "All stock";
        };
    }

    private String safeText(String value, String fallback) {
        return value != null && !value.isBlank() ? value.trim() : fallback;
    }

    private int normalizePageSize(int size) {
        if (size <= 10) {
            return 10;
        }
        if (size <= 25) {
            return 25;
        }
        if (size <= 50) {
            return 50;
        }
        return 100;
    }

    private String normalizeSearch(String q) {
        return q == null || q.trim().isEmpty() ? null : q.trim();
    }

    private String searchPattern(String search) {
        return search == null ? null : "%" + search + "%";
    }

    private Integer parseProductSku(String search) {
        if (search == null || !search.matches("\\d+")) {
            return null;
        }
        try {
            return Integer.valueOf(search);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private LocalDateTime startOfDay(LocalDate date) {
        return date == null ? null : date.atStartOfDay();
    }

    private LocalDateTime startOfNextDay(LocalDate date) {
        return date == null ? null : date.plusDays(1).atStartOfDay();
    }

    private List<Integer> pageNumbers(Page<?> page) {
        int totalPages = page.getTotalPages();
        if (totalPages <= 0) {
            return List.of();
        }
        int start = Math.max(0, page.getNumber() - 2);
        int end = Math.min(totalPages - 1, start + 4);
        start = Math.max(0, end - 4);
        List<Integer> numbers = new ArrayList<>();
        for (int i = start; i <= end; i++) {
            numbers.add(i);
        }
        return numbers;
    }
}
