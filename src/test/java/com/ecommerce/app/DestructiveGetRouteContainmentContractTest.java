package com.ecommerce.app;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;

class DestructiveGetRouteContainmentContractTest {

    @Test
    void targetedDestructiveEndpointsArePostOnlyInControllers() throws IOException {
        Map<String, String[]> controllerContracts = Map.of(
                "src/main/java/com/ecommerce/app/adminvendor/controller/AdminVendorController.java",
                new String[]{"@PostMapping(\"/delete/{uuid}\")"},
                "src/main/java/com/ecommerce/app/adminvendor/controller/AdminVendorPayoutMethodController.java",
                new String[]{"@PostMapping(\"/delete/{id}\")"},
                "src/main/java/com/ecommerce/app/module/ads/controller/AdsController.java",
                new String[]{"@PostMapping(\"/delete/{id}\")"},
                "src/main/java/com/ecommerce/app/product/controller/CatalogAttributeAdminController.java",
                new String[]{
                        "@PostMapping(\"/delete/{uuid}\")",
                        "@PostMapping(\"/options/delete/{uuid}\")",
                        "@PostMapping(\"/category-mappings/delete/{uuid}\")"
                },
                "src/main/java/com/ecommerce/app/product/controller/ManufacturerController.java",
                new String[]{"@PostMapping(\"/delete/{id}\")"},
                "src/main/java/com/ecommerce/app/product/controller/UnitsController.java",
                new String[]{"@PostMapping(\"/delete/{id}\")"}
        );

        for (Map.Entry<String, String[]> entry : controllerContracts.entrySet()) {
            String source = read(entry.getKey());
            assertFalse(source.contains("@GetMapping(\"/delete"),
                    entry.getKey() + " must not expose destructive delete through GET");
            for (String expectedMapping : entry.getValue()) {
                assertTrue(source.contains(expectedMapping),
                        entry.getKey() + " must expose " + expectedMapping);
            }
        }
    }

    @Test
    void targetedTemplatesUsePostFormsWithCsrfForDestructiveActions() throws IOException {
        Map<String, String[]> templateContracts = Map.of(
                "src/main/resources/templates/admin/vendor/admin_vendor_list.html",
                new String[]{"th:action=\"@{/adminvendor/delete/{uuid}(uuid=${vendor.uuid})}\""},
                "src/main/resources/templates/vendor/payoutmethod/list.html",
                new String[]{"th:action=\"@{/vendor-payout-methods/delete/{id}(id=${m.id})}\""},
                "src/main/resources/templates/ads/ads_list.html",
                new String[]{"th:action=\"@{/admin/ads/delete/{id}(id=${ad.id})}\""},
                "src/main/resources/templates/product/attribute/list.html",
                new String[]{"th:action=\"@{/catalog-attributes/delete/{uuid}(uuid=${attribute.uuid})}\""},
                "src/main/resources/templates/product/attribute/options.html",
                new String[]{"th:action=\"@{/catalog-attributes/options/delete/{uuid}(uuid=${option.uuid})}\""},
                "src/main/resources/templates/product/attribute/category_mappings.html",
                new String[]{"th:action=\"@{/catalog-attributes/category-mappings/delete/{uuid}(uuid=${mapping.uuid})}\""},
                "src/main/resources/templates/product/manufacturer/list.html",
                new String[]{"th:action=\"@{/manufacturer/delete/{id}(id=${lists.id})}\""},
                "src/main/resources/templates/product/unit/list.html",
                new String[]{"th:action=\"@{/uom/delete/{id}(id=${lists.id})}\""}
        );

        for (Map.Entry<String, String[]> entry : templateContracts.entrySet()) {
            String template = read(entry.getKey());
            assertTrue(template.contains("method=\"post\""),
                    entry.getKey() + " must submit destructive actions with POST");
            assertTrue(template.contains("th:name=\"${_csrf.parameterName}\""),
                    entry.getKey() + " must include a CSRF hidden field");
            for (String expectedAction : entry.getValue()) {
                assertTrue(template.contains(expectedAction),
                        entry.getKey() + " must include POST form " + expectedAction);
            }
        }
    }

    @Test
    void targetedTemplatesDoNotRenderDeleteHrefLinks() throws IOException {
        Map<String, String[]> forbiddenHrefPatterns = Map.of(
                "src/main/resources/templates/admin/vendor/admin_vendor_list.html",
                new String[]{"th:href=\"@{/adminvendor/delete"},
                "src/main/resources/templates/vendor/payoutmethod/list.html",
                new String[]{"th:href=\"@{/vendor-payout-methods/delete"},
                "src/main/resources/templates/ads/ads_list.html",
                new String[]{"th:href=\"@{|/admin/ads/delete", "th:href=\"@{/admin/ads/delete"},
                "src/main/resources/templates/product/attribute/list.html",
                new String[]{"th:href=\"@{/catalog-attributes/delete"},
                "src/main/resources/templates/product/attribute/options.html",
                new String[]{"th:href=\"@{/catalog-attributes/options/delete"},
                "src/main/resources/templates/product/attribute/category_mappings.html",
                new String[]{"th:href=\"@{/catalog-attributes/category-mappings/delete"},
                "src/main/resources/templates/product/manufacturer/list.html",
                new String[]{"th:href=\"@{/manufacturer/delete"},
                "src/main/resources/templates/product/unit/list.html",
                new String[]{"th:href=\"@{/uom/delete"}
        );

        for (Map.Entry<String, String[]> entry : forbiddenHrefPatterns.entrySet()) {
            String template = read(entry.getKey());
            for (String forbiddenHref : entry.getValue()) {
                assertFalse(template.contains(forbiddenHref),
                        entry.getKey() + " must not expose " + forbiddenHref);
            }
        }
    }

    private static String read(String path) throws IOException {
        return Files.readString(Path.of(path));
    }
}
