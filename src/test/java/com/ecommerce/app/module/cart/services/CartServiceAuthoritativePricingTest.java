package com.ecommerce.app.module.cart.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ecommerce.app.module.cart.model.CartItem;
import com.ecommerce.app.module.order.services.CheckoutRequestIdentityService;
import com.ecommerce.app.product.model.Product;
import com.ecommerce.app.product.model.ProductStatusEnum;
import com.ecommerce.app.product.ripository.ProductDimensionRepository;
import com.ecommerce.app.product.ripository.ProductRepository;
import com.ecommerce.app.product.ripository.ProductVariantRepository;
import com.ecommerce.app.product.services.ProductService;
import com.ecommerce.app.vendor.model.VendorStatusEnum;
import com.ecommerce.app.vendor.model.Vendorprofile;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

class CartServiceAuthoritativePricingTest {

    @Test
    void checkoutCartIsRepricedFromCurrentCatalogueAndRequestIdentityRotates() {
        CartService service = new CartService();
        service.productRepository = mock(ProductRepository.class);
        service.productVariantRepository = mock(ProductVariantRepository.class);
        service.productDimensionRepository = mock(ProductDimensionRepository.class);
        service.productService = mock(ProductService.class);
        service.checkoutRequestIdentityService = mock(CheckoutRequestIdentityService.class);

        Vendorprofile vendor = new Vendorprofile();
        vendor.setId(7L);
        vendor.setUuid("vendor-7");
        vendor.setVendorStatusEnum(VendorStatusEnum.Active);
        Product currentProduct = new Product();
        currentProduct.setId(11L);
        currentProduct.setUuid("product-11");
        currentProduct.setVendorprofile(vendor);
        currentProduct.setStatus(ProductStatusEnum.Active);
        currentProduct.setOnlineShow(true);
        currentProduct.setManageProductVariants(false);
        currentProduct.setSalesPrice(new BigDecimal("25.00"));
        currentProduct.setVatRate(BigDecimal.ZERO);
        currentProduct.setMarketPlaceCommissionRate(new BigDecimal("20.00"));
        when(service.productRepository.findByUuid("product-11")).thenReturn(Optional.of(currentProduct));
        when(service.productService.totalDiscountPercentCalculate(any(), any())).thenReturn(BigDecimal.ZERO);

        CartItem staleLine = new CartItem();
        staleLine.setProduct(currentProduct);
        staleLine.setProductId(11L);
        staleLine.setProductUuid("product-11");
        staleLine.setQuantity(new BigDecimal("2.00"));
        staleLine.setSalesPrice(new BigDecimal("10.00"));
        staleLine.setDiscountRate(BigDecimal.ZERO);
        staleLine.setDiscountAmount(BigDecimal.ZERO);
        staleLine.setMarketPlaceCommissionRate(new BigDecimal("20.00"));
        staleLine.setMarketPlaceCommissionAmount(new BigDecimal("4.00"));
        staleLine.setVendorAmount(new BigDecimal("16.00"));
        staleLine.setVatRate(BigDecimal.ZERO);
        staleLine.setVatAmount(BigDecimal.ZERO);
        staleLine.setWeight(BigDecimal.ZERO);
        staleLine.setItemTotal(new BigDecimal("20.00"));
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("sessioncart", new ArrayList<>(List.of(staleLine)));

        List<CartItem> sanitized = service.sanitizeSessionCart(session);

        assertEquals(1, sanitized.size());
        assertEquals(new BigDecimal("25.00"), sanitized.get(0).getSalesPrice());
        assertEquals(new BigDecimal("50.00"), sanitized.get(0).getItemTotal());
        assertEquals(new BigDecimal("40.00"), sanitized.get(0).getVendorAmount());
        verify(service.checkoutRequestIdentityService).clearCurrentRequestId(session);
    }
}
