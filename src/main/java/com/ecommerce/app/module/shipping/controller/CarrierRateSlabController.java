package com.ecommerce.app.module.shipping.controller;

import com.ecommerce.app.module.shipping.model.CarrierRate;
import com.ecommerce.app.module.shipping.model.CarrierRateSlab;
import com.ecommerce.app.module.shipping.repository.CarrierRateRepository;
import com.ecommerce.app.module.shipping.services.CarrierRateSlabService;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import org.springframework.dao.DataIntegrityViolationException;
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
@RequestMapping("/admin/carrier-rate-slabs")
public class CarrierRateSlabController {

    private final CarrierRateSlabService service;
    private final CarrierRateRepository carrierRateRepository;

    public CarrierRateSlabController(CarrierRateSlabService service,
            CarrierRateRepository carrierRateRepository) {
        this.service = service;
        this.carrierRateRepository = carrierRateRepository;
    }

    @GetMapping("/list")
    public String list(@RequestParam(name = "rateId", required = false) Long rateId, Model model) {
        CarrierRate rate = resolveRate(rateId);
        model.addAttribute("selectedRate", rate);
        model.addAttribute("rates", carrierRateRepository.findAll());
        model.addAttribute("slabs", service.getByRate(rate));
        return "admin/shipping/carriers/carrier_rate_slab_list";
    }

    @GetMapping("/create")
    public String create(@RequestParam(name = "rateId", required = false) Long rateId, Model model) {
        CarrierRateSlab slab = new CarrierRateSlab();
        CarrierRate rate = resolveRate(rateId);
        if (rate != null) {
            slab.setCarrierRate(rate);
        }
        model.addAttribute("slab", slab);
        populateFormOptions(model);
        return "admin/shipping/carriers/carrier_rate_slab_form";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        CarrierRateSlab slab = service.getById(id);
        if (slab == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Carrier rate slab not found.");
            return "redirect:/admin/carrier-rates/list";
        }
        model.addAttribute("slab", slab);
        populateFormOptions(model);
        return "admin/shipping/carriers/carrier_rate_slab_form";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("slab") CarrierRateSlab slab,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {
        validateWeightRange(slab, result);
        if (result.hasErrors()) {
            populateFormOptions(model);
            return "admin/shipping/carriers/carrier_rate_slab_form";
        }

        try {
            service.save(slab);
            redirectAttributes.addFlashAttribute("successMessage", "Carrier rate slab saved successfully.");
            return "redirect:/admin/carrier-rate-slabs/list?rateId=" + slab.getCarrierRate().getId();
        } catch (DataIntegrityViolationException ex) {
            result.reject("carrierRateSlab.save.failed", "Carrier rate slab could not be saved because it conflicts with existing data.");
            populateFormOptions(model);
            return "admin/shipping/carriers/carrier_rate_slab_form";
        }
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        CarrierRateSlab slab = service.getById(id);
        Long rateId = slab != null && slab.getCarrierRate() != null ? slab.getCarrierRate().getId() : null;
        service.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Carrier rate slab deleted successfully.");
        return rateId != null
                ? "redirect:/admin/carrier-rate-slabs/list?rateId=" + rateId
                : "redirect:/admin/carrier-rates/list";
    }

    private void populateFormOptions(Model model) {
        model.addAttribute("rates", carrierRateRepository.findAll());
    }

    private CarrierRate resolveRate(Long rateId) {
        if (rateId == null) {
            return null;
        }
        return carrierRateRepository.findById(rateId).orElse(null);
    }

    private void validateWeightRange(CarrierRateSlab slab, BindingResult result) {
        BigDecimal minWeight = slab.getMinWeight();
        BigDecimal maxWeight = slab.getMaxWeight();
        if (minWeight != null && maxWeight != null && maxWeight.compareTo(minWeight) < 0) {
            result.rejectValue("maxWeight", "carrierRateSlab.maxWeight.invalid",
                    "Maximum weight must be greater than or equal to minimum weight.");
        }
    }
}
