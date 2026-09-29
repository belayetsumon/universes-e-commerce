package com.ecommerce.app;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ExplicitMutationRouteContainmentContractTest {

    @Test
    void mutationHandlersUseExplicitPostMappings() throws IOException {
        Map<String, String[]> contracts = Map.of(
                "src/main/java/com/ecommerce/app/product/controller/ProductController.java",
                new String[]{"@PostMapping(\"/delete/{id}\")"},
                "src/main/java/com/ecommerce/app/product/controller/ProductcategoryController.java",
                new String[]{"@PostMapping(\"/save\")", "@PostMapping(\"/delete/{id}\")"},
                "src/main/java/com/ecommerce/app/vendor/controller/VendorProductController.java",
                new String[]{"@PostMapping(\"/save\")", "@PostMapping(\"/delete/{id}\")"},
                "src/main/java/com/ecommerce/app/vendor/controller/VendorProfileController.java",
                new String[]{"@PostMapping(\"/save\")"},
                "src/main/java/com/ecommerce/app/module/customer/controller/CustomerController.java",
                new String[]{"@PostMapping(\"/save\")"},
                "src/main/java/com/ecommerce/app/vendor/controller/VendorPayoutController.java",
                new String[]{"@PostMapping(\"/save\")"},
                "src/main/java/com/ecommerce/app/publics/controller/PublicController.java",
                new String[]{"@PostMapping(\"/home-contact-save\")"}
        );

        for (Map.Entry<String, String[]> entry : contracts.entrySet()) {
            String source = read(entry.getKey());
            for (String mapping : entry.getValue()) {
                assertTrue(source.contains(mapping), entry.getKey() + " must expose " + mapping);
            }
            assertFalse(source.contains("@RequestMapping(\"/save\")"),
                    entry.getKey() + " must not leave a mutation save handler unrestricted");
            assertFalse(source.contains("@RequestMapping(\"/delete/"),
                    entry.getKey() + " must not leave a destructive handler unrestricted");
        }
    }

    @Test
    void browserPageHandlersUseExplicitGetMappings() throws IOException {
        Map<String, String[]> contracts = Map.ofEntries(
                Map.entry("src/main/java/com/ecommerce/app/product/controller/ProductController.java",
                        new String[]{"@GetMapping(value = {\"\", \"/\", \"/index\"})", "@GetMapping(\"/create\")",
                            "@GetMapping(\"/details/{id}\")", "@GetMapping(\"/edit/{id}\")"}),
                Map.entry("src/main/java/com/ecommerce/app/product/controller/ProductcategoryController.java",
                        new String[]{"@GetMapping(value = {\"\", \"/\", \"/index\"})", "@GetMapping(\"/create\")",
                            "@GetMapping(\"/details/{id}\")", "@GetMapping(\"/edit/{id}\")"}),
                Map.entry("src/main/java/com/ecommerce/app/vendor/controller/VendorProductController.java",
                        new String[]{"@GetMapping(value = {\"\", \"/\", \"/index\"})", "@GetMapping(\"/create\")",
                            "@GetMapping(\"/details/{id}\")", "@GetMapping(\"/edit/{id}\")"}),
                Map.entry("src/main/java/com/ecommerce/app/vendor/controller/VendorProfileController.java",
                        new String[]{"@GetMapping(value = {\"\", \"/\", \"/index\"})", "@GetMapping(value = {\"/create\"})",
                            "@GetMapping(\"/edit\")", "@GetMapping(\"/details\")"}),
                Map.entry("src/main/java/com/ecommerce/app/product/controller/ProductImageController.java",
                        new String[]{"@GetMapping(\"/list/{id}\")"}),
                Map.entry("src/main/java/com/ecommerce/app/vendor/controller/Vendor_ProductImageController.java",
                        new String[]{"@GetMapping(\"/list/{id}\")"}),
                Map.entry("src/main/java/com/ecommerce/app/module/customer/controller/CustomerController.java",
                        new String[]{"@GetMapping(value = {\"\", \"/\", \"/index\", \"dashboards\"})", "@GetMapping(value = {\"/create\"})",
                            "@GetMapping(value = {\"/storelist\"})"}),
                Map.entry("src/main/java/com/ecommerce/app/module/customer/controller/CustomerOrderController.java",
                        new String[]{"@GetMapping(value = {\"\", \"/\", \"/index\"})", "@GetMapping(value = {\"/payment/{orderid}\"})",
                            "@GetMapping(value = {\"/payment_success/{orderid}\"})", "@GetMapping(value = {\"/payment_failed/{orderid}\"})",
                            "@GetMapping(value = {\"/payment_cancelled/{orderid}\"})"}),
                Map.entry("src/main/java/com/ecommerce/app/admincustomer/controller/AdminCustomerController.java",
                        new String[]{"@GetMapping(value = {\"\", \"/\", \"/index\"})", "@GetMapping(\"/orderlist\")",
                            "@GetMapping(\"/order-by-customer/{cid}\")", "@GetMapping(\"/order-details/{oid}\")"}),
                Map.entry("src/main/java/com/ecommerce/app/module/cart/controller/CartController.java",
                        new String[]{"@GetMapping(value = {\"\", \"/\", \"/index\"})", "@GetMapping(\"/shipping\")",
                            "@GetMapping(\"/payment\")"}),
                Map.entry("src/main/java/com/ecommerce/app/module/order/controller/SalesOrderController.java",
                        new String[]{"@GetMapping(value = {\"\", \"/\", \"/index\"})"})
        );

        for (Map.Entry<String, String[]> entry : contracts.entrySet()) {
            String source = read(entry.getKey());
            for (String mapping : entry.getValue()) {
                assertTrue(source.contains(mapping), entry.getKey() + " must expose " + mapping);
            }
        }
    }

    @Test
    void remainingDisplayHandlersUseExplicitGetMappings() throws IOException {
        Map<String, String[]> contracts = Map.ofEntries(
                Map.entry("src/main/java/com/ecommerce/app/admin/controller/AdminController.java",
                        new String[]{"@GetMapping(value = {\"\", \"/\", \"/index\"})",
                            "@GetMapping(value = {\"/returns-refunds\", \"/return-refund\", \"/return-refunds\", \"/returns\"})",
                            "@GetMapping(value = {\"/refunds\"})"}),
                Map.entry("src/main/java/com/ecommerce/app/module/customer/controller/BillingAddressController.java",
                        new String[]{"@GetMapping(value = {\"\", \"/\", \"/index\", \"dashboards\"})"}),
                Map.entry("src/main/java/com/ecommerce/app/module/customer/controller/CustomerBonusController.java",
                        new String[]{"@GetMapping(value = {\"\", \"/\", \"/index\", \"dashboards\"})"}),
                Map.entry("src/main/java/com/ecommerce/app/module/customer/controller/CustomerPaymentController.java",
                        new String[]{"@GetMapping(\"/payment_method\")"}),
                Map.entry("src/main/java/com/ecommerce/app/module/customer/controller/CustomerProductController.java",
                        new String[]{"@GetMapping(value = {\"\", \"/\", \"/index\"})", "@GetMapping(\"/details/{id}\")",
                            "@GetMapping(\"/question-by-exam/{examid}\")"}),
                Map.entry("src/main/java/com/ecommerce/app/module/customer/controller/CustomerTeamController.java",
                        new String[]{"@GetMapping(value = {\"\", \"/\", \"/index\"})"}),
                Map.entry("src/main/java/com/ecommerce/app/module/customer/controller/CustomerTransactionController.java",
                        new String[]{"@GetMapping(value = {\"\", \"/\", \"/index\"})"}),
                Map.entry("src/main/java/com/ecommerce/app/module/customer/ReferralRewards/controller/CustomerWalletTransactionController.java",
                        new String[]{"@GetMapping(\"/list\")"}),
                Map.entry("src/main/java/com/ecommerce/app/module/customer/ReferralRewards/controller/ReferralCustomerController.java",
                        new String[]{"@GetMapping(\"/list\")"}),
                Map.entry("src/main/java/com/ecommerce/app/module/customer/ReferralRewards/controller/ReferralRewardsController.java",
                        new String[]{"@GetMapping(\"/dashbords\")"}),
                Map.entry("src/main/java/com/ecommerce/app/module/customer/ReferralRewards/controller/RewardHistoryCustomerController.java",
                        new String[]{"@GetMapping(\"/rewards\")"}),
                Map.entry("src/main/java/com/ecommerce/app/module/ReferralRewards/controller/MultiLavelRateSettingsController.java",
                        new String[]{"@GetMapping(\"/list\")"}),
                Map.entry("src/main/java/com/ecommerce/app/module/ReferralRewards/controller/ReferralRewardController.java",
                        new String[]{"@GetMapping(\"/referral-reward-list\")", "@GetMapping(\"/rewards-history\")"}),
                Map.entry("src/main/java/com/ecommerce/app/module/ReferralRewards/controller/RewardRedemptionController.java",
                        new String[]{"@GetMapping(\"/list\")"}),
                Map.entry("src/main/java/com/ecommerce/app/publics/controller/PublicController.java",
                        new String[]{"@GetMapping(\"/about-us\")", "@GetMapping(\"/member-login\")",
                            "@GetMapping(\"/product\")", "@GetMapping(\"/search/suggestions\")",
                            "@GetMapping(\"/product-by-category/{prodcatid}\")", "@GetMapping(\"/single-product/{prodid}\")",
                            "@GetMapping(\"/browsing-history\")", "@GetMapping(\"/blogdetails/{blogid}\")",
                            "@GetMapping(\"/blog-by-cat/{catid}\")", "@GetMapping({\"/contactUs\", \"/contact-us\"})",
                            "@GetMapping({\"/privacy_policy\", \"/privacy-policy\"})", "@GetMapping(\"/help\")",
                            "@GetMapping(\"/terms-of-use\")", "@GetMapping(\"/payment-methods\")",
                            "@GetMapping(\"/returns-replacements\")",
                            "@GetMapping({\"/refund-returns-policy\", \"/refund-and-returns-policy\"})",
                            "@GetMapping(\"/shipping-rates-policies\")",
                            "@GetMapping({\"/term-and-conditions\", \"/terms-and-conditions\"})"}),
                Map.entry("src/main/java/com/ecommerce/app/publics/controller/WelcomeController.java",
                        new String[]{"@GetMapping(\"/\")"}),
                Map.entry("src/main/java/com/ecommerce/app/vendor/controller/VendorController.java",
                        new String[]{"@GetMapping(value = {\"/home\"})",
                            "@GetMapping(value = {\"/{id}\", \"/{id}\", \"/index/{id}\", \"/dashboards/{id}\"})"}),
                Map.entry("src/main/java/com/ecommerce/app/vendor/controller/VendoraddressController.java",
                        new String[]{"@GetMapping(value = {\"\", \"/\", \"/index\"})"}),
                Map.entry("src/main/java/com/ecommerce/app/vendor/controller/VendorLogoController.java",
                        new String[]{"@GetMapping(value = {\"\", \"/\", \"/index\"})"}),
                Map.entry("src/main/java/com/ecommerce/app/vendor/controller/VendorPayoutController.java",
                        new String[]{"@GetMapping(\"/request\")"}),
                Map.entry("src/main/java/com/ecommerce/app/vendor/controller/VendorSalesOrderController.java",
                        new String[]{"@GetMapping(value = {\"\", \"/\", \"/index\"})", "@GetMapping(value = {\"/details/{oid}\"})"}),
                Map.entry("src/main/java/com/ecommerce/app/vendor/controller/VendorTransactionController.java",
                        new String[]{"@GetMapping(\"/list\")"})
        );

        for (Map.Entry<String, String[]> entry : contracts.entrySet()) {
            String source = read(entry.getKey());
            for (String mapping : entry.getValue()) {
                assertTrue(source.contains(mapping), entry.getKey() + " must expose " + mapping);
            }
        }
    }

    @Test
    void mutationFormsIncludePostAndCsrf() throws IOException {
        Map<String, String> forms = Map.of(
                "src/main/resources/templates/product/product_details.html", "/product/delete/",
                "src/main/resources/templates/product/productcategory/add.html", "/productcategory/save",
                "src/main/resources/templates/product/productcategory/productcategory_details.html", "/productcategory/delete/",
                "src/main/resources/templates/vendor/product/add.html", "/productvendor/save",
                "src/main/resources/templates/vendor/product/product_details.html", "/productvendor/delete/",
                "src/main/resources/templates/vendor/profile/vendor_profile_create.html", "/vendorprofile/save",
                "src/main/resources/templates/customer/vendor_profile_create.html", "/customer/save",
                "src/main/resources/templates/vendor/payout/payout_form.html", "/vendor-payout/save",
                "src/main/resources/templates/frontview/contactUs.html", "/public/home-contact-save"
        );

        for (Map.Entry<String, String> entry : forms.entrySet()) {
            String template = read(entry.getKey());
            assertTrue(template.contains("method=\"post\""), entry.getKey() + " must submit with POST");
            assertTrue(template.contains("th:name=\"${_csrf.parameterName}\""),
                    entry.getKey() + " must include a CSRF hidden field");
            assertTrue(template.contains(entry.getValue()), entry.getKey() + " must include " + entry.getValue());
        }
    }

    @Test
    void categoryDeleteTemplatesDoNotRenderGetLinks() throws IOException {
        for (String path : new String[]{
            "src/main/resources/templates/product/productcategory/productcategory_details.html",
            "src/main/resources/templates/product/manufacturer/manufacturer_details.html"
        }) {
            assertFalse(read(path).contains("th:href=\"@{/productcategory/delete"),
                    path + " must not render a destructive GET link");
        }
    }

    private static String read(String path) throws IOException {
        return Files.readString(Path.of(path));
    }
}
