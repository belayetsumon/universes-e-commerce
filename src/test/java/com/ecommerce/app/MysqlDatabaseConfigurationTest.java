package com.ecommerce.app;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

class MysqlDatabaseConfigurationTest {

    @Test
    void runtimeProfilesAreMysqlOnly() throws IOException {
        for (String profile : new String[]{"application-local.properties", "application-remote.properties", "application-live.properties"}) {
            String content = read(Path.of("src/main/resources", profile));
            String normalized = content.toLowerCase(Locale.ROOT);
            assertTrue(normalized.contains("com.mysql.cj.jdbc.driver"), profile);
            assertTrue(normalized.contains("org.hibernate.dialect.mysqldialect"), profile);
            assertTrue(normalized.contains("classpath:db/migration/mysql"), profile);
            assertFalse(normalized.contains("${"), profile);
            assertFalse(normalized.contains("postgres"), profile);
            assertFalse(normalized.contains("derby"), profile);
            assertFalse(normalized.contains("jdbc:h2"), profile);
        }
    }

    @Test
    void baseProfileUsesTheConfiguredRemoteMysqlProfile() throws IOException {
        String content = read(Path.of("src/main/resources/application.properties"));
        assertTrue(content.contains("spring.profiles.active=remote"));
        assertFalse(content.contains("${SPRING_"));
    }

    @Test
    void localProfileUsesLiteralMysqlDefaults() throws IOException {
        String content = read(Path.of("src/main/resources/application-local.properties"));
        assertTrue(content.contains("spring.datasource.url=jdbc:mysql://localhost:3306/universes_ecommercenew"));
        assertTrue(content.contains("spring.datasource.username=root"));
        assertFalse(content.contains("${"));
    }

    @Test
    void buildAndResourcesContainNoNonMysqlDatabaseArtifacts() throws IOException {
        String pom = read(Path.of("pom.xml")).toLowerCase(Locale.ROOT);
        assertTrue(pom.contains("com.mysql"));
        assertTrue(pom.contains("flyway-mysql"));
        assertFalse(pom.contains("com.h2database"));
        assertFalse(pom.contains("org.postgresql"));
        assertFalse(pom.contains("org.apache.derby"));
        assertFalse(Files.exists(Path.of("src/test/resources/application-test.properties")));
        assertFalse(Files.exists(Path.of("src/main/resources/db/migration/postgresql")));
    }

    @Test
    void onlyMysqlFlywayMigrationsArePackagedAsDatabaseMigrations() throws IOException {
        Path migrationRoot = Path.of("src/main/resources/db/migration");
        assertTrue(Files.isDirectory(migrationRoot.resolve("mysql")));
        try (Stream<Path> files = Files.walk(migrationRoot)) {
            assertTrue(files.filter(Files::isRegularFile)
                    .map(Path::toString)
                    .noneMatch(path -> path.toLowerCase(Locale.ROOT).contains("postgres")));
        }
    }

    private static String read(Path path) throws IOException {
        return Files.readString(path);
    }
}
