/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Repository.java to edit this template
 */
package com.ecommerce.app.module.ads.repository;

import com.ecommerce.app.module.ads.model.Ads;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 *
 * @author libertyerp_local
 */
public interface AdsRepository extends JpaRepository<Ads, Long>, JpaSpecificationExecutor<Ads> {

}
