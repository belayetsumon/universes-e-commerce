package com.ecommerce.app.module.communication.services;

import com.ecommerce.app.module.communication.model.CommunicationSetting;
import com.ecommerce.app.module.communication.repository.CommunicationSettingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommunicationSettingsService {

    private final CommunicationSettingRepository repository;

    public CommunicationSettingsService(CommunicationSettingRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public CommunicationSetting getSettings() {
        return repository.findById(1).orElseGet(CommunicationSetting::new);
    }

    @Transactional
    public CommunicationSetting save(CommunicationSetting setting) {
        setting.setId(1);
        return repository.save(setting);
    }
}
