package com.denticode.desktop.core;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Lightweight, read-only configuration loader.
 *
 * Resolves {@code application.properties} from the classpath, then layers JVM
 * system properties on top. Also computes the local data directory used for
 * the SQLite database and exposes the JDBC URL.
 */
public final class Config {

    private final Properties props = new Properties();
    private final Path dataDir;

    public Config() {
        try (InputStream in = getClass().getResourceAsStream("/application.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load application.properties", e);
        }

        for (String key : props.stringPropertyNames()) {
            String sys = System.getProperty(key);
            if (sys != null && !sys.isBlank()) {
                props.setProperty(key, sys);
            }
        }

        String configured = props.getProperty("app.dataDir", "").trim();
        Path resolved = configured.isBlank()
                ? Path.of(System.getProperty("user.home"), ".denticode-desktop")
                : Path.of(configured);
        try {
            Files.createDirectories(resolved);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot create data dir: " + resolved, e);
        }
        this.dataDir = resolved;
    }

    public String appName() {
        return props.getProperty("app.name", "Denti-Code Desktop");
    }

    public String appVersion() {
        return props.getProperty("app.version", "0.1.0");
    }

    public String defaultLocale() {
        return props.getProperty("app.locale", "en");
    }

    public boolean demoMode() {
        return Boolean.parseBoolean(props.getProperty("app.demoMode", "true"));
    }

    public Path dataDir() {
        return dataDir;
    }

    public Path databasePath() {
        return dataDir.resolve("denti-code.db");
    }

    public String jdbcUrl() {
        return "jdbc:sqlite:" + databasePath().toAbsolutePath();
    }
}
