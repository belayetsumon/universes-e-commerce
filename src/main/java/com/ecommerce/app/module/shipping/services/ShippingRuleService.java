package com.ecommerce.app.module.shipping.services;

import com.ecommerce.app.module.shipping.model.ShippingRule;
import com.ecommerce.app.module.shipping.repository.ShippingRuleRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ShippingRuleService {

    private final ShippingRuleRepository repository;

    public ShippingRuleService(ShippingRuleRepository repository) {
        this.repository = repository;
    }

    public List<ShippingRule> getAll() {
        return repository.findAll();
    }

    public List<ShippingRule> getActiveRules() {
        return repository.findByActiveTrueOrderByPriorityAscIdAsc();
    }

    public ShippingRule getById(Long id) {
        return repository.findById(id).orElse(null);
    }

    public ShippingRule save(ShippingRule rule) {
        return repository.save(rule);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}
