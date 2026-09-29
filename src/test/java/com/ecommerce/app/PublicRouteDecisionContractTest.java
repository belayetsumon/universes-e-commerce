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

class PublicRouteDecisionContractTest {

    private static final Path ENDPOINT_DECISION_LEDGER =
            Path.of("src/docs/application-security-endpoint-decision-ledger.csv");

    private static final Set<String> REVIEWED_PUBLIC_ROUTE_KEYS = Set.of(
            "/robots.txt GET",
            "/sitemap.xml GET",
            "/llms.txt GET",
            "/maintenance GET",
            "/district/select POST",
            "/register GET",
            "/register POST",
            "/customerregister/register GET",
            "/customerregister/register POST"
    );

    @Test
    void reviewedPublicIngressRoutesAreExplicitlyApproved() throws IOException {
        Map<String, Map<String, String>> rowsByPathAndMethod = rowsByPathAndMethod();

        for (String key : REVIEWED_PUBLIC_ROUTE_KEYS) {
            Map<String, String> row = rowsByPathAndMethod.get(key);
            assertNotNull(row, key + " must remain in the endpoint decision ledger");
            assertEquals("PUBLIC", row.get("FinalZone"), key + " must remain public ingress");
            assertEquals("PERMIT_ALL", row.get("CurrentUrlRule"), key + " must stay explicitly permit-all");
            assertEquals("APPROVED", row.get("DecisionStatus"), key + " must stay approved after source review");
            assertTrue(row.get("FinalCapability").startsWith("public."),
                    key + " must use a protected non-assignable public policy capability");
        }
    }

    @Test
    void noPublicIntentRoutesRemainBlockedByUrlRuleMismatch() throws IOException {
        Set<String> mismatchedPublicPaths = readCsv(ENDPOINT_DECISION_LEDGER).stream()
                .filter(row -> row.get("DecisionRationale").contains("Proposed public route is not explicitly permit-all"))
                .map(row -> row.get("Path"))
                .collect(Collectors.toSet());

        assertTrue(mismatchedPublicPaths.isEmpty(),
                "Public-intent routes must be aligned with SecurityConfig.PUBLIC_URLS: " + mismatchedPublicPaths);
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
            char c = line.charAt(i);
            if (quoted) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        current.append('"');
                        i++;
                    } else {
                        quoted = false;
                    }
                } else {
                    current.append(c);
                }
            } else if (c == '"') {
                quoted = true;
            } else if (c == ',') {
                values.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        values.add(current.toString());
        return values;
    }
}
