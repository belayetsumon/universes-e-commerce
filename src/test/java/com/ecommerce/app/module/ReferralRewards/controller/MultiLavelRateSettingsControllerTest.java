package com.ecommerce.app.module.ReferralRewards.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ecommerce.app.module.ReferralRewards.enumvalue.LevelEnum;
import com.ecommerce.app.module.ReferralRewards.model.MultiLavelRateSettings;
import com.ecommerce.app.module.ReferralRewards.repository.LavelRateSettingsRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ConcurrentModel;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

@ExtendWith(MockitoExtension.class)
class MultiLavelRateSettingsControllerTest {

    @Mock
    private LavelRateSettingsRepository lavelRateSettingsRepository;

    private MultiLavelRateSettingsController controller;

    @BeforeEach
    void setUp() {
        controller = new MultiLavelRateSettingsController();
        controller.lavelRateSettingsRepository = lavelRateSettingsRepository;
    }

    @Test
    void duplicateLevelIsRejectedOnCreate() {
        MultiLavelRateSettings form = validSettings(null, LevelEnum.One);
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(form, "lavelRateSettings");
        when(lavelRateSettingsRepository.existsByLevel(LevelEnum.One)).thenReturn(true);

        String view = controller.save(
                form,
                bindingResult,
                new RedirectAttributesModelMap(),
                new ConcurrentModel());

        assertEquals("admin/referral_rewards/lavel_rate_settings_form", view);
        assertTrue(bindingResult.hasFieldErrors("level"));
        verify(lavelRateSettingsRepository, never()).save(form);
    }

    @Test
    void sameLevelIsAllowedWhenEditingSameRecord() {
        MultiLavelRateSettings form = validSettings(7L, LevelEnum.Two);
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(form, "lavelRateSettings");
        when(lavelRateSettingsRepository.existsByLevelAndIdNot(LevelEnum.Two, 7L)).thenReturn(false);

        String view = controller.save(
                form,
                bindingResult,
                new RedirectAttributesModelMap(),
                new ConcurrentModel());

        assertEquals("redirect:/lavelratesettings/list", view);
        verify(lavelRateSettingsRepository).save(form);
    }

    private MultiLavelRateSettings validSettings(Long id, LevelEnum level) {
        MultiLavelRateSettings settings = new MultiLavelRateSettings();
        settings.setId(id);
        settings.setLevel(level);
        settings.setAmount(BigDecimal.TEN);
        settings.setTotalRef(BigDecimal.ONE);
        settings.setCommissionRate(BigDecimal.ONE);
        return settings;
    }
}
