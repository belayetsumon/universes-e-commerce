package com.ecommerce.app.module.shipping.controller;

import com.ecommerce.app.module.shipping.model.Shipment;
import com.ecommerce.app.module.shipping.model.ShipmentInvoice;
import com.ecommerce.app.module.shipping.model.ShippingLabel;
import com.ecommerce.app.module.shipping.model.ShippingManifest;
import com.ecommerce.app.module.shipping.services.CarrierService;
import com.ecommerce.app.module.shipping.services.ShipmentService;
import com.ecommerce.app.module.shipping.services.ShippingDocumentService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/shipping-documents")
public class ShippingDocumentController {

    private final ShippingDocumentService service;
    private final ShipmentService shipmentService;
    private final CarrierService carrierService;

    public ShippingDocumentController(ShippingDocumentService service,
            ShipmentService shipmentService,
            CarrierService carrierService) {
        this.service = service;
        this.shipmentService = shipmentService;
        this.carrierService = carrierService;
    }

    @GetMapping("/labels")
    public String labels(Model model) {
        model.addAttribute("labels", service.getLabels());
        return "admin/shipping/documents/labels";
    }

    @GetMapping("/labels/create")
    public String createLabel(Model model) {
        model.addAttribute("label", new ShippingLabel());
        populateShipments(model);
        return "admin/shipping/documents/label_form";
    }

    @PostMapping("/labels/save")
    public String saveLabel(@Valid @ModelAttribute("label") ShippingLabel label,
            BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            populateShipments(model);
            return "admin/shipping/documents/label_form";
        }
        service.saveLabel(label);
        redirectAttributes.addFlashAttribute("successMessage", "Shipping label saved successfully.");
        return "redirect:/admin/shipping-documents/labels";
    }

    @PostMapping("/labels/delete/{id}")
    public String deleteLabel(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        service.deleteLabel(id);
        redirectAttributes.addFlashAttribute("successMessage", "Shipping label deleted successfully.");
        return "redirect:/admin/shipping-documents/labels";
    }

    @GetMapping("/manifests")
    public String manifests(Model model) {
        model.addAttribute("manifests", service.getManifests());
        return "admin/shipping/documents/manifests";
    }

    @GetMapping("/manifests/create")
    public String createManifest(Model model) {
        model.addAttribute("manifest", new ShippingManifest());
        populateManifest(model);
        return "admin/shipping/documents/manifest_form";
    }

    @PostMapping("/manifests/save")
    public String saveManifest(@Valid @ModelAttribute("manifest") ShippingManifest manifest,
            BindingResult result,
            @RequestParam(name = "shipmentIds", required = false) List<Long> shipmentIds,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (shipmentIds != null) {
            manifest.setShipments(shipmentIds.stream()
                    .map(shipmentService::getById)
                    .filter(java.util.Objects::nonNull)
                    .toList());
        }
        if (result.hasErrors()) {
            populateManifest(model);
            return "admin/shipping/documents/manifest_form";
        }
        service.saveManifest(manifest);
        redirectAttributes.addFlashAttribute("successMessage", "Shipping manifest saved successfully.");
        return "redirect:/admin/shipping-documents/manifests";
    }

    @PostMapping("/manifests/delete/{id}")
    public String deleteManifest(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        service.deleteManifest(id);
        redirectAttributes.addFlashAttribute("successMessage", "Shipping manifest deleted successfully.");
        return "redirect:/admin/shipping-documents/manifests";
    }

    @GetMapping("/invoices")
    public String invoices(Model model) {
        model.addAttribute("invoices", service.getInvoices());
        return "admin/shipping/documents/invoices";
    }

    @GetMapping("/invoices/create")
    public String createInvoice(Model model) {
        model.addAttribute("invoice", new ShipmentInvoice());
        populateShipments(model);
        return "admin/shipping/documents/invoice_form";
    }

    @PostMapping("/invoices/save")
    public String saveInvoice(@Valid @ModelAttribute("invoice") ShipmentInvoice invoice,
            BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            populateShipments(model);
            return "admin/shipping/documents/invoice_form";
        }
        service.saveInvoice(invoice);
        redirectAttributes.addFlashAttribute("successMessage", "Shipment invoice saved successfully.");
        return "redirect:/admin/shipping-documents/invoices";
    }

    @PostMapping("/invoices/delete/{id}")
    public String deleteInvoice(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        service.deleteInvoice(id);
        redirectAttributes.addFlashAttribute("successMessage", "Shipment invoice deleted successfully.");
        return "redirect:/admin/shipping-documents/invoices";
    }

    private void populateShipments(Model model) {
        model.addAttribute("shipments", shipmentService.getAll());
    }

    private void populateManifest(Model model) {
        populateShipments(model);
        model.addAttribute("carriers", carrierService.getAll());
    }
}
