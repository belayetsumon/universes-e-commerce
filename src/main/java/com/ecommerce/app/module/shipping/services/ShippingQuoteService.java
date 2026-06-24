package com.ecommerce.app.module.shipping.services;

import com.ecommerce.app.module.shipping.dto.ShippingOption;
import com.ecommerce.app.module.shipping.model.ShippingLocation;
import com.ecommerce.app.module.shipping.model.ShippingRule;
import com.ecommerce.app.module.shipping.model.ShippingRuleAction;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ShippingQuoteService {

    private final ShippingOptionService shippingOptionService;
    private final ShippingRuleService shippingRuleService;

    public ShippingQuoteService(ShippingOptionService shippingOptionService, ShippingRuleService shippingRuleService) {
        this.shippingOptionService = shippingOptionService;
        this.shippingRuleService = shippingRuleService;
    }

    public List<ShippingOption> getShippingOptions(Long vendorId, ShippingLocation customerLocation,
            BigDecimal totalWeight, BigDecimal orderAmount) {
        List<ShippingOption> options = shippingOptionService.getShippingOptions(vendorId, customerLocation, totalWeight);
        if (options == null || options.isEmpty()) {
            return List.of();
        }

        List<ShippingRule> rules = shippingRuleService.getActiveRules();
        if (rules == null || rules.isEmpty()) {
            return options;
        }

        List<ShippingOption> filtered = options.stream()
                .filter(option -> !isCarrierDisabled(option, rules, vendorId, customerLocation, totalWeight, orderAmount))
                .toList();

        for (ShippingOption option : filtered) {
            applyOptionRules(option, rules, vendorId, customerLocation, totalWeight, orderAmount);
        }

        return filtered.stream()
                .sorted(Comparator
                        .<ShippingOption>comparingInt(option
                                -> resolvePriority(option, rules, vendorId, customerLocation, totalWeight, orderAmount))
                        .thenComparing(
                                ShippingOption::getCarrierName,
                                Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();
    }

    public List<ShippingOption> getShippingOptions(Long vendorId, ShippingLocation customerLocation,
            BigDecimal totalWeight) {
        return getShippingOptions(vendorId, customerLocation, totalWeight, BigDecimal.ZERO);
    }

    private boolean isCarrierDisabled(ShippingOption option, List<ShippingRule> rules, Long vendorId,
            ShippingLocation customerLocation, BigDecimal totalWeight, BigDecimal orderAmount) {
        return rules.stream().anyMatch(rule -> rule.getAction() == ShippingRuleAction.DISABLE_CARRIER
                && rule.matches(vendorId, customerLocation, totalWeight, orderAmount, option.getCarrierCode()));
    }

    private void applyOptionRules(ShippingOption option, List<ShippingRule> rules, Long vendorId,
            ShippingLocation customerLocation, BigDecimal totalWeight, BigDecimal orderAmount) {
        for (ShippingRule rule : rules) {
            if (!rule.matches(vendorId, customerLocation, totalWeight, orderAmount, option.getCarrierCode())) {
                continue;
            }
            if (rule.getAction() == ShippingRuleAction.DISABLE_COD) {
                option.setCodAvailable(false);
            }
            if (rule.getAction() == ShippingRuleAction.ADD_EXTRA_FEE && rule.getExtraFee() != null) {
                BigDecimal currentPrice = option.getPrice() != null ? option.getPrice() : BigDecimal.ZERO;
                option.setPrice(currentPrice.add(rule.getExtraFee()));
            }
        }
    }

    private int resolvePriority(ShippingOption option, List<ShippingRule> rules, Long vendorId,
            ShippingLocation customerLocation, BigDecimal totalWeight, BigDecimal orderAmount) {
        return rules.stream()
                .filter(rule -> rule.getAction() == ShippingRuleAction.PRIORITIZE)
                .filter(rule -> rule.matches(vendorId, customerLocation, totalWeight, orderAmount, option.getCarrierCode()))
                .map(ShippingRule::getPriority)
                .filter(java.util.Objects::nonNull)
                .findFirst()
                .orElse(1000);
    }
}
