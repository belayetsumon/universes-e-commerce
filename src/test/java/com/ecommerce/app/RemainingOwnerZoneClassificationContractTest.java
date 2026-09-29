package com.ecommerce.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class RemainingOwnerZoneClassificationContractTest {

    @Test
    void previouslyUnclassifiedRoutesHaveExplicitZonesAndCapabilities() throws IOException {
        List<Map<String, String>> rows = Files.readAllLines(Path.of("src/docs/application-security-endpoint-decision-ledger.csv"))
                .stream()
                .skip(1)
                .map(RemainingOwnerZoneClassificationContractTest::parseCsvRow)
                .toList();

        assertDecision(rows, "/order", "PLATFORM_ADMIN", "platform.order.payment.read");
        assertDecision(rows, "/order/", "PLATFORM_ADMIN", "platform.order.payment.read");
        assertDecision(rows, "/order/index", "PLATFORM_ADMIN", "platform.order.payment.read");
        assertDecision(rows, "/users/login", "PUBLIC", "public.identity.access.read");
        assertDecision(rows, "/users/profile", "CUSTOMER", "customer.identity.access.read");
        assertDecision(rows, "/users/view/{uid}", "PLATFORM_ADMIN", "platform.identity.access.read");
    }

    @Test
    void marketplaceOrderListIsProtectedByNamedPlatformCapability() throws IOException {
        String securityConfig = Files.readString(Path.of("src/main/java/com/ecommerce/app/SecurityConfig.java"));
        String controller = Files.readString(Path.of("src/main/java/com/ecommerce/app/module/order/controller/SalesOrderController.java"));

        assertTrue(securityConfig.contains("requestMatchers(\"/order\", \"/order/\", \"/order/index\").hasAnyAuthority("));
        assertTrue(securityConfig.contains("platform.order.payment.read"));
        assertTrue(controller.contains("@PreAuthorize(PlatformOrderPermissions.CAN_READ)"));
    }

    private static void assertDecision(List<Map<String, String>> rows, String path, String zone, String capability) {
        List<Map<String, String>> matches = rows.stream()
                .filter(row -> path.equals(row.get("Path")))
                .toList();
        assertEquals(1, matches.size(), path + " must have exactly one ledger row");
        Map<String, String> row = matches.get(0);
        assertEquals(zone, row.get("FinalZone"), path + " final zone");
        assertEquals(capability, row.get("FinalCapability"), path + " capability");
        assertEquals("APPROVED", row.get("DecisionStatus"), path + " decision status");
    }

    private static Map<String, String> parseCsvRow(String line) {
        String[] values = parseValues(line);
        String[] headers = {
            "DecisionSchemaVersion", "DecisionKey", "SourceCandidateFingerprint", "SourceInventorySha256",
            "SourceFile", "SourceLine", "Package", "Controller", "JavaMethod", "SourceHttpMethod", "Path",
            "CurrentUrlRule", "CurrentMethodGuard", "TargetZoneCandidate", "ModuleCandidate", "ActionCandidate",
            "PermissionCandidate", "RequiredScopeCandidate", "FinalZone", "FinalHttpMethod", "RequiredAuthenticationMechanism",
            "FinalCapability", "OwnershipScopeRule", "RepositoryScopeMethod", "CsrfSignatureReplayRule", "RateLimitPolicy",
            "IdempotencyPolicy", "AuditEventRequirement", "TestIds", "DecisionStatus", "ImplementationEvidenceStatus",
            "DecisionRationale", "EvidenceReferences", "DecisionRevision", "Reviewer", "ReviewedAtUtc"
        };
        java.util.LinkedHashMap<String, String> row = new java.util.LinkedHashMap<>();
        for (int i = 0; i < headers.length && i < values.length; i++) {
            row.put(headers[i], values[i].replaceAll("^\"|\"$", "").replace("\"\"", "\""));
        }
        return row;
    }

    private static String[] parseValues(String line) {
        java.util.ArrayList<String> values = new java.util.ArrayList<>();
        StringBuilder value = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char current = line.charAt(i);
            if (current == '\"') {
                if (quoted && i + 1 < line.length() && line.charAt(i + 1) == '\"') {
                    value.append('\"');
                    i++;
                } else {
                    quoted = !quoted;
                }
            } else if (current == ',' && !quoted) {
                values.add(value.toString());
                value.setLength(0);
            } else {
                value.append(current);
            }
        }
        values.add(value.toString());
        return values.toArray(String[]::new);
    }
}
