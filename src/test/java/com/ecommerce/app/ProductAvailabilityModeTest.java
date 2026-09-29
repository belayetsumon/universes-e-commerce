package com.ecommerce.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ecommerce.app.product.model.Product;
import com.ecommerce.app.product.model.ProductAvailabilityMode;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ProductAvailabilityModeTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        validatorFactory.close();
    }

    @Test
    void newProductDefaultsToManagedStock() {
        Product product = new Product();

        assertEquals(ProductAvailabilityMode.STOCK_MANAGED, product.getAvailabilityMode());
        assertTrue(product.getManageStock());
        assertFalse(product.getAllowPreorder());
        assertTrue(validator.validateProperty(product, "availabilityModeValid").isEmpty());
    }

    @Test
    void selectingPreorderDisablesStockManagement() {
        Product product = new Product();

        product.setAvailabilityMode(ProductAvailabilityMode.PREORDER);

        assertFalse(product.getManageStock());
        assertTrue(product.getAllowPreorder());
        assertTrue(product.usesPreorder());
        assertTrue(validator.validateProperty(product, "availabilityModeValid").isEmpty());
    }

    @Test
    void invalidLegacyCombinationsAreRejectedUntilMigrated() {
        Product product = new Product();
        product.setManageStock(true);
        product.setAllowPreorder(true);

        assertFalse(product.isAvailabilityModeValid());
        assertFalse(validator.validateProperty(product, "availabilityModeValid").isEmpty());

        product.setManageStock(false);
        product.setAllowPreorder(false);

        assertFalse(product.isAvailabilityModeValid());
        assertFalse(validator.validateProperty(product, "availabilityModeValid").isEmpty());
    }

    @Test
    void purchasePriceCannotExceedSalesPrice() {
        Product product = new Product();
        product.setPurchasePrice(new BigDecimal("100.00"));
        product.setSalesPrice(new BigDecimal("99.99"));

        assertFalse(product.isSalesPriceAtLeastPurchasePrice());
        assertFalse(validator.validateProperty(product, "salesPriceAtLeastPurchasePrice").isEmpty());

        product.setSalesPrice(new BigDecimal("100.00"));
        assertTrue(product.isSalesPriceAtLeastPurchasePrice());
        assertTrue(validator.validateProperty(product, "salesPriceAtLeastPurchasePrice").isEmpty());

        product.setSalesPrice(new BigDecimal("125.00"));
        assertTrue(product.isSalesPriceAtLeastPurchasePrice());
        assertTrue(validator.validateProperty(product, "salesPriceAtLeastPurchasePrice").isEmpty());
    }

    @Test
    void templatesExposeOneRequiredAvailabilityChoiceInsteadOfIndependentSwitches() throws IOException {
        String admin = read("src/main/resources/templates/product/add.html");
        String vendor = read("src/main/resources/templates/vendor/product/add.html");

        for (String template : new String[]{admin, vendor}) {
            assertTrue(template.contains("name=\"availabilityMode\""));
            assertTrue(template.contains("value=\"STOCK_MANAGED\""));
            assertTrue(template.contains("value=\"PREORDER\""));
            assertTrue(template.contains("required"));
            assertTrue(template.contains("availabilityModeValid"));
            assertFalse(template.contains("id=\"manageStock\""));
            assertFalse(template.contains("id=\"allowPreorder\""));
        }
    }

    @Test
    void migrationRepairsLegacyRowsAndAddsDatabaseInvariant() throws IOException {
        String migration = read("src/main/resources/db/migration/mysql/V202608270002__product_availability_mode_invariant.sql");

        assertTrue(migration.contains("UPDATE product"));
        assertTrue(migration.contains("ck_product_availability_mode"));
        assertTrue(migration.contains("MODIFY manage_stock BOOLEAN NOT NULL DEFAULT TRUE"));
        assertTrue(migration.contains("MODIFY allow_preorder BOOLEAN NOT NULL DEFAULT FALSE"));
        assertTrue(migration.contains("allow_preorder = CASE WHEN allow_preorder = 1 THEN 1 ELSE 0 END"));
    }

    private String read(String path) throws IOException {
        return Files.readString(Path.of(path));
    }
}
