/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Repository.java to edit this template
 */
package com.ecommerce.app.module.ReferralRewards.repository;

import com.ecommerce.app.module.ReferralRewards.model.MultiLavelRateSettings;
import com.ecommerce.app.module.ReferralRewards.enumvalue.LevelEnum;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author libertyerp_local
 */
public interface LavelRateSettingsRepository extends JpaRepository<MultiLavelRateSettings, Long> {

    Optional<MultiLavelRateSettings> findTopByLevelOrderByIdDesc(LevelEnum level);

}
