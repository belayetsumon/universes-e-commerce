package com.ecommerce.app.product.controller;

import com.ecommerce.app.product.model.Product;
import com.ecommerce.app.product.model.ProductVariant;
import com.ecommerce.app.product.model.StockTransaction;
import com.ecommerce.app.product.ripository.ProductRepository;
import com.ecommerce.app.product.ripository.ProductVariantRepository;
import com.ecommerce.app.product.ripository.StockTransactionRepository;
import com.ecommerce.app.product.services.StockLedgerService;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
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

    @GetMapping("/transactions")
    public String transactions(Model model) {
        try {
            List<StockTransaction> transactions = stockTransactionRepository.findAll(Sort.by(Sort.Direction.DESC, "created", "id"));
            model.addAttribute("transactions", transactions);
        } catch (Exception e) {
            // 2026-04-22: Keep stock history page usable even when inventory loading fails.
            model.addAttribute("errorMessage", "Runtime error while loading stock transactions: " + userFacingMessage(e));
            model.addAttribute("transactions", List.of());
        }
        return "admin/stock/transactions";
    }

    @GetMapping("/receive")
    public String receiveForm(Model model) {
        populateStockFormModel(model, "Receive Stock", "/admin/stock/receive", "RECEIVE");
        return "admin/stock/form";
    }

    @PostMapping("/receive")
    public String receiveStock(
            @RequestParam("productId") Long productId,
            @RequestParam(value = "catalogVariantUuid", required = false) String catalogVariantUuid,
            @RequestParam("quantity") BigDecimal quantity,
            @RequestParam(value = "note", required = false) String note,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        try {
            String idempotencyKey = UUID.randomUUID().toString();
            stockLedgerService.receiveStock(productId, catalogVariantUuid, quantity, idempotencyKey, note);
            redirectAttributes.addFlashAttribute("successMessage", "Stock received successfully.");
            return "redirect:/admin/stock/transactions";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Runtime error while receiving stock: " + userFacingMessage(e));
            populateStockFormModel(model, "Receive Stock", "/admin/stock/receive", "RECEIVE");
            return "admin/stock/form";
        }
    }

    @GetMapping("/adjust")
    public String adjustForm(Model model) {
        populateStockFormModel(model, "Adjust Stock", "/admin/stock/adjust", "ADJUST");
        return "admin/stock/form";
    }

    @PostMapping("/adjust")
    public String adjustStock(
            @RequestParam("productId") Long productId,
            @RequestParam(value = "catalogVariantUuid", required = false) String catalogVariantUuid,
            @RequestParam("quantityDelta") BigDecimal quantityDelta,
            @RequestParam(value = "note", required = false) String note,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        try {
            String idempotencyKey = UUID.randomUUID().toString();
            stockLedgerService.adjustAvailableStock(productId, catalogVariantUuid, quantityDelta, idempotencyKey, note);
            redirectAttributes.addFlashAttribute("successMessage", "Stock adjusted successfully.");
            return "redirect:/admin/stock/transactions";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Runtime error while adjusting stock: " + userFacingMessage(e));
            populateStockFormModel(model, "Adjust Stock", "/admin/stock/adjust", "ADJUST");
            return "admin/stock/form";
        }
    }

    private void populateStockFormModel(Model model, String pageTitle, String actionUrl, String mode) {
        List<Product> products = productRepository.findAll(Sort.by(Sort.Direction.ASC, "title", "id"));
        List<ProductVariant> catalogVariants = productVariantRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
        model.addAttribute("products", products);
        model.addAttribute("catalogVariants", catalogVariants);
        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("stockAction", actionUrl);
        model.addAttribute("stockMode", mode);
    }

    private String userFacingMessage(Exception e) {
        Throwable rootCause = NestedExceptionUtils.getMostSpecificCause(e);
        String message = rootCause == null ? e.getMessage() : rootCause.getMessage();
        return message == null || message.isBlank() ? e.getClass().getSimpleName() : message;
    }
}
