package com.ecommerce.app.vendor.controller;

import com.ecommerce.app.product.model.Product;
import com.ecommerce.app.product.model.ProductVariant;
import com.ecommerce.app.product.model.StockTransaction;
import com.ecommerce.app.product.ripository.ProductRepository;
import com.ecommerce.app.product.ripository.ProductVariantRepository;
import com.ecommerce.app.product.ripository.StockTransactionRepository;
import com.ecommerce.app.product.services.StockLedgerService;
import com.ecommerce.app.vendor.user.componant.VendorUserContext;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/vendor/stock")
public class VendorStockController {

    @Autowired
    private StockTransactionRepository stockTransactionRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @Autowired
    private StockLedgerService stockLedgerService;

    @Autowired
    private VendorUserContext vendorUserContext;

//    @PreAuthorize("""
//            @vendorAccessAuthorityChecker.hasAuthority(authentication, 'vendor.stock.read')
//            or @vendorAccessAuthorityChecker.hasAuthority(authentication, 'vendor.stock.manage')
//            or @vendorRoleChecker.hasVendorRole(authentication, 'ADMIN')
//            or @vendorRoleChecker.hasVendorRole(authentication, 'OWNER')
//            or @vendorRoleChecker.hasVendorRole(authentication, 'VENDOR_OWNER')
//            """)
    @GetMapping("/transactions")
    public String transactions(Model model) {
        try {
            Long vendorId = vendorUserContext.getActiveVendor().getId();
            List<StockTransaction> transactions = stockTransactionRepository.findAll(Sort.by(Sort.Direction.DESC, "created", "id"))
                    .stream()
                    .filter(tx -> tx.getProduct() != null
                    && tx.getProduct().getVendorprofile() != null
                    && tx.getProduct().getVendorprofile().getId() != null
                    && tx.getProduct().getVendorprofile().getId().equals(vendorId))
                    .collect(Collectors.toList());
            model.addAttribute("transactions", transactions);
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Runtime error while loading vendor stock transactions: " + userFacingMessage(e));
            model.addAttribute("transactions", List.of());
        }
        return "vendor/stock/transactions";
    }

//    @PreAuthorize("""
//            @vendorAccessAuthorityChecker.hasAuthority(authentication, 'vendor.stock.manage')
//            or @vendorRoleChecker.hasVendorRole(authentication, 'ADMIN')
//            or @vendorRoleChecker.hasVendorRole(authentication, 'OWNER')
//            or @vendorRoleChecker.hasVendorRole(authentication, 'VENDOR_OWNER')
//            """)
    @GetMapping("/receive")
    public String receiveForm(Model model) {
        populateStockFormModel(model, "Receive Stock", "/vendor/stock/receive", "RECEIVE");
        return "vendor/stock/form";
    }

//    @PreAuthorize("""
//            @vendorAccessAuthorityChecker.hasAuthority(authentication, 'vendor.stock.manage')
//            or @vendorRoleChecker.hasVendorRole(authentication, 'ADMIN')
//            or @vendorRoleChecker.hasVendorRole(authentication, 'OWNER')
//            or @vendorRoleChecker.hasVendorRole(authentication, 'VENDOR_OWNER')
//            """)
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
            validateVendorOwnership(productId);
            String idempotencyKey = UUID.randomUUID().toString();
            stockLedgerService.receiveStock(productId, catalogVariantUuid, quantity, idempotencyKey, note);
            redirectAttributes.addFlashAttribute("successMessage", "Stock received successfully.");
            return "redirect:/vendor/stock/transactions";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Runtime error while receiving stock: " + userFacingMessage(e));
            populateStockFormModel(model, "Receive Stock", "/vendor/stock/receive", "RECEIVE");
            return "vendor/stock/form";
        }
    }

//    @PreAuthorize("""
//            @vendorAccessAuthorityChecker.hasAuthority(authentication, 'vendor.stock.manage')
//            or @vendorRoleChecker.hasVendorRole(authentication, 'ADMIN')
//            or @vendorRoleChecker.hasVendorRole(authentication, 'OWNER')
//            or @vendorRoleChecker.hasVendorRole(authentication, 'VENDOR_OWNER')
//            """)
    @GetMapping("/adjust")
    public String adjustForm(Model model) {
        populateStockFormModel(model, "Adjust Stock", "/vendor/stock/adjust", "ADJUST");
        return "vendor/stock/form";
    }

//    @PreAuthorize("""
//            @vendorAccessAuthorityChecker.hasAuthority(authentication, 'vendor.stock.manage')
//            or @vendorRoleChecker.hasVendorRole(authentication, 'ADMIN')
//            or @vendorRoleChecker.hasVendorRole(authentication, 'OWNER')
//            or @vendorRoleChecker.hasVendorRole(authentication, 'VENDOR_OWNER')
//            """)
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
            validateVendorOwnership(productId);
            String idempotencyKey = UUID.randomUUID().toString();
            stockLedgerService.adjustAvailableStock(productId, catalogVariantUuid, quantityDelta, idempotencyKey, note);
            redirectAttributes.addFlashAttribute("successMessage", "Stock adjusted successfully.");
            return "redirect:/vendor/stock/transactions";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Runtime error while adjusting stock: " + userFacingMessage(e));
            populateStockFormModel(model, "Adjust Stock", "/vendor/stock/adjust", "ADJUST");
            return "vendor/stock/form";
        }
    }

    private void validateVendorOwnership(Long productId) {
        Long vendorId = vendorUserContext.getActiveVendor().getId();
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found."));
        if (product.getVendorprofile() == null || product.getVendorprofile().getId() == null
                || !product.getVendorprofile().getId().equals(vendorId)) {
            throw new IllegalStateException("You are not allowed to update stock for this product.");
        }
    }

    private void populateStockFormModel(Model model, String pageTitle, String actionUrl, String mode) {
        Long vendorId = vendorUserContext.getActiveVendor().getId();
        List<Product> products = productRepository.findByVendorprofile_IdOrderByIdDesc(vendorId);
        List<ProductVariant> catalogVariants = productVariantRepository.findByProduct_Vendorprofile_IdOrderByProduct_TitleAscIdAsc(vendorId);
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
