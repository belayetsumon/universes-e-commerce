/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Repository.java to edit this template
 */
package com.ecommerce.app.module.settings.repository;

import com.ecommerce.app.module.settings.model.GlobalSettings;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author libertyerp_local
 */
@Repository
public interface GlobalSettingsRepository extends JpaRepository<GlobalSettings, Integer> {

    Optional<GlobalSettings> findFirstByActiveTrueOrderByIdAsc();
}
