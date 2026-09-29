package com.ecommerce.app.module.cart.services;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.ecommerce.app.module.cart.model.CartItem;
import com.ecommerce.app.module.order.services.CheckoutRequestIdentityService;
import com.ecommerce.app.product.model.Product;
import com.ecommerce.app.product.model.ProductStatusEnum;
import com.ecommerce.app.product.ripository.ProductDimensionRepository;
import com.ecommerce.app.product.ripository.ProductRepository;
import com.ecommerce.app.product.ripository.ProductVariantRepository;
import com.ecommerce.app.product.services.ProductService;
import com.ecommerce.app.product.services.ProductVariantCatalogService;
import com.ecommerce.app.product.services.StockLedgerService;
import com.ecommerce.app.vendor.model.VendorStatusEnum;
import com.ecommerce.app.vendor.model.Vendorprofile;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

class CartServiceAvailabilityModeTest {

    @Test
    void preorderModeCanBeAddedEvenWhenAvailableStockIsZero() {
        CartService service = serviceWithStock(BigDecimal.ZERO);
        Product product = activeProduct();
        product.setAvailabilityMode(com.ecommerce.app.product.model.ProductAvailabilityMode.PREORDER);
        when(service.productRepository.findByUuid("product-1")).thenReturn(Optional.of(product));

        MockHttpSession session = new MockHttpSession();

        assertTrue(service.addToCart("product-1", null, BigDecimal.ONE, session));
        CartItem item = ((List<CartItem>) session.getAttribute("sessioncart")).get(0);
        assertTrue(item.getPreorder());
    }

    @Test
    void managedStockModeRejectsQuantityBeyondAvailableStock() {
        CartService service = serviceWithStock(BigDecimal.ZERO);
        Product product = activeProduct();
        product.setAvailabilityMode(com.ecommerce.app.product.model.ProductAvailabilityMode.STOCK_MANAGED);
        when(service.productRepository.findByUuid("product-1")).thenReturn(Optional.of(product));

        assertFalse(service.addToCart("product-1", null, BigDecimal.ONE, new MockHttpSession()));
    }

    @Test
    void invalidLegacyModeIsNotAcceptedIntoCart() {
        CartService service = serviceWithStock(BigDecimal.ZERO);
        Product product = activeProduct();
        product.setManageStock(true);
        product.setAllowPreorder(true);
        when(service.productRepository.findByUuid("product-1")).thenReturn(Optional.of(product));

        assertFalse(service.addToCart("product-1", null, BigDecimal.ONE, new MockHttpSession()));
    }

    private CartService serviceWithStock(BigDecimal availableStock) {
        CartService service = new CartService();
        service.productRepository = mock(ProductRepository.class);
        service.productVariantRepository = mock(ProductVariantRepository.class);
        service.productDimensionRepository = mock(ProductDimensionRepository.class);
        service.productService = mock(ProductService.class);
        service.productVariantCatalogService = mock(ProductVariantCatalogService.class);
        service.stockLedgerService = mock(StockLedgerService.class);
        service.checkoutRequestIdentityService = mock(CheckoutRequestIdentityService.class);
        when(service.stockLedgerService.getAvailableQuantity(1L, null)).thenReturn(availableStock);
        when(service.productService.totalDiscountPercentCalculate(any(), any())).thenReturn(BigDecimal.ZERO);
        return service;
    }

    private Product activeProduct() {
        Vendorprofile vendor = new Vendorprofile();
        vendor.setId(7L);
        vendor.setUuid("vendor-1");
        vendor.setVendorStatusEnum(VendorStatusEnum.Active);

        Product product = new Product();
        product.setId(1L);
        product.setUuid("product-1");
        product.setVendorprofile(vendor);
        product.setStatus(ProductStatusEnum.Active);
        product.setOnlineShow(true);
        product.setManageProductVariants(false);
        product.setSalesPrice(new BigDecimal("10.00"));
        product.setVatRate(BigDecimal.ZERO);
        product.setMarketPlaceCommissionRate(BigDecimal.ZERO);
        return product;
    }
}
