package com.ecommerce.app.vendor.controller;

import com.ecommerce.app.module.shipping.model.DeliveryPerson;
import com.ecommerce.app.module.shipping.repository.DeliveryPersonRepository;
import com.ecommerce.app.vendor.model.Vendorprofile;
import com.ecommerce.app.vendor.user.componant.VendorUserContext;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/vendor/delivery-persons")
public class Vendor_DeliveryPersonController {

    private final DeliveryPersonRepository deliveryPersonRepository;
    private final VendorUserContext vendorUserContext;

    public Vendor_DeliveryPersonController(DeliveryPersonRepository deliveryPersonRepository,
            VendorUserContext vendorUserContext) {
        this.deliveryPersonRepository = deliveryPersonRepository;
        this.vendorUserContext = vendorUserContext;
    }

    @GetMapping
    public String list(Model model) {
        Vendorprofile activeVendor = vendorUserContext.getActiveVendor();
        if (activeVendor == null || activeVendor.getId() == null) {
            model.addAttribute("deliveryPersons", List.of());
            model.addAttribute("activeCount", 0L);
            model.addAttribute("inactiveCount", 0L);
            model.addAttribute("errorMessage", "Vendor context not found.");
            return "vendor/delivery_person/list";
        }

        List<DeliveryPerson> deliveryPersons = deliveryPersonRepository.findByVendorId(activeVendor.getId());
        long activeCount = deliveryPersons.stream().filter(DeliveryPerson::isActive).count();
        model.addAttribute("deliveryPersons", deliveryPersons);
        model.addAttribute("activeCount", activeCount);
        model.addAttribute("inactiveCount", deliveryPersons.size() - activeCount);
        model.addAttribute("activeVendor", activeVendor);
        return "vendor/delivery_person/list";
    }

    @GetMapping("/new")
    public String createForm(Model model, RedirectAttributes redirectAttributes) {
        Vendorprofile activeVendor = vendorUserContext.getActiveVendor();
        if (activeVendor == null || activeVendor.getId() == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vendor context not found.");
            return "redirect:/vendor/home";
        }

        DeliveryPerson deliveryPerson = new DeliveryPerson();
        deliveryPerson.setVendorId(activeVendor.getId());
        deliveryPerson.setActive(true);
        model.addAttribute("deliveryPerson", deliveryPerson);
        model.addAttribute("activeVendor", activeVendor);
        return "vendor/delivery_person/form";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Vendorprofile activeVendor = vendorUserContext.getActiveVendor();
        if (activeVendor == null || activeVendor.getId() == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vendor context not found.");
            return "redirect:/vendor/home";
        }

        DeliveryPerson deliveryPerson = deliveryPersonRepository.findById(id).orElse(null);
        if (deliveryPerson == null || !Objects.equals(deliveryPerson.getVendorId(), activeVendor.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Delivery person not found for the active vendor.");
            return "redirect:/vendor/delivery-persons";
        }

        model.addAttribute("deliveryPerson", deliveryPerson);
        model.addAttribute("activeVendor", activeVendor);
        return "vendor/delivery_person/form";
    }

    @PostMapping
    public String save(@ModelAttribute DeliveryPerson deliveryPerson, RedirectAttributes redirectAttributes) {
        Vendorprofile activeVendor = vendorUserContext.getActiveVendor();
        if (activeVendor == null || activeVendor.getId() == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vendor context not found.");
            return "redirect:/vendor/home";
        }

        if (deliveryPerson.getId() != null) {
            DeliveryPerson existing = deliveryPersonRepository.findById(deliveryPerson.getId()).orElse(null);
            if (existing == null || !Objects.equals(existing.getVendorId(), activeVendor.getId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Delivery person not found for the active vendor.");
                return "redirect:/vendor/delivery-persons";
            }
        }

        deliveryPerson.setVendorId(activeVendor.getId());
        deliveryPersonRepository.save(deliveryPerson);
        redirectAttributes.addFlashAttribute("successMessage", "Delivery person saved successfully.");
        return "redirect:/vendor/delivery-persons";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Vendorprofile activeVendor = vendorUserContext.getActiveVendor();
        if (activeVendor == null || activeVendor.getId() == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vendor context not found.");
            return "redirect:/vendor/home";
        }

        DeliveryPerson deliveryPerson = deliveryPersonRepository.findById(id).orElse(null);
        if (deliveryPerson == null || !Objects.equals(deliveryPerson.getVendorId(), activeVendor.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Delivery person not found for the active vendor.");
            return "redirect:/vendor/delivery-persons";
        }

        deliveryPersonRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("successMessage", "Delivery person deleted successfully.");
        return "redirect:/vendor/delivery-persons";
    }
}
