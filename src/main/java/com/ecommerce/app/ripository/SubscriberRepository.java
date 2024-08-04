/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecommerce.app.ripository;

import com.ecommerce.app.model.Subscriber;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author User
 */
public interface SubscriberRepository extends JpaRepository<Subscriber, Long> {
    
}
