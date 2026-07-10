package com.ecommerce.app.module.communication.repository;

import com.ecommerce.app.module.communication.model.CommunicationSetting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommunicationSettingRepository extends JpaRepository<CommunicationSetting, Integer> {
}
