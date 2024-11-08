/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Component.java to edit this template
 */
package com.ecommerce.app.globalComponant;

import java.time.Instant;
import org.springframework.stereotype.Component;

/**
 *
 * @author libertyerp_local
 */
@Component
public class UnixTimeComponent {
     public long unixTimeEpochSecond() {
        long unixTimeSeconds = Instant.now().getEpochSecond();
        return unixTimeSeconds;
    }
}
