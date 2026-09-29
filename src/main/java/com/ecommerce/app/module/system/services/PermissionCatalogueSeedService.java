package com.ecommerce.app.module.system.services;

import com.ecommerce.app.module.user.model.Modules;
import com.ecommerce.app.module.user.model.Privilege;
import com.ecommerce.app.module.user.ripository.ModuleRepository;
import com.ecommerce.app.module.user.ripository.PrivilegeRepository;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PermissionCatalogueSeedService {

    private static final String CATALOGUE_RESOURCE = "security/application-security-permission-catalogue.csv";

    private final ModuleRepository moduleRepository;
    private final PrivilegeRepository privilegeRepository;

    public PermissionCatalogueSeedService(ModuleRepository moduleRepository,
            PrivilegeRepository privilegeRepository) {
        this.moduleRepository = moduleRepository;
        this.privilegeRepository = privilegeRepository;
    }

    @Transactional
    public SeedResult seedPermissionCatalogue() {
        SeedCounter counter = new SeedCounter();
        Map<String, Modules> modulesBySlug = new HashMap<>();
        Set<String> existingModuleSlugs = new HashSet<>();
        for (PermissionSeed seed : loadCatalogue()) {
            counter.catalogueRows++;
            Modules module = modulesBySlug.computeIfAbsent(seed.moduleSlug(),
                    moduleSlug -> resolveModule(seed, counter, existingModuleSlugs));

            if (privilegeRepository.findFirstBySlugIgnoreCase(seed.permissionSlug()).isPresent()) {
                counter.permissionsExisting++;
                continue;
            }

            Privilege privilege = new Privilege();
            privilege.setModule(module);
            privilege.setSlug(seed.permissionSlug());
            privilege.setName(seed.permissionName());
            privilegeRepository.save(privilege);
            counter.permissionsCreated++;
        }
        return new SeedResult(
                counter.catalogueRows,
                counter.modulesCreated,
                counter.modulesExisting,
                counter.permissionsCreated,
                counter.permissionsExisting);
    }

    private Modules resolveModule(PermissionSeed seed, SeedCounter counter, Set<String> existingModuleSlugs) {
        Optional<Modules> existingModule = moduleRepository.findFirstBySlugIgnoreCase(seed.moduleSlug());
        if (existingModule.isPresent()) {
            if (existingModuleSlugs.add(seed.moduleSlug())) {
                counter.modulesExisting++;
            }
            return existingModule.get();
        }
        return createModule(seed, counter);
    }

    private Modules createModule(PermissionSeed seed, SeedCounter counter) {
        Modules module = new Modules();
        module.setSlug(seed.moduleSlug());
        module.setName(seed.moduleName());
        counter.modulesCreated++;
        return moduleRepository.save(module);
    }

    private List<PermissionSeed> loadCatalogue() {
        ClassPathResource resource = new ClassPathResource(CATALOGUE_RESOURCE);
        if (!resource.exists()) {
            throw new IllegalStateException("Permission catalogue resource is missing: " + CATALOGUE_RESOURCE);
        }

        Map<String, PermissionSeed> seedsBySlug = new LinkedHashMap<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new IllegalStateException("Permission catalogue is empty.");
            }
            Map<String, Integer> headers = indexHeaders(parseCsvLine(headerLine));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                List<String> values = parseCsvLine(line);
                String permissionSlug = value(values, headers, "PermissionCandidate");
                String moduleCandidate = primaryModule(value(values, headers, "ModuleCandidates"));
                if (permissionSlug == null || moduleCandidate == null) {
                    continue;
                }
                String normalizedPermission = permissionSlug.trim().toLowerCase(Locale.ROOT);
                String moduleSlug = moduleCandidate.trim().toLowerCase(Locale.ROOT);
                PermissionSeed seed = new PermissionSeed(
                        moduleSlug,
                        displayName(moduleSlug),
                        normalizedPermission,
                        displayName(normalizedPermission));
                seedsBySlug.putIfAbsent(normalizedPermission, seed);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read permission catalogue.", exception);
        }
        return new ArrayList<>(seedsBySlug.values());
    }

    private Map<String, Integer> indexHeaders(List<String> headers) {
        Map<String, Integer> indexed = new LinkedHashMap<>();
        for (int i = 0; i < headers.size(); i++) {
            indexed.put(normalizeCsvValue(headers.get(i)), i);
        }
        if (!indexed.containsKey("PermissionCandidate") || !indexed.containsKey("ModuleCandidates")) {
            throw new IllegalStateException("Permission catalogue header is invalid.");
        }
        return indexed;
    }

    private String value(List<String> values, Map<String, Integer> headers, String header) {
        Integer index = headers.get(header);
        if (index == null || index >= values.size()) {
            return null;
        }
        String value = normalizeCsvValue(values.get(index));
        return value == null || value.isBlank() ? null : value;
    }

    private String normalizeCsvValue(String value) {
        if (value == null) {
            return null;
        }
        return value.replace("\uFEFF", "").trim();
    }

    private String primaryModule(String moduleCandidates) {
        if (moduleCandidates == null || moduleCandidates.isBlank()) {
            return null;
        }
        return moduleCandidates.split("\\|", 2)[0];
    }

    private List<String> parseCsvLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder value = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char current = line.charAt(i);
            if (current == '"') {
                if (quoted && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    value.append('"');
                    i++;
                } else {
                    quoted = !quoted;
                }
            } else if (current == ',' && !quoted) {
                values.add(value.toString());
                value.setLength(0);
            } else {
                value.append(current);
            }
        }
        values.add(value.toString());
        return values;
    }

    private String displayName(String slug) {
        String[] parts = slug.replace('_', '.').split("\\.");
        List<String> words = new ArrayList<>();
        for (String part : parts) {
            if (part.isBlank()) {
                continue;
            }
            words.add(displayWord(part));
        }
        return String.join(" ", words);
    }

    private String displayWord(String value) {
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "api" -> "API";
            case "cod" -> "COD";
            case "emi" -> "EMI";
            case "iam" -> "IAM";
            case "pdf" -> "PDF";
            case "seo" -> "SEO";
            default -> Character.toUpperCase(normalized.charAt(0)) + normalized.substring(1);
        };
    }

    public record SeedResult(
            int catalogueRows,
            int modulesCreated,
            int modulesExisting,
            int permissionsCreated,
            int permissionsExisting) {
    }

    private record PermissionSeed(
            String moduleSlug,
            String moduleName,
            String permissionSlug,
            String permissionName) {
    }

    private static final class SeedCounter {

        private int catalogueRows;
        private int modulesCreated;
        private int modulesExisting;
        private int permissionsCreated;
        private int permissionsExisting;
    }
}
