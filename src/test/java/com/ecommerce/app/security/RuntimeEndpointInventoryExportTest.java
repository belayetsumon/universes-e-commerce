package com.ecommerce.app.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.mock.web.MockServletContext;
import org.springframework.stereotype.Controller;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.support.GenericWebApplicationContext;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

class RuntimeEndpointInventoryExportTest {

    private static final String APPLICATION_PACKAGE = "com.ecommerce.app";
    private static final String OUTPUT_PROPERTY = "security.runtime.inventory.output";

    @Test
    void exportsSpringResolvedApplicationControllerMappings() throws Exception {
        List<RuntimeEndpoint> endpoints = scanRuntimeEndpoints();

        assertFalse(endpoints.isEmpty(), "No Spring MVC controller mappings were discovered");
        assertTrue(endpoints.stream().noneMatch(endpoint -> endpoint.path().isBlank()),
                "Runtime inventory contains a blank route path");

        String configuredOutput = System.getProperty(OUTPUT_PROPERTY);
        if (configuredOutput != null && !configuredOutput.isBlank()) {
            Path output = Path.of(configuredOutput).toAbsolutePath().normalize();
            writeCsv(output, endpoints);
            System.out.printf("runtime_inventory_rows=%d%n", endpoints.size());
            System.out.printf("runtime_inventory_output=%s%n", output);
        }
    }

    private List<RuntimeEndpoint> scanRuntimeEndpoints() throws Exception {
        try (GenericWebApplicationContext context = new GenericWebApplicationContext()) {
            context.setServletContext(new MockServletContext());
            AnnotationConfigUtils.registerAnnotationConfigProcessors(context);
            registerControllerDefinitions(context);
            context.registerBean(
                    "runtimeInventoryRequestMappingHandlerMapping",
                    RequestMappingHandlerMapping.class,
                    RequestMappingHandlerMapping::new);
            context.refresh();

            RequestMappingHandlerMapping handlerMapping = context.getBean(
                    "runtimeInventoryRequestMappingHandlerMapping",
                    RequestMappingHandlerMapping.class);
            List<RuntimeEndpoint> endpoints = new ArrayList<>();
            handlerMapping.getHandlerMethods().forEach((mapping, handler) -> {
                Set<String> paths = mapping.getPatternValues();
                Set<RequestMethod> methods = mapping.getMethodsCondition().getMethods();
                for (String path : paths) {
                    if (methods.isEmpty()) {
                        endpoints.add(RuntimeEndpoint.from(handler.getBeanType(), handler.getMethod().getName(), "ALL", path));
                    } else {
                        for (RequestMethod method : methods) {
                            endpoints.add(RuntimeEndpoint.from(
                                    handler.getBeanType(), handler.getMethod().getName(), method.name(), path));
                        }
                    }
                }
            });
            endpoints.sort(Comparator
                    .comparing(RuntimeEndpoint::packageName)
                    .thenComparing(RuntimeEndpoint::controller)
                    .thenComparing(RuntimeEndpoint::path)
                    .thenComparing(RuntimeEndpoint::httpMethod)
                    .thenComparing(RuntimeEndpoint::javaMethod));
            return List.copyOf(endpoints);
        }
    }

    private void registerControllerDefinitions(GenericWebApplicationContext context) throws ClassNotFoundException {
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(Controller.class, true));

        ClassLoader classLoader = RuntimeEndpointInventoryExportTest.class.getClassLoader();
        for (BeanDefinition candidate : scanner.findCandidateComponents(APPLICATION_PACKAGE)) {
            String className = candidate.getBeanClassName();
            if (className == null) {
                continue;
            }
            Class<?> controllerClass = ClassUtils.forName(className, classLoader);
            RootBeanDefinition definition = new RootBeanDefinition(controllerClass);
            definition.setLazyInit(true);
            context.registerBeanDefinition(className, definition);
        }
    }

    private void writeCsv(Path output, List<RuntimeEndpoint> endpoints) throws IOException {
        Path parent = output.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        List<String> lines = new ArrayList<>(endpoints.size() + 1);
        lines.add("Package,Controller,JavaMethod,HttpMethod,Path");
        for (RuntimeEndpoint endpoint : endpoints) {
            lines.add(String.join(",",
                    csv(endpoint.packageName()),
                    csv(endpoint.controller()),
                    csv(endpoint.javaMethod()),
                    csv(endpoint.httpMethod()),
                    csv(endpoint.path())));
        }
        Files.write(output, lines, StandardCharsets.UTF_8);
    }

    private String csv(String value) {
        return '"' + value.replace("\"", "\"\"") + '"';
    }

    private record RuntimeEndpoint(
            String packageName,
            String controller,
            String javaMethod,
            String httpMethod,
            String path) {

        private static RuntimeEndpoint from(Class<?> controllerType, String javaMethod, String httpMethod, String path) {
            return new RuntimeEndpoint(
                    controllerType.getPackageName(),
                    controllerType.getSimpleName(),
                    javaMethod,
                    httpMethod,
                    path);
        }
    }
}
