package com.ecommerce.app.module.shipping.services;

import com.ecommerce.app.module.shipping.model.Shipment;
import com.ecommerce.app.module.shipping.model.ShipmentInvoice;
import com.ecommerce.app.module.shipping.model.ShippingLabel;
import com.ecommerce.app.module.shipping.model.ShippingManifest;
import com.ecommerce.app.module.shipping.repository.ShipmentInvoiceRepository;
import com.ecommerce.app.module.shipping.repository.ShipmentRepository;
import com.ecommerce.app.module.shipping.repository.ShippingLabelRepository;
import com.ecommerce.app.module.shipping.repository.ShippingManifestRepository;
import com.ecommerce.app.module.order.model.SalesOrder;
import com.ecommerce.app.module.order.repository.SalesOrderRepository;
import com.ecommerce.app.services.StorageProperties;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
public class ShippingDocumentService {

    private final ShippingLabelRepository labelRepository;
    private final ShippingManifestRepository manifestRepository;
    private final ShipmentInvoiceRepository invoiceRepository;
    private final ShipmentRepository shipmentRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final CarrierService carrierService;
    private final SpringTemplateEngine templateEngine;
    private final StorageProperties storageProperties;

    public ShippingDocumentService(ShippingLabelRepository labelRepository,
            ShippingManifestRepository manifestRepository,
            ShipmentInvoiceRepository invoiceRepository,
            ShipmentRepository shipmentRepository,
            SalesOrderRepository salesOrderRepository,
            CarrierService carrierService,
            SpringTemplateEngine templateEngine,
            StorageProperties storageProperties) {
        this.labelRepository = labelRepository;
        this.manifestRepository = manifestRepository;
        this.invoiceRepository = invoiceRepository;
        this.shipmentRepository = shipmentRepository;
        this.salesOrderRepository = salesOrderRepository;
        this.carrierService = carrierService;
        this.templateEngine = templateEngine;
        this.storageProperties = storageProperties;
    }

    public List<ShippingLabel> getLabels() {
        return labelRepository.findAll();
    }

    public List<ShippingManifest> getManifests() {
        return manifestRepository.findAll();
    }

    public List<ShipmentInvoice> getInvoices() {
        return invoiceRepository.findAll();
    }

    public List<ShippingLabel> getLabelsByVendor(Long vendorId) {
        return labelRepository.findByShipmentVendorIdOrderByIdDesc(vendorId);
    }

    public List<ShippingManifest> getManifestsByVendor(Long vendorId) {
        return manifestRepository.findDistinctByShipmentsVendorIdOrderByIdDesc(vendorId);
    }

    public List<ShipmentInvoice> getInvoicesByVendor(Long vendorId) {
        return invoiceRepository.findByShipmentVendorIdOrderByIdDesc(vendorId);
    }

    public ShippingLabel getLabel(Long id) {
        return labelRepository.findById(id).orElse(null);
    }

    public ShippingManifest getManifest(Long id) {
        return manifestRepository.findById(id).orElse(null);
    }

    public ShipmentInvoice getInvoice(Long id) {
        return invoiceRepository.findById(id).orElse(null);
    }

    @Transactional
    public ShippingLabel saveLabel(ShippingLabel label) {
        if (label.getLabelNumber() == null || label.getLabelNumber().isBlank()) {
            label.setLabelNumber("LBL-" + documentTimestamp());
        }
        ShippingLabel savedLabel = labelRepository.save(label);
        syncShipmentLabelUrl(savedLabel);
        return savedLabel;
    }

