package com.ecommerce.app.product.services;

import com.ecommerce.app.product.dto.ProductStockReportRow;
import com.ecommerce.app.product.dto.ProductStockReportSummary;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
public class ProductStockReportPdfService {

    private final SpringTemplateEngine templateEngine;

    public ProductStockReportPdfService(SpringTemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public byte[] generateAdminCurrentStockReport(
            List<ProductStockReportRow> rows,
            ProductStockReportSummary summary,
            Map<String, String> filters
    ) {
        Context context = new Context();
        context.setVariable("rows", rows == null ? List.of() : rows);
        context.setVariable("summary", summary == null ? new ProductStockReportSummary() : summary);
        context.setVariable("filters", filters == null ? Map.of() : filters);
        context.setVariable("issuedAt", LocalDateTime.now());

        String html = templateEngine.process("admin/stock/current-stock-pdf", context)
                .replaceFirst("^\\uFEFF", "")
                .trim();
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, "");
            builder.toStream(outputStream);
            builder.run();
            return outputStream.toByteArray();
        } catch (Exception ex) {
            throw new IllegalStateException("Could not generate current stock PDF.", ex);
        }
    }

    public String currentStockFilename() {
        return "current-stock-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")) + ".pdf";
    }
}
