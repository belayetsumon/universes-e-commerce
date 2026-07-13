package com.ecommerce.app.module.system.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ecommerce.app.module.shipping.model.ShippingLocation;
import com.ecommerce.app.module.shipping.model.ShippingLocationType;
import com.ecommerce.app.module.shipping.repository.ShippingLocationRepository;
import com.ecommerce.app.module.shipping.services.ShippingLocationService;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Installs the Bangladesh country, division, district, and thana hierarchy
 * used by the shipping-location picker on a new deployment.
 */
@Service
public class BangladeshLocationSeedService {

    private static final List<DivisionSeed> BANGLADESH_DIVISIONS = List.of(
            new DivisionSeed("Dhaka", List.of("Dhaka", "Faridpur", "Gazipur", "Gopalganj", "Kishoreganj", "Madaripur", "Manikganj", "Munshiganj", "Narayanganj", "Narsingdi", "Rajbari", "Shariatpur", "Tangail")),
            new DivisionSeed("Khulna", List.of("Bagerhat", "Chuadanga", "Jashore", "Jhenaidah", "Khulna", "Kushtia", "Magura", "Meherpur", "Narail", "Satkhira")),
            new DivisionSeed("Chattogram", List.of("Bandarban", "Brahmanbaria", "Chandpur", "Chattogram", "Cumilla", "Cox's Bazar", "Feni", "Khagrachhari", "Lakshmipur", "Noakhali", "Rangamati")),
            new DivisionSeed("Rajshahi", List.of("Bogura", "Joypurhat", "Naogaon", "Natore", "Chapainawabganj", "Pabna", "Rajshahi", "Sirajganj")),
            new DivisionSeed("Sylhet", List.of("Habiganj", "Moulvibazar", "Sunamganj", "Sylhet")),
            new DivisionSeed("Rangpur", List.of("Dinajpur", "Gaibandha", "Kurigram", "Lalmonirhat", "Nilphamari", "Panchagarh", "Rangpur", "Thakurgaon")),
            new DivisionSeed("Mymensingh", List.of("Jamalpur", "Mymensingh", "Netrokona", "Sherpur")),
            new DivisionSeed("Barishal", List.of("Barguna", "Barishal", "Bhola", "Jhalokati", "Patuakhali", "Pirojpur"))
    );

    private static final Map<String, String> SOURCE_DISTRICT_CODE_ALIASES = Map.of(
            "Khagrachari", "KHAGRACHHARI",
            "Maulvibazar", "MOULVIBAZAR",
            "Nawabganj", "CHAPAINAWABGANJ",
            "Sirajgonj", "SIRAJGANJ"
    );

    /**
     * Dhaka's metropolitan police-station areas are not part of the standard
     * upazila source file. They are delivery-relevant urban areas, so seed
     * them as thana children of the Dhaka district as well.
     */
    private static final List<String> DHAKA_METROPOLITAN_THANAS = List.of(
            "Adabor", "Airport", "Badda", "Banani", "Bangshal", "Bhashantek",
            "Cantonment", "Chackbazar", "Darussalam", "Dakshinkhan", "Demra",
            "Dhanmondi", "Gandaria", "Gulshan", "Hazaribag", "Jatrabari",
            "Kadamtoli", "Kafrul", "Kalabagan", "Kamrangirchar", "Khilgaon",
            "Khilkhet", "Kotwali", "Lalbag", "Mirpur Model", "Mohammadpur",
            "Motijheel", "Mugda", "New Market", "Pallabi", "Paltan Model",
            "Ramna Model", "Rampura", "Rupnagar", "Sabujbag", "Shah Ali",
            "Shahbag", "Sherebanglanagar", "Shyampur", "Sutrapur", "Shahjahanpur",
            "Tejgaon", "Tejgaon I/A", "Turag", "Uttara Model", "Uttarkhan",
            "Uttara West", "Vatara", "Wari"
    );

    private final ShippingLocationRepository locationRepository;
    private final ShippingLocationService locationService;
    private final ObjectMapper objectMapper;

