/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Service.java to edit this template
 */
package com.ecommerce.app.module.ReferralRewards.services;

import com.ecommerce.app.module.ReferralRewards.model.MultiLavelRateSettings;
import com.ecommerce.app.module.ReferralRewards.enumvalue.LevelEnum;
import com.ecommerce.app.module.ReferralRewards.repository.LavelRateSettingsRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author libertyerp_local
 */
@Service
public class LavelRateSettingsService {

    private static final Map<LevelEnum, BigDecimal> DEFAULT_RATES = new EnumMap<>(LevelEnum.class);

    static {
        DEFAULT_RATES.put(LevelEnum.One, new BigDecimal("0.10"));
        DEFAULT_RATES.put(LevelEnum.Two, new BigDecimal("0.05"));
        DEFAULT_RATES.put(LevelEnum.Three, new BigDecimal("0.04"));
        DEFAULT_RATES.put(LevelEnum.Four, new BigDecimal("0.03"));
        DEFAULT_RATES.put(LevelEnum.Five, new BigDecimal("0.02"));
        DEFAULT_RATES.put(LevelEnum.Six, new BigDecimal("0.01"));
        DEFAULT_RATES.put(LevelEnum.Seven, new BigDecimal("0.01"));
        DEFAULT_RATES.put(LevelEnum.Eight, new BigDecimal("0.005"));
    }

    @Autowired
    private LavelRateSettingsRepository lavelRateSettingsRepository;

    public BigDecimal getCommissionRateForLevel(int levelNumber) {
        LevelEnum level = toLevelEnum(levelNumber);
        if (level == null) {
            return BigDecimal.ZERO;
        }

        return lavelRateSettingsRepository.findTopByLevelOrderByIdDesc(level)
                .map(MultiLavelRateSettings::getCommissionRate)
                .map(this::normalizeRate)
                .filter(rate -> rate.compareTo(BigDecimal.ZERO) > 0)
                .orElse(DEFAULT_RATES.getOrDefault(level, BigDecimal.ZERO));
    }

    public LevelEnum toLevelEnum(int levelNumber) {
        return switch (levelNumber) {
            case 1 ->
                LevelEnum.One;
            case 2 ->
                LevelEnum.Two;
            case 3 ->
                LevelEnum.Three;
            case 4 ->
                LevelEnum.Four;
            case 5 ->
                LevelEnum.Five;
            case 6 ->
                LevelEnum.Six;
            case 7 ->
                LevelEnum.Seven;
            case 8 ->
                LevelEnum.Eight;
            case 9 ->
                LevelEnum.Nine;
            case 10 ->
                LevelEnum.Ten;
            default ->
                null;
        };
    }

    private BigDecimal normalizeRate(BigDecimal rawRate) {
        if (rawRate == null) {
            return BigDecimal.ZERO;
        }

        if (rawRate.compareTo(BigDecimal.ONE) > 0) {
            return rawRate.divide(new BigDecimal("100"), 6, RoundingMode.HALF_UP);
        }

        return rawRate;
    }

}
