/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Component.java to edit this template
 */
package com.ecommerce.app.globalAOPComponent;

/**
 *
 * @author libertyerp_local
 */
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class DeleteValidationAspect {

    @Before("@annotation(com.ecommerce.app.globalAOPComponent.CheckNoChildrenBeforeDelete)")
    public void checkBeforeDelete(JoinPoint joinPoint) {
        for (Object arg : joinPoint.getArgs()) {
            // You can filter by entity type if needed
            if (arg != null) {
                EntityDeleteValidator.validateNoChildren(arg);
            }
        }
    }
}