    @Transactional
    public ShippingLabel generateLabelForShipment(Long shipmentId, String createdFrom) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new IllegalArgumentException("Shipment not found."));

        ShippingLabel label = new ShippingLabel();
        label.setShipment(shipment);
        label.setActive(true);
        label.setLabelNumber(resolveLabelNumber(shipment));

        try {
            String carrierLabelUrl = shipment.getCarrier() != null && shipment.getCarrier().getId() != null
                    ? carrierService.generateShipmentLabel(shipment.getCarrier().getId(), shipment)
                    : null;
            if (hasText(carrierLabelUrl)) {
                label.setLabelUrl(carrierLabelUrl);
                label.setLabelPayload("Generated from carrier by " + createdFrom);
                return saveLabel(label);
            }
        } catch (Exception ex) {
            label.setLabelPayload("Carrier label failed: " + ex.getMessage());
        }

        String localLabelUrl = generateLocalLabelPdf(shipment, label.getLabelNumber(), createdFrom);
        label.setLabelUrl(localLabelUrl);
        if (!hasText(label.getLabelPayload())) {
            label.setLabelPayload("Generated local PDF label by " + createdFrom);
        }
        return saveLabel(label);
    }

    public ShippingManifest saveManifest(ShippingManifest manifest) {
        if (manifest.getManifestNumber() == null || manifest.getManifestNumber().isBlank()) {
            manifest.setManifestNumber("MAN-" + documentTimestamp());
        }
        if (manifest.isClosed() && manifest.getHandoverTime() == null) {
            manifest.setHandoverTime(LocalDateTime.now());
        }
        return manifestRepository.save(manifest);
    }

    @Transactional
    public ShipmentInvoice saveInvoice(ShipmentInvoice invoice) {
        if (invoice.getInvoiceNumber() == null || invoice.getInvoiceNumber().isBlank()) {
            invoice.setInvoiceNumber("SINV-" + documentTimestamp());
        }
        invoice.snapshotFromShipment();
        return invoiceRepository.save(invoice);
    }

    @Transactional
    public ShipmentInvoice generateInvoiceForShipment(Long shipmentId) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new IllegalArgumentException("Shipment not found."));
        ShipmentInvoice invoice = new ShipmentInvoice();
        invoice.setShipment(shipment);
        return saveInvoice(invoice);
    }

    public void deleteLabel(Long id) {
        labelRepository.deleteById(id);
    }

    public void deleteManifest(Long id) {
        manifestRepository.deleteById(id);
    }

    public void deleteInvoice(Long id) {
        invoiceRepository.deleteById(id);
    }

    private void syncShipmentLabelUrl(ShippingLabel label) {
        if (label == null || label.getShipment() == null || label.getShipment().getId() == null || !hasText(label.getLabelUrl())) {
            return;
        }
        Shipment shipment = shipmentRepository.findById(label.getShipment().getId()).orElse(null);
        if (shipment == null) {
            return;
        }
        shipment.setLabelUrl(label.getLabelUrl());
        shipmentRepository.save(shipment);
    }

    private String generateLocalLabelPdf(Shipment shipment, String labelNumber, String createdFrom) {
        try {
            SalesOrder order = shipment.getSalesOrderId() != null
                    ? salesOrderRepository.findById(shipment.getSalesOrderId()).orElse(null)
                    : null;
            Context context = new Context();
            context.setVariable("shipment", shipment);
            context.setVariable("order", order);
            context.setVariable("labelNumber", labelNumber);
            context.setVariable("createdFrom", createdFrom);
            context.setVariable("createdAt", LocalDateTime.now());

            String html = templateEngine.process("admin/shipping/documents/local-label-pdf", context)
                    .replaceFirst("^\\uFEFF", "")
                    .trim();
            byte[] pdfBytes;
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                PdfRendererBuilder builder = new PdfRendererBuilder();
                builder.useFastMode();
                builder.withHtmlContent(html, "");
                builder.toStream(outputStream);
                builder.run();
                pdfBytes = outputStream.toByteArray();
            }

            String fileName = labelNumber.replaceAll("[^A-Za-z0-9._-]", "-") + ".pdf";
            Path directory = Paths.get(storageProperties.getRootPath(), "shipping", "labels");
            Files.createDirectories(directory);
            Files.write(directory.resolve(fileName), pdfBytes);
            return "/files/shipping/labels/" + fileName;
        } catch (Exception ex) {
            throw new IllegalStateException("Could not generate local shipping label PDF.", ex);
        }
    }

    private String resolveLabelNumber(Shipment shipment) {
        if (shipment.getTrackingNumber() != null && !shipment.getTrackingNumber().isBlank()) {
            return shipment.getTrackingNumber().trim();
        }
        return "LBL-SHP-" + shipment.getId() + "-" + documentTimestamp();
    }

    private String documentTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
