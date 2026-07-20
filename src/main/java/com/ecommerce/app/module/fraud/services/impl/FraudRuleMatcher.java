package com.ecommerce.app.module.fraud.services.impl;

import com.ecommerce.app.module.fraud.dto.FraudContext;
import com.ecommerce.app.module.fraud.dto.FraudSignalResult;
import com.ecommerce.app.module.fraud.model.FraudRule;
import com.ecommerce.app.module.fraud.model.FraudRuleOperator;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
class FraudRuleMatcher {

    boolean matches(FraudRule rule, List<FraudSignalResult> signals, FraudContext context) {
        if (rule == null || !rule.isActive() || !isEffective(rule)) {
            return false;
        }
        if (!scopeMatches(rule, context)) {
            return false;
        }

        Optional<FraudSignalResult> signal = findSignal(rule, signals);
        FraudRuleOperator operator = rule.getOperator() == null ? FraudRuleOperator.EXISTS : rule.getOperator();

        if (operator == FraudRuleOperator.NOT_EXISTS) {
            return signal.isEmpty();
        }
        if (signal.isEmpty()) {
            return false;
        }

        FraudSignalResult result = signal.get();
        return switch (operator) {
            case EXISTS -> result.isTriggered();
            case EQUALS -> equalsValue(result.getSignalValue(), rule.getComparisonValue());
            case NOT_EQUALS -> !equalsValue(result.getSignalValue(), rule.getComparisonValue());
            case GREATER_THAN -> compareNumber(result.getSignalValue(), rule.getComparisonValue(), 1);
            case GREATER_THAN_OR_EQUAL -> compareNumber(result.getSignalValue(), rule.getComparisonValue(), 0)
                    || compareNumber(result.getSignalValue(), rule.getComparisonValue(), 1);
            case LESS_THAN -> compareNumber(result.getSignalValue(), rule.getComparisonValue(), -1);
            case LESS_THAN_OR_EQUAL -> compareNumber(result.getSignalValue(), rule.getComparisonValue(), 0)
                    || compareNumber(result.getSignalValue(), rule.getComparisonValue(), -1);
            case CONTAINS -> containsValue(result.getSignalValue(), rule.getComparisonValue());
            case IN -> inValues(result.getSignalValue(), rule.getComparisonValue());
            case BETWEEN -> betweenValues(result.getSignalValue(), rule.getComparisonValue());
            case NOT_EXISTS -> false;
        };
    }

    Optional<FraudSignalResult> findSignal(FraudRule rule, List<FraudSignalResult> signals) {
        if (rule == null || rule.getSignalCode() == null || signals == null) {
            return Optional.empty();
        }
        return signals.stream()
                .filter(signal -> signal != null && rule.getSignalCode().equalsIgnoreCase(signal.getSignalCode()))
                .findFirst();
    }

    String executionDetail(FraudRule rule, FraudSignalResult signal, boolean matched) {
        String signalCode = signal == null ? "" : safe(signal.getSignalCode());
        String signalValue = signal == null ? "" : safe(signal.getSignalValue());
        return "{\"ruleCode\":\"" + safe(rule.getRuleCode())
                + "\",\"signalCode\":\"" + signalCode
                + "\",\"signalValue\":\"" + signalValue
                + "\",\"matched\":" + matched + "}";
    }

    private boolean isEffective(FraudRule rule) {
        LocalDateTime now = LocalDateTime.now();
        return (rule.getEffectiveStartAt() == null || !rule.getEffectiveStartAt().isAfter(now))
                && (rule.getEffectiveEndAt() == null || !rule.getEffectiveEndAt().isBefore(now));
    }

    private boolean scopeMatches(FraudRule rule, FraudContext context) {
        FraudContext safeContext = context == null ? new FraudContext() : context;
        if (rule.getPaymentMethod() != null && !equalsValue(rule.getPaymentMethod(), safeContext.getPaymentMethod())) {
            return false;
        }
        if (rule.getVendorId() != null && !rule.getVendorId().equals(safeContext.getVendorId())) {
            return false;
        }
        if (rule.getProductId() != null && !rule.getProductId().equals(safeContext.getProductId())) {
            return false;
        }
        if (rule.getCategoryId() != null && !rule.getCategoryId().equals(safeContext.getCategoryId())) {
            return false;
        }
        if (rule.getCountry() != null && !equalsValue(rule.getCountry(), safeContext.getShippingCountry())) {
            return false;
        }
        if (rule.getDistrict() != null && !equalsValue(rule.getDistrict(), safeContext.getShippingDistrict())) {
            return false;
        }
        return rule.getChannel() == null || equalsValue(rule.getChannel(), safeContext.getSalesChannel());
    }

    private boolean equalsValue(String left, String right) {
        if (left == null || right == null) {
            return left == null && right == null;
        }
        return normalize(left).equals(normalize(right));
    }

    private boolean containsValue(String left, String right) {
        if (left == null || right == null) {
            return false;
        }
        return normalize(left).contains(normalize(right));
    }

    private boolean inValues(String value, String csv) {
        if (value == null || csv == null) {
            return false;
        }
        String normalizedValue = normalize(value);
        return Arrays.stream(csv.split(","))
                .map(this::normalize)
                .anyMatch(normalizedValue::equals);
    }

    private boolean betweenValues(String value, String range) {
        if (value == null || range == null || !range.contains(",")) {
            return false;
        }
        String[] parts = range.split(",", 2);
        BigDecimal number = parseNumber(value);
        BigDecimal min = parseNumber(parts[0]);
        BigDecimal max = parseNumber(parts[1]);
        return number != null && min != null && max != null
                && number.compareTo(min) >= 0 && number.compareTo(max) <= 0;
    }

    private boolean compareNumber(String left, String right, int expectedComparison) {
        BigDecimal leftNumber = parseNumber(left);
        BigDecimal rightNumber = parseNumber(right);
        if (leftNumber == null || rightNumber == null) {
            return false;
        }
        return Integer.compare(leftNumber.compareTo(rightNumber), 0) == Integer.compare(expectedComparison, 0);
    }

    private BigDecimal parseNumber(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException ex) {
            if ("true".equalsIgnoreCase(value)) {
                return BigDecimal.ONE;
            }
            if ("false".equalsIgnoreCase(value)) {
                return BigDecimal.ZERO;
            }
            return null;
        }
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private String safe(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
