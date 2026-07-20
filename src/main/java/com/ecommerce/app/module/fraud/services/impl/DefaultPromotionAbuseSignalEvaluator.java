package com.ecommerce.app.module.fraud.services.impl;

import com.ecommerce.app.module.fraud.dto.FraudContext;
import com.ecommerce.app.module.fraud.dto.FraudSignalResult;
import com.ecommerce.app.module.fraud.model.FraudReasonCode;
import com.ecommerce.app.module.fraud.model.FraudSignalSeverity;
import com.ecommerce.app.module.fraud.services.evaluator.PromotionAbuseSignalEvaluator;
import com.ecommerce.app.module.order.model.SalesOrder;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class DefaultPromotionAbuseSignalEvaluator extends AbstractFraudSignalEvaluator implements PromotionAbuseSignalEvaluator {

    private static final BigDecimal LARGE_DISCOUNT_PERCENT = new BigDecimal("50.00");

    @Override
    public List<FraudSignalResult> evaluate(SalesOrder order, FraudContext context) {
        List<FraudSignalResult> signals = new ArrayList<>();
        BigDecimal orderTotal = money(order == null ? null : order.getGrandTotal());
        BigDecimal discount = money(order == null ? null : order.getTotalDiscountAmount())
                .add(money(context.getCouponDiscountAmount()));

        BigDecimal discountPercent = BigDecimal.ZERO;
        if (orderTotal.compareTo(BigDecimal.ZERO) > 0) {
            discountPercent = discount.multiply(new BigDecimal("100")).divide(orderTotal, 2, RoundingMode.HALF_UP);
        }
        boolean largeDiscount = discountPercent.compareTo(LARGE_DISCOUNT_PERCENT) >= 0;
        signals.add(signal("LARGE_DISCOUNT_PERCENTAGE", category(), largeDiscount, 15, FraudSignalSeverity.MEDIUM,
                FraudReasonCode.PROMOTION_STACKING, discountPercent.toPlainString(), "promotion-abuse", "{\"threshold\":50}"));

        boolean couponUsed = context.getCouponCode() != null && !context.getCouponCode().isBlank();
        signals.add(signal("COUPON_USED", category(), couponUsed, 0, FraudSignalSeverity.INFO,
                null, couponUsed ? context.getCouponCode() : null, "promotion-abuse", null));

        boolean highWalletUsage = orderTotal.compareTo(BigDecimal.ZERO) > 0
                && money(context.getWalletAmount()).compareTo(orderTotal.multiply(new BigDecimal("0.80"))) >= 0;
        signals.add(signal("HIGH_WALLET_USAGE", category(), highWalletUsage, 15, FraudSignalSeverity.MEDIUM,
                FraudReasonCode.WALLET_ABUSE, String.valueOf(money(context.getWalletAmount())), "promotion-abuse", "{\"thresholdPercent\":80}"));

        boolean highGiftCardUsage = orderTotal.compareTo(BigDecimal.ZERO) > 0
                && money(context.getGiftCardAmount()).compareTo(orderTotal.multiply(new BigDecimal("0.80"))) >= 0;
        signals.add(signal("HIGH_GIFT_CARD_USAGE", category(), highGiftCardUsage, 15, FraudSignalSeverity.MEDIUM,
                FraudReasonCode.GIFT_CARD_ABUSE, String.valueOf(money(context.getGiftCardAmount())), "promotion-abuse", "{\"thresholdPercent\":80}"));

        boolean cashbackUsage = money(context.getCashbackAmount()).compareTo(BigDecimal.ZERO) > 0;
        signals.add(signal("CASHBACK_USED", category(), cashbackUsage, cashbackUsage ? 5 : 0, FraudSignalSeverity.LOW,
                FraudReasonCode.CASHBACK_ABUSE, String.valueOf(money(context.getCashbackAmount())), "promotion-abuse", null));

        return signals;
    }
}