    public BangladeshLocationSeedService(ShippingLocationRepository locationRepository,
            ShippingLocationService locationService, ObjectMapper objectMapper) {
        this.locationRepository = locationRepository;
        this.locationService = locationService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public SeedResult seedBangladeshLocations() {
        SeedCounter counter = new SeedCounter();
        ShippingLocation country = ensureLocation(
                ShippingLocationType.COUNTRY, "Bangladesh", null, "BD", null, 1, counter);

        for (int divisionIndex = 0; divisionIndex < BANGLADESH_DIVISIONS.size(); divisionIndex++) {
            DivisionSeed divisionSeed = BANGLADESH_DIVISIONS.get(divisionIndex);
            ShippingLocation division = ensureLocation(
                    ShippingLocationType.DIVISION,
                    divisionSeed.name(),
                    null,
                    "BD_" + codeFor(divisionSeed.name()),
                    country,
                    divisionIndex + 1,
                    counter);

            for (int districtIndex = 0; districtIndex < divisionSeed.districts().size(); districtIndex++) {
                String districtName = divisionSeed.districts().get(districtIndex);
                ensureLocation(
                        ShippingLocationType.DISTRICT,
                        districtName,
                        null,
                        codeFor(districtName),
                        division,
                        districtIndex + 1,
                        counter);
            }
        }

        seedThanas(counter);
        seedDhakaMetropolitanThanas(counter);

        return new SeedResult(counter.created, counter.skipped);
    }

    private void seedThanas(SeedCounter counter) {
        Map<String, ShippingLocation> districtsBySourceId = new HashMap<>();
        for (JsonNode district : loadSeedArray("system/bangladesh-districts.json", "districts")) {
            String sourceName = district.path("name").asText();
            String districtCode = SOURCE_DISTRICT_CODE_ALIASES.getOrDefault(sourceName, codeFor(sourceName));
            ShippingLocation location = locationRepository
                    .findFirstByTypeAndCodeIgnoreCase(ShippingLocationType.DISTRICT, districtCode)
                    .orElseThrow(() -> new IllegalStateException("Missing seed district: " + sourceName));
            districtsBySourceId.put(district.path("id").asText(), location);
        }

        Map<String, Integer> thanaPriorities = new HashMap<>();
        for (JsonNode thana : loadSeedArray("system/bangladesh-upazilas.json", "upazilas")) {
            String districtId = thana.path("district_id").asText();
            ShippingLocation district = districtsBySourceId.get(districtId);
            if (district == null) {
                throw new IllegalStateException("Missing parent district for thana seed: " + thana.path("name").asText());
            }

            String thanaName = thana.path("name").asText();
            int priority = thanaPriorities.merge(districtId, 1, Integer::sum);
            ensureLocation(
                    ShippingLocationType.THANA,
                    thanaName,
                    thana.path("bn_name").asText(),
                    district.getCode() + "_" + codeFor(thanaName),
                    district,
                    priority,
                    counter);
        }
    }

    private void seedDhakaMetropolitanThanas(SeedCounter counter) {
        ShippingLocation dhakaDistrict = locationRepository
                .findFirstByTypeAndCodeIgnoreCase(ShippingLocationType.DISTRICT, "DHAKA")
                .orElseThrow(() -> new IllegalStateException("Missing Dhaka district for metropolitan thana seed."));

        for (int index = 0; index < DHAKA_METROPOLITAN_THANAS.size(); index++) {
            String thanaName = DHAKA_METROPOLITAN_THANAS.get(index);
            ensureLocation(
                    ShippingLocationType.THANA,
                    thanaName,
                    null,
                    "DHAKA_METRO_" + codeFor(thanaName),
                    dhakaDistrict,
                    1_000 + index,
                    counter);
        }
    }

    private JsonNode loadSeedArray(String resourcePath, String rootProperty) {
        try (InputStream inputStream = new ClassPathResource(resourcePath).getInputStream()) {
            JsonNode seedArray = objectMapper.readTree(inputStream).path(rootProperty);
            if (!seedArray.isArray()) {
                throw new IllegalStateException("Invalid location seed resource: " + resourcePath);
            }
            return seedArray;
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to read location seed resource: " + resourcePath, ex);
        }
    }

    private ShippingLocation ensureLocation(ShippingLocationType type, String name, String localName, String code,
            ShippingLocation parent, int priority, SeedCounter counter) {
        ShippingLocation existing = locationRepository.findFirstByTypeAndCodeIgnoreCase(type, code).orElse(null);
        if (existing != null) {
            counter.skipped++;
            return existing;
        }

        ShippingLocation location = new ShippingLocation();
        location.setType(type);
        location.setName(name);
        location.setLocalName(localName);
        location.setCode(code);
        location.setParent(parent);
        location.setPriority(priority);
        location.setActive(true);
        if (type == ShippingLocationType.COUNTRY) {
            location.setIso2("BD");
            location.setIso3("BGD");
        }
        counter.created++;
        return locationService.save(location);
    }

    private String codeFor(String value) {
        return value.toUpperCase(Locale.ROOT)
                .replaceAll("[^A-Z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
    }

    private record DivisionSeed(String name, List<String> districts) {
    }

    private static class SeedCounter {
        private int created;
        private int skipped;
    }

    public record SeedResult(int created, int skipped) {
        public int total() {
            return created + skipped;
        }
    }
}
