/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Service.java to edit this template
 */
package com.ecommerce.app.module.system.services;

import com.ecommerce.app.module.system.model.EndpointInfo;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 *
 * @author libertyerp_local
 */
@Service
public class EndpointScannerService {

    @Autowired
    @Qualifier("requestMappingHandlerMapping")
    private RequestMappingHandlerMapping handlerMapping;

    public List<EndpointInfo> getAllEndpoints() {

        List<EndpointInfo> endpoints = new ArrayList<>();

        handlerMapping.getHandlerMethods().forEach((mapping, handler) -> {
            String packageName = handler.getBeanType().getPackageName();
            String controller = handler.getBeanType().getSimpleName();
            String method = handler.getMethod().getName();

            Set<String> paths = mapping.getPatternValues();

            Set<RequestMethod> methods = mapping.getMethodsCondition().getMethods();

            if (methods.isEmpty()) {
                for (String path : paths) {
                    endpoints.add(new EndpointInfo(
                            packageName,
                            controller,
                            method,
                            "ALL",
                            path
                    ));
                }
            } else {
                for (String path : paths) {
                    for (RequestMethod requestMethod : methods) {
                        endpoints.add(new EndpointInfo(
                                packageName,
                                controller,
                                method,
                                requestMethod.name(),
                                path
                        ));
                    }
                }
            }
        });

        endpoints.sort(
                Comparator.comparing(EndpointInfo::getPackageName)
                        .thenComparing(EndpointInfo::getController)
                        .thenComparing(EndpointInfo::getPath)
                        .thenComparing(EndpointInfo::getHttpMethod)
        );

        return endpoints;
    }
}
