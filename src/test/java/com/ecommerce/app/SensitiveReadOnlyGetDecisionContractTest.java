package com.ecommerce.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class SensitiveReadOnlyGetDecisionContractTest {

    private static final Path ENDPOINT_DECISION_LEDGER =
            Path.of("src/docs/application-security-endpoint-decision-ledger.csv");

    private static final Set<String> REVIEWED_READ_ONLY_GET_PATHS = Set.of(
            "/admin/refunds",
            "/admin/return-refund",
            "/admin/return-refunds",
            "/admin/returns",
            "/admin/returns-refunds",
            "/admin-customer/orders/{id}/pdf",
            "/admin-customer/pdf/{id}",
            "/admin/finance/refunds",
            "/admin/finance/return-refund",
            "/admin/finance/return-refunds",
            "/admin/finance/returns",
            "/admin/finance/returns-refunds",
            "/admin/blog/export",
            "/customerorder/orders/{id}/pdf",
            "/customerorder/refunds",
            "/forgotpassword",
            "/forgotpassword/",
            "/forgotpassword/index",
            "/forgotpassword/userforgotpassword",
            "/users/change-password",
            "/users/change-password/{id}",
            "/admin/stock/current/pdf",
            "/catalog-variants/generate/{productUuid}",
            "/public/forgot-password",
            "/public/refund-and-returns-policy",
            "/public/refund-returns-policy",
            "/vendor-payout/request",
            "/vendor-order/orders/{id}/pdf",
            "/vendor-order/refunds",
            "/vendorverifications/verify-mobile"
    );

    @Test
    void reviewedSensitiveGetRowsAreApprovedAsReadOnlyExceptions() throws IOException {
        Map<String, Map<String, String>> rowsByPath = rowsByPath();

        for (String path : REVIEWED_READ_ONLY_GET_PATHS) {
            Map<String, String> row = rowsByPath.get(path);
            assertNotNull(row, path + " must remain in the endpoint decision ledger");
            assertEquals("GET", row.get("SourceHttpMethod"), path + " must stay an exact GET source mapping");
            assertEquals("GET", row.get("FinalHttpMethod"), path + " must stay an exact GET final method");
            assertEquals("SENSITIVE_REVIEW", row.get("ActionCandidate"), path + " must stay visibly sensitive");
            assertEquals("APPROVED", row.get("DecisionStatus"), path + " must be approved only after read-only review");
            assertTrue(row.get("DecisionRationale").startsWith("APPROVED: Source review confirms this sensitive GET is a read-only"),
                    path + " must document why the sensitive GET is safe to approve");
            assertTrue(row.get("TestIds").contains("SensitiveReadOnlyGetDecisionContractTest"),
                    path + " must carry the sensitive GET decision contract evidence");
        }
    }

    @Test
    void noSensitiveGetRowsRemainDeferred() throws IOException {
        Set<String> deferredSensitiveGetPaths = readCsv(ENDPOINT_DECISION_LEDGER).stream()
                .filter(row -> "GET".equals(row.get("SourceHttpMethod")))
                .filter(row -> "SENSITIVE_REVIEW".equals(row.get("ActionCandidate")))
                .filter(row -> "DEFERRED".equals(row.get("DecisionStatus")))
                .map(row -> row.get("Path"))
                .collect(Collectors.toSet());

        assertTrue(deferredSensitiveGetPaths.isEmpty(),
                "No sensitive GET route should remain deferred after action-style GET containment: " + deferredSensitiveGetPaths);
    }

    @Test
    void vendorEmailVerificationGetIsImplementedAsConfirmationStep() throws IOException {
        Map<String, String> row = rowsByPathAndMethod().get("/vendorverifications/verify-email GET");

        assertNotNull(row, "vendor email verification GET must remain in the endpoint decision ledger");
        assertEquals("GET", row.get("FinalHttpMethod"));
        assertEquals("SENSITIVE_REVIEW", row.get("ActionCandidate"));
        assertEquals("IMPLEMENTED", row.get("DecisionStatus"));
        assertTrue(row.get("DecisionRationale").contains("token consumption moved off GET"),
                "vendor email verification GET must document the confirmation-only containment");
    }

    private static Map<String, Map<String, String>> rowsByPath() throws IOException {
        return readCsv(ENDPOINT_DECISION_LEDGER).stream()
                .collect(Collectors.toMap(
                        row -> row.get("Path"),
                        row -> row,
                        (first, duplicate) -> first,
                        LinkedHashMap::new
                ));
    }

    private static Map<String, Map<String, String>> rowsByPathAndMethod() throws IOException {
        return readCsv(ENDPOINT_DECISION_LEDGER).stream()
                .collect(Collectors.toMap(
                        row -> row.get("Path") + " " + row.get("SourceHttpMethod"),
                        row -> row,
                        (first, duplicate) -> first,
                        LinkedHashMap::new
                ));
    }

    private static List<Map<String, String>> readCsv(Path path) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String headerLine = reader.readLine();
            assertNotNull(headerLine, path + " must include a header row");
            List<String> headers = parseCsvLine(headerLine);
            List<Map<String, String>> rows = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                List<String> values = parseCsvLine(line);
                assertEquals(headers.size(), values.size(), "CSV column count changed for line: " + line);
                Map<String, String> row = new LinkedHashMap<>();
                for (int i = 0; i < headers.size(); i++) {
                    row.put(headers.get(i), values.get(i));
                }
                rows.add(row);
            }
            return rows;
        }
    }

    private static List<String> parseCsvLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean quoted = false;

        for (int i = 0; i < line.length(); i++) {
            char character = line.charAt(i);
            if (character == '"') {
                if (quoted && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    quoted = !quoted;
                }
            } else if (character == ',' && !quoted) {
                values.add(current.toString());
                current.setLength(0);
            } else {
                current.append(character);
            }
        }
        values.add(current.toString());
        return values;
    }
}
